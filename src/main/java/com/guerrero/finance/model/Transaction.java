package com.guerrero.finance.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions", uniqueConstraints = {
        @UniqueConstraint(name = "uk_transactions_user_external", columnNames = {"username", "external_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private String category;

    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false)
    private String username;

    private Long accountId;

    private Long destinationAccountId;

    /**
     * Llave de idempotencia enviada por la app móvil (hash de la notificación).
     * Si llega dos veces el mismo valor, se devuelve el movimiento ya creado.
     */
    @Column(name = "external_id", length = 80)
    private String externalId;
}
