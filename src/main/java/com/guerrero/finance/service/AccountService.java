package com.guerrero.finance.service;

import com.guerrero.finance.model.Account;
import java.util.List;

public interface AccountService {
    List<Account> getAccounts(String username);
    Account createAccount(String username, Account account);
    Account updateAccount(String username, Long id, Account accountDetails);
    void deleteAccount(String username, Long id);
    /**
     * Aplica un flujo de dinero a una cuenta DEL USUARIO.
     * amountChange > 0 = entra dinero, < 0 = sale dinero.
     * En tarjetas de crédito el saldo es deuda, así que el signo se invierte.
     */
    void adjustBalance(Long accountId, String username, java.math.BigDecimal amountChange);
}
