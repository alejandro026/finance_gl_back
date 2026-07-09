package com.guerrero.finance.service.impl;

import com.guerrero.finance.model.SavingsGoal;
import com.guerrero.finance.repository.SavingsGoalRepository;
import com.guerrero.finance.service.SavingsGoalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SavingsGoalServiceImpl implements SavingsGoalService {

    private final SavingsGoalRepository savingsGoalRepository;

    @Override
    public List<SavingsGoal> getSavingsGoals(String username) {
        return savingsGoalRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public SavingsGoal createSavingsGoal(String username, SavingsGoal savingsGoal) {
        savingsGoal.setUsername(username);
        return savingsGoalRepository.save(savingsGoal);
    }

    @Override
    @Transactional
    public SavingsGoal updateSavingsGoal(String username, Long id, SavingsGoal savingsGoalDetails) {
        SavingsGoal savingsGoal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings Goal not found with id: " + id));

        if (!savingsGoal.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this savings goal");
        }

        savingsGoal.setTitle(savingsGoalDetails.getTitle());
        savingsGoal.setTargetAmount(savingsGoalDetails.getTargetAmount());
        savingsGoal.setCurrentAmount(savingsGoalDetails.getCurrentAmount());
        savingsGoal.setDeadline(savingsGoalDetails.getDeadline());

        return savingsGoalRepository.save(savingsGoal);
    }

    @Override
    @Transactional
    public void deleteSavingsGoal(String username, Long id) {
        SavingsGoal savingsGoal = savingsGoalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Savings Goal not found with id: " + id));

        if (!savingsGoal.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized access to this savings goal");
        }

        savingsGoalRepository.delete(savingsGoal);
    }
}
