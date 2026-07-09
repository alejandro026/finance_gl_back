package com.guerrero.finance.service.impl;

import com.guerrero.finance.model.Transaction;
import com.guerrero.finance.model.TransactionType;
import com.guerrero.finance.repository.TransactionRepository;
import com.guerrero.finance.service.AccountService;
import com.guerrero.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

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

    private void applyBalanceAdjustment(Transaction tx) {
        BigDecimal amount = tx.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return;

        if (tx.getType() == TransactionType.INCOME && tx.getAccountId() != null) {
            accountService.adjustBalance(tx.getAccountId(), amount);
        } else if (tx.getType() == TransactionType.EXPENSE && tx.getAccountId() != null) {
            accountService.adjustBalance(tx.getAccountId(), amount.negate());
        } else if (tx.getType() == TransactionType.TRANSFER && tx.getAccountId() != null && tx.getDestinationAccountId() != null) {
            // Origin Account (AccountId) loses money
            accountService.adjustBalance(tx.getAccountId(), amount.negate());
            // Destination Account (DestinationAccountId) gains money
            accountService.adjustBalance(tx.getDestinationAccountId(), amount);
        }
    }

    private void revertBalanceAdjustment(Transaction tx) {
        BigDecimal amount = tx.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) == 0) return;

        if (tx.getType() == TransactionType.INCOME && tx.getAccountId() != null) {
            // Revert income = subtract money
            accountService.adjustBalance(tx.getAccountId(), amount.negate());
        } else if (tx.getType() == TransactionType.EXPENSE && tx.getAccountId() != null) {
            // Revert expense = add money back
            accountService.adjustBalance(tx.getAccountId(), amount);
        } else if (tx.getType() == TransactionType.TRANSFER && tx.getAccountId() != null && tx.getDestinationAccountId() != null) {
            // Revert transfer: add back to origin, subtract from destination
            accountService.adjustBalance(tx.getAccountId(), amount);
            accountService.adjustBalance(tx.getDestinationAccountId(), amount.negate());
        }
    }
}
