package com.pms.repository;

import com.pms.entity.PerformanceReview;
import com.pms.entity.Task;
import com.pms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerformanceReviewRepository extends JpaRepository<PerformanceReview, Long> {
    List<PerformanceReview> findByEmployeeOrderByCreatedAtDesc(User employee);
    Optional<PerformanceReview> findByTask(Task task);
    boolean existsByTask(Task task);
}
