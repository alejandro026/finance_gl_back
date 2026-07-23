package com.guerrero.finance.service;

import com.guerrero.finance.model.Subscription;
import java.util.List;

public interface SubscriptionService {
    List<Subscription> getSubscriptions(String username);
    Subscription createSubscription(String username, Subscription subscription);
    Subscription updateSubscription(String username, Long id, Subscription subscriptionDetails);
    void deleteSubscription(String username, Long id);
    void processPendingSubscriptions(String username);
}
