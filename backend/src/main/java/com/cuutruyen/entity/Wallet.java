package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Wallets")
@Data
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "wallet_id")
    private Integer walletId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private Long balance = 0L;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
