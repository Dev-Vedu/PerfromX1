package com.pms.repository;

import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.entity.WorkLog;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface WorkLogRepository extends JpaRepository<WorkLog, Long> {
    List<WorkLog> findByTaskOrderByLoggedAtDesc(Task task);
    List<WorkLog> findByEmployeeOrderByLoggedAtDesc(User employee, Pageable pageable);
    List<WorkLog> findByEmployeeAndLoggedAtAfter(User employee, LocalDateTime after);
}
