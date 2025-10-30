package com.learnit.analytics.repository;

import com.learnit.analytics.entity.ThreadAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ThreadAggregateRepository extends JpaRepository<ThreadAggregate, Long> {
	List<ThreadAggregate> findByCourseId(Long courseId);
}
