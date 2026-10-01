package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @Column(nullable = false, length = 400)
    private String message;

    @Column(length = 200)
    private String link;

    /** One of: TASK, DEADLINE, APPROVAL, RISK, REVIEW, REPORT */
    @Column(length = 20)
    @Builder.Default
    private String category = "TASK";

    /** Used to avoid sending the same reminder twice. */
    @Column(length = 120)
    private String dedupeKey;

    @Builder.Default
    private boolean seen = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
