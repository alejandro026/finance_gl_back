package com.guerrero.finance.service;

import com.guerrero.finance.model.Transaction;
import java.time.LocalDate;
import java.util.List;

public interface TransactionService {
    List<Transaction> getTransactions(String username);
    List<Transaction> getTransactionsByMonth(String username, LocalDate startDate, LocalDate endDate);
    Transaction createTransaction(String username, Transaction transaction);
    Transaction updateTransaction(String username, Long id, Transaction transactionDetails);
    void deleteTransaction(String username, Long id);
}
