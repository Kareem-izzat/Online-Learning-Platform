package com.learnit.analytics.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "thread_aggregate")
public class ThreadAggregate {
    @Id
    private Long threadId;

    @Column
    private Long courseId;

    @Column
    private Integer views = 0;

    @Column
    private Integer comments = 0;

    @Column
    private Integer upvotes = 0;

    @Column
    private Integer downvotes = 0;

    @Column
    private Instant lastUpdated;

    public ThreadAggregate() {}

    public ThreadAggregate(Long threadId, Long courseId) {
        this.threadId = threadId;
        this.courseId = courseId;
        this.lastUpdated = Instant.now();
    }

    public Long getThreadId() {
        return threadId;
    }

    public Long getCourseId() {
        return courseId;
    }

    public Integer getViews() {
        return views;
    }

    public Integer getComments() {
        return comments;
    }

    public Integer getUpvotes() {
        return upvotes;
    }

    public Integer getDownvotes() {
        return downvotes;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void incrementViews() {
        this.views = (this.views == null ? 1 : this.views + 1);
        this.lastUpdated = Instant.now();
    }

    public void incrementComments() {
        this.comments = (this.comments == null ? 1 : this.comments + 1);
        this.lastUpdated = Instant.now();
    }

    public void applyUpvote() {
        this.upvotes = (this.upvotes == null ? 1 : this.upvotes + 1);
        this.lastUpdated = Instant.now();
    }

    public void applyDownvote() {
        this.downvotes = (this.downvotes == null ? 1 : this.downvotes + 1);
        this.lastUpdated = Instant.now();
    }

    public void applyVoteDelta(int deltaUp, int deltaDown) {
        this.upvotes = (this.upvotes == null ? deltaUp : this.upvotes + deltaUp);
        this.downvotes = (this.downvotes == null ? deltaDown : this.downvotes + deltaDown);
        this.lastUpdated = Instant.now();
    }
}
