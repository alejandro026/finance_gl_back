package com.guerrero.finance.service;

import com.guerrero.finance.model.Budget;
import java.util.List;

public interface BudgetService {
    List<Budget> getBudgets(String username, String month);
    Budget createOrUpdateBudget(String username, Budget budget);
    void deleteBudget(String username, Long id);
}
