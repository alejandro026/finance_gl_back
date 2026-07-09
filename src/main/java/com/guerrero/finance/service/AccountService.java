package com.guerrero.finance.service;

import com.guerrero.finance.model.Account;
import java.util.List;

public interface AccountService {
    List<Account> getAccounts(String username);
    Account createAccount(String username, Account account);
    Account updateAccount(String username, Long id, Account accountDetails);
    void deleteAccount(String username, Long id);
    void adjustBalance(Long accountId, java.math.BigDecimal amountChange);
}
