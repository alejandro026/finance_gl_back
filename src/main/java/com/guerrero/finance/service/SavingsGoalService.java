package com.guerrero.finance.service;

import com.guerrero.finance.model.SavingsGoal;
import java.util.List;

public interface SavingsGoalService {
    List<SavingsGoal> getSavingsGoals(String username);
    SavingsGoal createSavingsGoal(String username, SavingsGoal savingsGoal);
    SavingsGoal updateSavingsGoal(String username, Long id, SavingsGoal savingsGoalDetails);
    void deleteSavingsGoal(String username, Long id);
}
