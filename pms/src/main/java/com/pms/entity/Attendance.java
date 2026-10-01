package com.pms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_id")
    private User employee;

    @Column(nullable = false)
    private LocalDate workDate;

    private LocalDateTime firstLogin;
    private LocalDateTime lastSeen;

    public long minutesOnline() {
        if (firstLogin == null || lastSeen == null) return 0;
        return Duration.between(firstLogin, lastSeen).toMinutes();
    }
}
