package com.learnit.discussion.dto;

import com.learnit.discussion.entity.TargetType;
import com.learnit.discussion.entity.VoteType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteResponse {

    private Long id;
    private Long userId;
    private TargetType targetType;
    private Long targetId;
    private VoteType voteType;
    private LocalDateTime createdAt;

    public VoteResponse(com.learnit.discussion.entity.Vote vote) {
        this.id = vote.getId();
        this.userId = vote.getUserId();
        this.targetType = vote.getTargetType();
        this.targetId = vote.getTargetId();
        this.voteType = vote.getVoteType();
        this.createdAt = vote.getCreatedAt();
    }
}
