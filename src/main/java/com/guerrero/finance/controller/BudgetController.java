package com.guerrero.finance.controller;

import com.guerrero.finance.model.Budget;
import com.guerrero.finance.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/finance/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<Budget>> getBudgets(Principal principal, @RequestParam String month) {
        return ResponseEntity.ok(budgetService.getBudgets(principal.getName(), month));
    }

    @PostMapping
    public ResponseEntity<Budget> createOrUpdateBudget(Principal principal, @RequestBody Budget budget) {
        return ResponseEntity.ok(budgetService.createOrUpdateBudget(principal.getName(), budget));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(Principal principal, @PathVariable Long id) {
        budgetService.deleteBudget(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
