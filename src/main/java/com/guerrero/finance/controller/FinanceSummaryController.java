package com.guerrero.finance.controller;

import com.guerrero.finance.model.Account;
import com.guerrero.finance.model.AccountType;
import com.guerrero.finance.model.Transaction;
import com.guerrero.finance.model.TransactionType;
import com.guerrero.finance.service.AccountService;
import com.guerrero.finance.service.TransactionService;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance/summary")
@RequiredArgsConstructor
public class FinanceSummaryController {

    private final TransactionService transactionService;
    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<FinanceSummaryDto> getSummary(
            Principal principal,
            @RequestParam(required = false) String month) {
        
        String username = principal.getName();
        LocalDate startDate;
        LocalDate endDate;

        if (month != null && month.matches("^\\d{4}-\\d{2}$")) {
            YearMonth ym = YearMonth.parse(month);
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        } else {
            YearMonth ym = YearMonth.now();
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        }

        // Fetch monthly transactions
        List<Transaction> transactions = transactionService.getTransactionsByMonth(username, startDate, endDate);

        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        Map<String, BigDecimal> categoryBreakdown = new HashMap<>();

        for (Transaction t : transactions) {
            // Transfers do not count towards monthly net income/expenses
            if (t.getType() == TransactionType.INCOME) {
                totalIncome = totalIncome.add(t.getAmount());
            } else if (t.getType() == TransactionType.EXPENSE) {
                totalExpense = totalExpense.add(t.getAmount());
                String cat = t.getCategory();
                categoryBreakdown.put(cat, categoryBreakdown.getOrDefault(cat, BigDecimal.ZERO).add(t.getAmount()));
            }
        }

        BigDecimal balance = totalIncome.subtract(totalExpense);

        // Fetch all user accounts to compute consolidated Net Worth
        List<Account> accounts = accountService.getAccounts(username);
        BigDecimal netWorth = BigDecimal.ZERO;

        for (Account acc : accounts) {
            if (acc.getType() == AccountType.CREDIT_CARD) {
                // Credit card balances are debts, so they reduce Net Worth
                netWorth = netWorth.subtract(acc.getBalance());
            } else {
                netWorth = netWorth.add(acc.getBalance());
            }
        }

        FinanceSummaryDto summary = FinanceSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(balance)
                .categoryBreakdown(categoryBreakdown)
                .accounts(accounts)
                .netWorth(netWorth)
                .build();

        return ResponseEntity.ok(summary);
    }

    @Data
    @Builder
    public static class FinanceSummaryDto {
        private BigDecimal totalIncome;
        private BigDecimal totalExpense;
        private BigDecimal balance;
        private Map<String, BigDecimal> categoryBreakdown;
        private List<Account> accounts;
        private BigDecimal netWorth;
    }
}
