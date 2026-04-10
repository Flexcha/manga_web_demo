package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tx_id")
    private Integer txId;

    @ManyToOne
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "ref_id")
    private Integer refId;

    private String note;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum TransactionType {
        deposit, purchase, refund, withdrawal_pending, withdrawal_approved, withdrawal_rejected
    }
}
