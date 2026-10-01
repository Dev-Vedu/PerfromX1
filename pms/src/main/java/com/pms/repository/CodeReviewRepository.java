package com.pms.repository;

import com.pms.entity.CodeReview;
import com.pms.entity.Task;
import com.pms.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CodeReviewRepository extends JpaRepository<CodeReview, Long> {
    List<CodeReview> findByEmployeeOrderByCreatedAtDesc(User employee, Pageable pageable);
    List<CodeReview> findByTaskOrderByCreatedAtDesc(Task task);
}
