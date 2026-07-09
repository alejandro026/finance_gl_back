package com.guerrero.finance.repository;

import com.guerrero.finance.model.Transaction;
import com.guerrero.finance.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUsername(String username);
    List<Transaction> findByUsernameOrderByDateDesc(String username);
    List<Transaction> findByUsernameAndDateBetweenOrderByDateDesc(String username, LocalDate startDate, LocalDate endDate);
    List<Transaction> findByUsernameAndTypeAndDateBetween(String username, TransactionType type, LocalDate startDate, LocalDate endDate);
}
