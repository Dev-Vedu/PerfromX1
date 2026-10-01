package com.pms.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ProductivityScore {
    private final long totalTasks;
    private final long completedTasks;
    private final long openTasks;
    private final long overdueTasks;
    /** 0-100 */
    private final double completionRate;
    /** 0-100, share of completed tasks that met the deadline */
    private final double onTimeRate;
    private final long extensionsRequested;
    /** 0-5, zero when never rated */
    private final double averageRating;
    private final long attendanceDays;
    /** 0-100 composite */
    private final int score;
    private final String band;

    public static ProductivityScore empty() {
        return new ProductivityScore(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "No data yet");
    }
}
