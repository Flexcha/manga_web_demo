package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Chapters")
@Data
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Integer chapterId;

    @ManyToOne
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @Column(name = "chapter_number", nullable = false)
    private BigDecimal chapterNumber;

    private String title;

    @Column(name = "is_paid")
    private boolean isPaid = false;

    private Integer price = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
