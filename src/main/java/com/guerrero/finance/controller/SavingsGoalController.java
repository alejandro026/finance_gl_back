package com.guerrero.finance.controller;

import com.guerrero.finance.model.SavingsGoal;
import com.guerrero.finance.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/finance/savings")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService savingsGoalService;

    @GetMapping
    public ResponseEntity<List<SavingsGoal>> getSavingsGoals(Principal principal) {
        return ResponseEntity.ok(savingsGoalService.getSavingsGoals(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<SavingsGoal> createSavingsGoal(Principal principal, @RequestBody SavingsGoal savingsGoal) {
        return ResponseEntity.ok(savingsGoalService.createSavingsGoal(principal.getName(), savingsGoal));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SavingsGoal> updateSavingsGoal(
            Principal principal,
            @PathVariable Long id,
            @RequestBody SavingsGoal savingsGoalDetails) {
        return ResponseEntity.ok(savingsGoalService.updateSavingsGoal(principal.getName(), id, savingsGoalDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSavingsGoal(Principal principal, @PathVariable Long id) {
        savingsGoalService.deleteSavingsGoal(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
