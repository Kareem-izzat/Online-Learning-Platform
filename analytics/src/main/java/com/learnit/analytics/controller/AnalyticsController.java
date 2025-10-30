package com.learnit.analytics.controller;

import com.learnit.analytics.dto.EventEnvelope;
import com.learnit.analytics.service.AnalyticsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
import com.learnit.analytics.entity.ThreadAggregate;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingest(@RequestBody EventEnvelope envelope) {
        analyticsService.processEvent(envelope);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("accepted");
    }

    @PostMapping("/ingest/batch")
    public ResponseEntity<String> ingestBatch(@RequestBody java.util.List<EventEnvelope> envelopes) {
        analyticsService.processEvents(envelopes);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("accepted");
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<ThreadAggregate> getThreadAggregate(@PathVariable Long threadId) {
        ThreadAggregate agg = analyticsService.getThreadAggregate(threadId);
        if (agg == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(agg);
    }

    @GetMapping("/courses/{courseId}/top")
    public ResponseEntity<List<ThreadAggregate>> getTopThreadsByCourse(
            @PathVariable Long courseId,
            @RequestParam(defaultValue = "10") int limit) {
        List<ThreadAggregate> list = analyticsService.getTopThreadsByCourse(courseId, limit);
        return ResponseEntity.ok(list);
    }
}
