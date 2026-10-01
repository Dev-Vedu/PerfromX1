package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(length = 4000)
    private String description;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "project_id")
    private Project project;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assignee_id")
    private User assignee;

    private LocalDate startDate;
    private LocalDate deadline;

    /** Deadline as first agreed, kept for deadline-adherence analytics. */
    private LocalDate originalDeadline;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private Priority priority = Priority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private TaskStatus status = TaskStatus.ASSIGNED;

    @Builder.Default
    private int progress = 0;

    @Builder.Default
    private int extensionCount = 0;

    @Column(length = 2000)
    private String submissionNote;

    @Column(length = 300)
    private String attachmentPath;

    @Column(length = 300)
    private String attachmentName;

    @Column(length = 2000)
    private String reviewComment;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime submittedAt;
    private LocalDateTime completedAt;

    public boolean isOverdue() {
        return deadline != null && status.isOpen() && LocalDate.now().isAfter(deadline);
    }

    public long daysLeft() {
        if (deadline == null) return Long.MAX_VALUE;
        return ChronoUnit.DAYS.between(LocalDate.now(), deadline);
    }

    /** True when the task was finished on or before the agreed deadline. */
    public boolean finishedOnTime() {
        return status == TaskStatus.COMPLETED && completedAt != null && deadline != null
                && !completedAt.toLocalDate().isAfter(deadline);
    }
}
