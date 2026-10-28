package com.guerrero.finance.service.impl;

import com.guerrero.finance.model.Subscription;
import com.guerrero.finance.model.Transaction;
import com.guerrero.finance.model.TransactionType;
import com.guerrero.finance.repository.SubscriptionRepository;
import com.guerrero.finance.repository.TransactionRepository;
import com.guerrero.finance.service.AccountService;
import com.guerrero.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;
    private final SubscriptionRepository subscriptionRepository;

    /** Marca de los cargos que genera el procesador de suscripciones. */
    public static final String AUTO_CHARGE_DESCRIPTION = "Cobro automático recurrente de suscripción activa.";

    @Override
    public List<Transaction> getTransactions(String username) {
        return transactionRepository.findByUsernameOrderByDateDesc(username);
    }

    @Override
    public List<Transaction> getTransactionsByMonth(String username, LocalDate startDate, LocalDate endDate) {
        return transactionRepository.findByUsernameAndDateBetweenOrderByDateDesc(username, startDate, endDate);
    }

    @Override
    @Transactional
    public Transaction createTransaction(String username, Transaction transaction) {
        transaction.setUsername(username);
        // El id lo asigna la BD: un id enviado por el cliente convertiría el POST en un UPDATE
        // de un registro ajeno (IDOR).
        transaction.setId(null);

        // Idempotencia: la app reintenta envíos; si ya llegó, se devuelve el mismo registro.
        String externalId = transaction.getExternalId();
        if (externalId != null && !externalId.isBlank()) {
            Optional<Transaction> existing = transactionRepository.findFirstByUsernameAndExternalId(username, externalId);
            if (existing.isPresent()) return existing.get();
        } else {
            transaction.setExternalId(null);
        }

        reconcileWithSubscription(username, transaction);

        // Persist transaction first
        Transaction savedTransaction = transactionRepository.save(transaction);
        
        // Apply balance adjustment to linked accounts
        applyBalanceAdjustment(savedTransaction);
        
        return savedTransaction;
    }

    @Override
    @Transactional
    public Transaction updateTransaction(String username, Long id, Transaction transactionDetails) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        if (!transaction.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this transaction");
        }

        // 1. Revert impact of old transaction balances
        revertBalanceAdjustment(transaction);

        // 2. Update properties
        transaction.setTitle(transactionDetails.getTitle());
        transaction.setAmount(transactionDetails.getAmount());
        transaction.setDate(transactionDetails.getDate());
        transaction.setCategory(transactionDetails.getCategory());
        transaction.setDescription(transactionDetails.getDescription());
        transaction.setType(transactionDetails.getType());
        transaction.setAccountId(transactionDetails.getAccountId());
        transaction.setDestinationAccountId(transactionDetails.getDestinationAccountId());

        // 3. Save updated entity
        Transaction updatedTransaction = transactionRepository.save(transaction);

        // 4. Apply new balance adjustment
        applyBalanceAdjustment(updatedTransaction);

        return updatedTransaction;
    }

    @Override
    @Transactional
    public void deleteTransaction(String username, Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));

        if (!transaction.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this transaction");
        }

        // Revert balance adjustments before deleting
        revertBalanceAdjustment(transaction);

        transactionRepository.delete(transaction);
    }

    /**
     * Evita contar dos veces un cargo de suscripción: el procesador crea "Cobro: X" y la app
     * captura la notificación real del banco.
     *  a) Si el "Cobro: X" automático ya existe cerca de la fecha → se elimina (gana el real).
     *  b) Si aún no se generó → se adelanta nextBillingDate para que no se genere.
     */
    private void reconcileWithSubscription(String username, Transaction tx) {
        if (tx.getType() != TransactionType.EXPENSE || tx.getAmount() == null || tx.getDate() == null
                || tx.getTitle() == null || AUTO_CHARGE_DESCRIPTION.equals(tx.getDescription())) {
            return;
        }
        String title = normalize(tx.getTitle());
        for (Subscription sub : subscriptionRepository.findByUsernameAndActiveTrue(username)) {
            String name = normalize(sub.getName());
            if (name.length() < 3 || !(title.contains(name) || name.contains(title))) continue;
            if (!amountClose(sub.getAmount(), tx.getAmount())) continue;

            List<Transaction> autos = transactionRepository.findByUsernameAndTitleAndDateBetween(
                    username, "Cobro: " + sub.getName(), tx.getDate().minusDays(5), tx.getDate().plusDays(5));
            for (Transaction auto : autos) {
                if (AUTO_CHARGE_DESCRIPTION.equals(auto.getDescription())) {
                    deleteTransaction(username, auto.getId());
                    return;
                }
            }
            if (sub.getNextBillingDate() != null
                    && Math.abs(ChronoUnit.DAYS.between(tx.getDate(), sub.getNextBillingDate())) <= 5) {
                sub.advanceBillingDate();
                subscriptionRepository.save(sub);
            }
            return;
        }
    }

    private static String normalize(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return n.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    /** ±10 % (los cobros en USD o con IVA varían un poco). */
    private static boolean amountClose(BigDecimal expected, BigDecimal actual) {
        if (expected == null || expected.signum() == 0) return false;
        BigDecimal diff = expected.subtract(actual).abs();
        return diff.divide(expected.abs(), 4, RoundingMode.HALF_UP).compareTo(new BigDecimal("0.10")) <= 0;
    }

    private void applyBalanceAdjustment(Transaction tx) {
        BigDecimal amount = tx.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return;

        if (tx.getType() == TransactionType.INCOME && tx.getAccountId() != null) {
            accountService.adjustBalance(tx.getAccountId(), tx.getUsername(), amount);
        } else if (tx.getType() == TransactionType.EXPENSE && tx.getAccountId() != null) {
            accountService.adjustBalance(tx.getAccountId(), tx.getUsername(), amount.negate());
        } else if (tx.getType() == TransactionType.TRANSFER && tx.getAccountId() != null && tx.getDestinationAccountId() != null) {
            // Origin Account (AccountId) loses money
            accountService.adjustBalance(tx.getAccountId(), tx.getUsername(), amount.negate());
            // Destination Account (DestinationAccountId) gains money
            accountService.adjustBalance(tx.getDestinationAccountId(), tx.getUsername(), amount);
        }
    }

    private void revertBalanceAdjustment(Transaction tx) {
        BigDecimal amount = tx.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return;

        if (tx.getType() == TransactionType.INCOME && tx.getAccountId() != null) {
            // Revert income = subtract money
            accountService.adjustBalance(tx.getAccountId(), tx.getUsername(), amount.negate());
        } else if (tx.getType() == TransactionType.EXPENSE && tx.getAccountId() != null) {
            // Revert expense = add money back
            accountService.adjustBalance(tx.getAccountId(), tx.getUsername(), amount);
        } else if (tx.getType() == TransactionType.TRANSFER && tx.getAccountId() != null && tx.getDestinationAccountId() != null) {
            // Revert transfer: add back to origin, subtract from destination
            accountService.adjustBalance(tx.getAccountId(), tx.getUsername(), amount);
            accountService.adjustBalance(tx.getDestinationAccountId(), tx.getUsername(), amount.negate());
        }
    }
}
