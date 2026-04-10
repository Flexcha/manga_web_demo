package com.cuutruyen.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "Comments")
@Data
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "series_id", nullable = false)
    private Series series;

    @ManyToOne
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    @ManyToOne
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL)
    private List<Comment> replies;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
