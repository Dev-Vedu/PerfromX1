package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_logs")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WorkLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private User employee;

    private int progressBefore;
    private int progressAfter;

    @Column(length = 1500)
    private String note;

    @Builder.Default
    private LocalDateTime loggedAt = LocalDateTime.now();
}
