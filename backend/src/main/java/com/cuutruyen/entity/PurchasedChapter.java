package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "PurchasedChapters")
@IdClass(PurchasedChapterId.class)
@Data
public class PurchasedChapter {
    @Id
    @Column(name = "user_id")
    private Integer userId;

    @Id
    @Column(name = "chapter_id")
    private Integer chapterId;

    @Column(name = "price_paid", nullable = false)
    private Long pricePaid = 0L;

    @Column(name = "purchased_at", updatable = false)
    private LocalDateTime purchasedAt = LocalDateTime.now();

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "chapter_id", insertable = false, updatable = false)
    private Chapter chapter;
}

