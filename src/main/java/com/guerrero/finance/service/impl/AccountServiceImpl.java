package com.guerrero.finance.service.impl;

import com.guerrero.finance.model.Account;
import com.guerrero.finance.model.AccountType;
import com.guerrero.finance.repository.AccountRepository;
import com.guerrero.finance.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public List<Account> getAccounts(String username) {
        List<Account> accounts = accountRepository.findByUsername(username);
        if (accounts.isEmpty()) {
            // Automatically initialize a default cash account
            Account defaultAccount = Account.builder()
                    .name("Efectivo")
                    .type(AccountType.CASH)
                    .balance(BigDecimal.ZERO)
                    .username(username)
                    .color("emerald")
                    .build();
            accountRepository.save(defaultAccount);
            return List.of(defaultAccount);
        }
        return accounts;
    }

    @Override
    @Transactional
    public Account createAccount(String username, Account account) {
        account.setUsername(username);
        account.setId(null); // evita IDOR: el id lo asigna la BD
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public Account updateAccount(String username, Long id, Account accountDetails) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (!account.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this account");
        }

        account.setName(accountDetails.getName());
        account.setType(accountDetails.getType());
        account.setColor(accountDetails.getColor());
        if (accountDetails.getBalance() != null) {
            account.setBalance(accountDetails.getBalance());
        }

        return accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(String username, Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));

        if (!account.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this account");
        }

        accountRepository.delete(account);
    }

    @Override
    @Transactional
    public void adjustBalance(Long accountId, String username, BigDecimal amountChange) {
        Account account = accountRepository.findById(accountId)
                // Mismo error si no existe o es de otro usuario: no revela ids ajenos.
                .filter(a -> a.getUsername().equals(username))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

        // Tarjeta de crédito: el saldo es lo que se debe. Un gasto (flujo negativo) AUMENTA la deuda
        // y un pago/abono (flujo positivo) la reduce. Consistente con el cálculo de netWorth.
        BigDecimal delta = account.getType() == AccountType.CREDIT_CARD ? amountChange.negate() : amountChange;
        account.setBalance(account.getBalance().add(delta));
        accountRepository.save(account);
    }
}
