package com.learnit.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentResponse {

    private Long id;
    private Long threadId;
    private Long parentCommentId;
    private Long authorId;
    private String content;
    private Boolean isAnswer;
    private Boolean isEdited;
    private Integer upvotes;
    private Integer downvotes;
    private Integer depth;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Computed field
    private Integer netVotes;

    public CommentResponse(com.learnit.discussion.entity.Comment comment) {
        this.id = comment.getId();
        this.threadId = comment.getThreadId();
        this.parentCommentId = comment.getParentCommentId();
        this.authorId = comment.getAuthorId();
        this.content = comment.getContent();
        this.isAnswer = comment.getIsAnswer();
        this.isEdited = comment.getIsEdited();
        this.upvotes = comment.getUpvotes();
        this.downvotes = comment.getDownvotes();
        this.depth = comment.getDepth();
        this.createdAt = comment.getCreatedAt();
        this.updatedAt = comment.getUpdatedAt();
        this.netVotes = comment.getUpvotes() - comment.getDownvotes();
    }
}
