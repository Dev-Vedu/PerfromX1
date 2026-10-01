package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "performance_reviews")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PerformanceReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "task_id")
    private Task task;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private User employee;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    /** All four scored 1-5. */
    private int quality;
    private int timeliness;
    private int punctuality;
    private int professionalism;

    @Column(length = 2000)
    private String comments;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public double average() {
        return (quality + timeliness + punctuality + professionalism) / 4.0;
    }
}
