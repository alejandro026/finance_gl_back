package com.guerrero.finance.repository;

import com.guerrero.finance.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUsername(String username);
    List<Subscription> findByUsernameAndActiveTrue(String username);
    List<Subscription> findByActiveTrueAndNextBillingDateLessThanEqual(LocalDate date);
}
