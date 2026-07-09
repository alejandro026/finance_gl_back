package com.guerrero.finance.repository;

import com.guerrero.finance.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByUsernameAndMonth(String username, String month);
    Optional<Budget> findByUsernameAndCategoryAndMonth(String username, String category, String month);
}
