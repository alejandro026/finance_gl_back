package com.guerrero.finance.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "accounts")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false)
    private String username;

    private String color;
}
