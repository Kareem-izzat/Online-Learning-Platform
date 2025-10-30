package com.learnit.discussion.dto;

import com.learnit.discussion.entity.ThreadCategory;
import com.learnit.discussion.entity.ThreadStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadResponse {

    private Long id;
    private Long courseId;
    private Long authorId;
    private String title;
    private String content;
    private ThreadCategory category;
    private ThreadStatus status;
    private Set<String> tags;
    private Boolean isPinned;
    private Boolean isLocked;
    private Integer viewCount;
    private Integer replyCount;
    private Integer upvotes;
    private Integer downvotes;
    private Long acceptedAnswerId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastActivityAt;
    
    // Computed field
    private Integer netVotes; // upvotes - downvotes

    public ThreadResponse(com.learnit.discussion.entity.DiscussionThread thread) {
        this.id = thread.getId();
        this.courseId = thread.getCourseId();
        this.authorId = thread.getAuthorId();
        this.title = thread.getTitle();
        this.content = thread.getContent();
        this.category = thread.getCategory();
        this.status = thread.getStatus();
        this.tags = thread.getTags();
        this.isPinned = thread.getIsPinned();
        this.isLocked = thread.getIsLocked();
        this.viewCount = thread.getViewCount();
        this.replyCount = thread.getReplyCount();
        this.upvotes = thread.getUpvotes();
        this.downvotes = thread.getDownvotes();
        this.acceptedAnswerId = thread.getAcceptedAnswerId();
        this.createdAt = thread.getCreatedAt();
        this.updatedAt = thread.getUpdatedAt();
        this.lastActivityAt = thread.getLastActivityAt();
        this.netVotes = thread.getUpvotes() - thread.getDownvotes();
    }
}
