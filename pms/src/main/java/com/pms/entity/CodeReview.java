package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/** One round of AI code review requested by an employee from the chatbot. */
@Entity
@Table(name = "code_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CodeReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private User employee;

    @Column(length = 40)
    private String language;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String code;

    /** OPTIMIZED | NEEDS_WORK | UNCLEAR */
    @Column(length = 20)
    private String verdict;

    private int score;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String findings;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
