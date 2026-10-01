package com.pms.repository;

import com.pms.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectOrderByDeadlineAsc(Project project);

    List<Task> findByAssigneeOrderByDeadlineAsc(User assignee);

    List<Task> findByAssigneeAndStatus(User assignee, TaskStatus status);

    List<Task> findByStatus(TaskStatus status);

    long countByAssignee(User assignee);

    long countByAssigneeAndStatus(User assignee, TaskStatus status);

    @Query("select t from Task t where t.status <> com.pms.entity.TaskStatus.COMPLETED and t.deadline < :today")
    List<Task> findOverdue(@Param("today") LocalDate today);

    @Query("select t from Task t where t.status <> com.pms.entity.TaskStatus.COMPLETED "
            + "and t.deadline between :from and :to")
    List<Task> findDueBetween(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("select t from Task t where t.project = :project "
            + "and t.status <> com.pms.entity.TaskStatus.COMPLETED and t.deadline < :today")
    List<Task> findOverdueForProject(@Param("project") Project project, @Param("today") LocalDate today);

    @Query("select t from Task t where t.project.lead = :lead and t.status = :status")
    List<Task> findByLeadAndStatus(@Param("lead") User lead, @Param("status") TaskStatus status);

    @Query("select t from Task t where t.project.lead = :lead")
    List<Task> findAllForLead(@Param("lead") User lead);
}
