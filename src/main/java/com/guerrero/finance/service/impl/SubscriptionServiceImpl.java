package com.guerrero.finance.service.impl;

import com.guerrero.finance.model.Subscription;
import com.guerrero.finance.model.Transaction;
import com.guerrero.finance.model.TransactionType;
import com.guerrero.finance.repository.SubscriptionRepository;
import com.guerrero.finance.service.SubscriptionService;
import com.guerrero.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final TransactionService transactionService;

    @Override
    @Transactional
    public List<Subscription> getSubscriptions(String username) {
        // Run billing processor to post pending transactions first
        processPendingSubscriptions(username);
        return subscriptionRepository.findByUsername(username);
    }

    @Override
    @Transactional
    public Subscription createSubscription(String username, Subscription subscription) {
        subscription.setUsername(username);
        subscription.setId(null); // evita IDOR: el id lo asigna la BD
        subscription.setActive(true);
        if (subscription.getNextBillingDate() == null) {
            subscription.setNextBillingDate(LocalDate.now());
        }
        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public Subscription updateSubscription(String username, Long id, Subscription subscriptionDetails) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));

        if (!sub.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized update attempt!");
        }

        sub.setName(subscriptionDetails.getName());
        sub.setAmount(subscriptionDetails.getAmount());
        sub.setCategory(subscriptionDetails.getCategory());
        sub.setNextBillingDate(subscriptionDetails.getNextBillingDate());
        sub.setBillingCycle(subscriptionDetails.getBillingCycle());
        sub.setActive(subscriptionDetails.isActive());
        sub.setAccountId(subscriptionDetails.getAccountId());

        return subscriptionRepository.save(sub);
    }

    @Override
    @Transactional
    public void deleteSubscription(String username, Long id) {
        Subscription sub = subscriptionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription not found: " + id));

        if (!sub.getUsername().equals(username)) {
            throw new RuntimeException("Unauthorized delete attempt!");
        }

        subscriptionRepository.delete(sub);
    }

    @Override
    @Transactional
    public void processPendingSubscriptions(String username) {
        LocalDate today = LocalDate.now();
        List<Subscription> pending = subscriptionRepository.findByUsernameAndActiveTrue(username);

        for (Subscription sub : pending) {
            // Keep posting transactions as long as next billing date is in the past (e.g. user hasn't logged in for months)
            while (sub.getNextBillingDate() != null && !sub.getNextBillingDate().isAfter(today)) {
                // Post transaction
                Transaction transaction = Transaction.builder()
                        .title("Cobro: " + sub.getName())
                        .amount(sub.getAmount())
                        .type(TransactionType.EXPENSE)
                        .category(sub.getCategory())
                        .date(sub.getNextBillingDate())
                        .accountId(sub.getAccountId())
                        .username(username)
                        .description(com.guerrero.finance.service.impl.TransactionServiceImpl.AUTO_CHARGE_DESCRIPTION)
                        .build();

                transactionService.createTransaction(username, transaction);

                // Advance next billing date
                String cycle = sub.getBillingCycle() != null ? sub.getBillingCycle().toUpperCase() : "MONTHLY";
                switch (cycle) {
                    case "WEEKLY":
                        sub.setNextBillingDate(sub.getNextBillingDate().plusWeeks(1));
                        break;
                    case "YEARLY":
                        sub.setNextBillingDate(sub.getNextBillingDate().plusYears(1));
                        break;
                    case "MONTHLY":
                    default:
                        sub.setNextBillingDate(sub.getNextBillingDate().plusMonths(1));
                        break;
                }
            }
            subscriptionRepository.save(sub);
        }
    }
}
