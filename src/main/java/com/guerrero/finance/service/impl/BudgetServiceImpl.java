package com.guerrero.finance.service.impl;

import com.guerrero.finance.model.Budget;
import com.guerrero.finance.repository.BudgetRepository;
import com.guerrero.finance.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;

    @Override
    public List<Budget> getBudgets(String username, String month) {
        return budgetRepository.findByUsernameAndMonth(username, month);
    }

    @Override
    @Transactional
    public Budget createOrUpdateBudget(String username, Budget budget) {
        budget.setUsername(username);
        budget.setId(null); // evita IDOR: el upsert es por categoría + mes, nunca por id del cliente
        Optional<Budget> existing = budgetRepository.findByUsernameAndCategoryAndMonth(
                username, budget.getCategory(), budget.getMonth()
        );

        if (existing.isPresent()) {
            Budget budgetToUpdate = existing.get();
            budgetToUpdate.setLimitAmount(budget.getLimitAmount());
            return budgetRepository.save(budgetToUpdate);
        }

        return budgetRepository.save(budget);
    }

    @Override
    @Transactional
    public void deleteBudget(String username, Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Budget not found with id: " + id));

        if (!budget.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this budget limit");
        }

        budgetRepository.delete(budget);
    }
}
