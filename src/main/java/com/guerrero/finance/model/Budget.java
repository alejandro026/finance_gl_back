package com.guerrero.finance.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "budgets")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal limitAmount;

    @Column(nullable = false, length = 7)
    private String month; // Format: YYYY-MM

    @Column(nullable = false)
    private String username;
}
