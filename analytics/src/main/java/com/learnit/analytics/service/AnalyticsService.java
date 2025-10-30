package com.learnit.analytics.service;

import com.learnit.analytics.dto.EventEnvelope;
import com.learnit.analytics.entity.EventProcessed;
import com.learnit.analytics.entity.ThreadAggregate;
import com.learnit.analytics.repository.EventProcessedRepository;
import com.learnit.analytics.repository.ThreadAggregateRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
public class AnalyticsService {

    private final ThreadAggregateRepository repo;
    private final EventProcessedRepository processedRepo;

    public AnalyticsService(ThreadAggregateRepository repo, EventProcessedRepository processedRepo) {
        this.repo = repo;
        this.processedRepo = processedRepo;
    }

    @Transactional
    public void processEvent(EventEnvelope event) {
        if (event == null) return;
        String eventId = event.getEventId();
        if (eventId != null && processedRepo.existsById(eventId)) {
            // already processed
            return;
        }

        String type = event.getEventType();
        Map<String, Object> p = event.getPayload();

        if (type == null) return;

        switch (type) {
            case "thread_created":
                handleThreadCreated(p);
                break;
            case "comment_added":
                handleCommentAdded(p);
                break;
            case "vote_cast":
                handleVoteCast(p);
                break;
            case "thread_viewed":
                handleThreadViewed(p);
                break;
            default:
                // ignore unknown for now
        }

        // record processed event for idempotency
        if (eventId != null) {
            EventProcessed ep = new EventProcessed(eventId, type, Instant.now());
            processedRepo.save(ep);
        }
    }

    @Transactional
    public void processEvents(List<EventEnvelope> events) {
        if (events == null || events.isEmpty()) return;
        for (EventEnvelope e : events) {
            processEvent(e);
        }
    }

    private void handleThreadCreated(Map<String, Object> p) {
        Number threadIdNum = (Number) p.get("threadId");
        Number courseIdNum = (Number) p.get("courseId");
        if (threadIdNum == null) return;
        Long threadId = threadIdNum.longValue();
        Long courseId = courseIdNum == null ? null : courseIdNum.longValue();

        if (!repo.existsById(threadId)) {
            ThreadAggregate a = new ThreadAggregate(threadId, courseId);
            repo.save(a);
        }
    }

    private void handleCommentAdded(Map<String, Object> p) {
        Number threadIdNum = (Number) p.get("threadId");
        if (threadIdNum == null) return;
        Long threadId = threadIdNum.longValue();
        ThreadAggregate a = repo.findById(threadId).orElseGet(() -> new ThreadAggregate(threadId, null));
        a.incrementComments();
        repo.save(a);
    }

    private void handleVoteCast(Map<String, Object> p) {
        Object targetType = p.get("targetType");
        if ("THREAD".equals(String.valueOf(targetType))) {
            Number targetIdNum = (Number) p.get("targetId");
            String voteType = p.get("voteType") == null ? null : String.valueOf(p.get("voteType"));
            if (targetIdNum == null || voteType == null) return;
            Long threadId = targetIdNum.longValue();
            ThreadAggregate a = repo.findById(threadId).orElseGet(() -> new ThreadAggregate(threadId, null));
            if ("UPVOTE".equalsIgnoreCase(voteType)) a.applyUpvote();
            else if ("DOWNVOTE".equalsIgnoreCase(voteType)) a.applyDownvote();
            repo.save(a);
        }
    }

    private void handleThreadViewed(Map<String, Object> p) {
        Number threadIdNum = (Number) p.get("threadId");
        if (threadIdNum == null) return;
        Long threadId = threadIdNum.longValue();
        ThreadAggregate a = repo.findById(threadId).orElseGet(() -> new ThreadAggregate(threadId, null));
        a.incrementViews();
        repo.save(a);
    }

    // Query helpers
    @Transactional(readOnly = true)
    public ThreadAggregate getThreadAggregate(Long threadId) {
        return repo.findById(threadId).orElse(null);
    }

    @Transactional(readOnly = true)
    public java.util.List<ThreadAggregate> getTopThreadsByCourse(Long courseId, int limit) {
        java.util.List<ThreadAggregate> list = repo.findByCourseId(courseId);
        // compute simple engagement score and sort
        list.sort((a, b) -> {
            int scoreA = (a.getViews() == null ? 0 : a.getViews())
                    + ((a.getComments() == null ? 0 : a.getComments()) * 2)
                    + (((a.getUpvotes() == null ? 0 : a.getUpvotes()) - (a.getDownvotes() == null ? 0 : a.getDownvotes())) * 5);
            int scoreB = (b.getViews() == null ? 0 : b.getViews())
                    + ((b.getComments() == null ? 0 : b.getComments()) * 2)
                    + (((b.getUpvotes() == null ? 0 : b.getUpvotes()) - (b.getDownvotes() == null ? 0 : b.getDownvotes())) * 5);
            return Integer.compare(scoreB, scoreA);
        });
        if (list.size() > limit) return list.subList(0, limit);
        return list;
    }
}
