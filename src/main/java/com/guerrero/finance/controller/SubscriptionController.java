package com.guerrero.finance.controller;

import com.guerrero.finance.model.Subscription;
import com.guerrero.finance.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/finance/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions(Principal principal) {
        return ResponseEntity.ok(subscriptionService.getSubscriptions(principal.getName()));
    }

    @PostMapping
    public ResponseEntity<Subscription> createSubscription(Principal principal, @RequestBody Subscription subscription) {
        return ResponseEntity.ok(subscriptionService.createSubscription(principal.getName(), subscription));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Subscription> updateSubscription(
            Principal principal,
            @PathVariable Long id,
            @RequestBody Subscription subscriptionDetails) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(principal.getName(), id, subscriptionDetails));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubscription(Principal principal, @PathVariable Long id) {
        subscriptionService.deleteSubscription(principal.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
