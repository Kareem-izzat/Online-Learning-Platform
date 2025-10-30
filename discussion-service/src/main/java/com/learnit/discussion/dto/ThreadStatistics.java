package com.learnit.discussion.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ThreadStatistics {

    private Long threadId;
    private Integer totalViews;
    private Integer totalReplies;
    private Integer totalUpvotes;
    private Integer totalDownvotes;
    private Integer netVotes;
    private Boolean hasAcceptedAnswer;
    private Double engagementScore; // Custom formula: (views + replies*2 + netVotes*5)
}
