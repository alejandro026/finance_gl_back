package com.guerrero.finance.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "subscriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, name = "next_billing_date")
    private LocalDate nextBillingDate;

    @Column(nullable = false, name = "billing_cycle")
    private String billingCycle; // MONTHLY, YEARLY, WEEKLY

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "account_id")
    private Long accountId;

    @Column(nullable = false)
    private String username;

    /** Mueve nextBillingDate al siguiente ciclo (MONTHLY por defecto). */
    public void advanceBillingDate() {
        if (nextBillingDate == null) return;
        String cycle = billingCycle != null ? billingCycle.toUpperCase() : "MONTHLY";
        nextBillingDate = switch (cycle) {
            case "WEEKLY" -> nextBillingDate.plusWeeks(1);
            case "YEARLY" -> nextBillingDate.plusYears(1);
            default -> nextBillingDate.plusMonths(1);
        };
    }
}
