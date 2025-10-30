package com.learnit.discussion.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long threadId;

    private Long parentCommentId; // For nested replies

    @Column(nullable = false)
    private Long authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean isAnswer = false; // Marked as best answer

    @Column(nullable = false)
    private Boolean isEdited = false;

    @Column(nullable = false)
    private Integer upvotes = 0;

    @Column(nullable = false)
    private Integer downvotes = 0;

    @Column(nullable = false)
    private Integer depth = 0; // Nesting level (0 = top-level comment)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        isEdited = true;
    }

    public void addUpvote() {
        this.upvotes++;
    }

    public void removeUpvote() {
        if (this.upvotes > 0) {
            this.upvotes--;
        }
    }

    public void addDownvote() {
        this.downvotes++;
    }

    public void removeDownvote() {
        if (this.downvotes > 0) {
            this.downvotes--;
        }
    }

    public void markAsAnswer() {
        this.isAnswer = true;
    }

    public void unmarkAsAnswer() {
        this.isAnswer = false;
    }
}
