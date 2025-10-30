package com.learnit.discussion.dto;

import com.learnit.discussion.entity.TargetType;
import com.learnit.discussion.entity.VoteType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoteRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Target type is required")
    private TargetType targetType;

    @NotNull(message = "Target ID is required")
    private Long targetId;

    @NotNull(message = "Vote type is required")
    private VoteType voteType;
}
