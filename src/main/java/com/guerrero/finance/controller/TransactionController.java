package com.guerrero.finance.controller;

import com.guerrero.finance.model.Transaction;
import com.guerrero.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<Transaction>> getTransactions(
            Principal principal,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        String username = principal.getName();
        if (startDate != null && endDate != null) {
            return ResponseEntity.ok(transactionService.getTransactionsByMonth(username, startDate, endDate));
        }
        return ResponseEntity.ok(transactionService.getTransactions(username));
    }

    @PostMapping
    public ResponseEntity<Transaction> createTransaction(Principal principal, @RequestBody Transaction transaction) {
        return ResponseEntity.ok(transactionService.createTransaction(principal.getName(), transaction));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Transaction> updateTransaction(
            Principal principal,
            @PathVariable Long id,
            @RequestBody Transaction transactionDetails) {
        return ResponseEntity.ok(transactionService.updateTransaction(principal.getName(), id, transactionDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(Principal principal, @PathVariable Long id) {
        transactionService.deleteTransaction(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
