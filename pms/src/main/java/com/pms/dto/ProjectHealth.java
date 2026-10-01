package com.pms.dto;

import com.pms.entity.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;

/** risk alerts. */
@Getter
@AllArgsConstructor
public class ProjectHealth {
    private final Project project;
    private final long totalTasks;
    private final long completedTasks;
    private final long overdueTasks;
    private final long criticalOpen;
    private final int progressPercent;
    /** 0-100, higher is worse */
    private final int riskScore;
    /** Low / Watch / High */
    private final String riskLevel;
    private final String headline;
}
