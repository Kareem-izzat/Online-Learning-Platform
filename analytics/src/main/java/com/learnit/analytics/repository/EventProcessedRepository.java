package com.learnit.analytics.repository;

import com.learnit.analytics.entity.EventProcessed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventProcessedRepository extends JpaRepository<EventProcessed, String> {
}
