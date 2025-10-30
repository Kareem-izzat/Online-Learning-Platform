package com.learnit.discussion.repository;

import com.learnit.discussion.entity.TargetType;
import com.learnit.discussion.entity.Vote;
import com.learnit.discussion.entity.VoteType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VoteRepository extends JpaRepository<Vote, Long> {

    // Find vote by user and target
    Optional<Vote> findByUserIdAndTargetTypeAndTargetId(
        Long userId, TargetType targetType, Long targetId);

    // Find all votes by user
    List<Vote> findByUserId(Long userId);

    // Find all votes for a target
    List<Vote> findByTargetTypeAndTargetId(TargetType targetType, Long targetId);

    // Count upvotes for a target
    Long countByTargetTypeAndTargetIdAndVoteType(
        TargetType targetType, Long targetId, VoteType voteType);

    // Get net votes for a target
    @Query("SELECT (COUNT(CASE WHEN v.voteType = 'UPVOTE' THEN 1 END) - " +
           "COUNT(CASE WHEN v.voteType = 'DOWNVOTE' THEN 1 END)) " +
           "FROM Vote v WHERE v.targetType = :targetType AND v.targetId = :targetId")
    Long getNetVotes(@Param("targetType") TargetType targetType, @Param("targetId") Long targetId);

    // Delete vote
    void deleteByUserIdAndTargetTypeAndTargetId(
        Long userId, TargetType targetType, Long targetId);

    // Check if user has voted
    boolean existsByUserIdAndTargetTypeAndTargetId(
        Long userId, TargetType targetType, Long targetId);
}
