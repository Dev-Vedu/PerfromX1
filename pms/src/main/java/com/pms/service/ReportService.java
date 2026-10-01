package com.pms.service;

import com.pms.dto.ProjectHealth;
import com.pms.entity.*;
import com.pms.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Builds the report tables and the plain-text digest used by the scheduler. */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d MMM yyyy");

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    public Map<TaskStatus, List<Task>> byStatus(List<Task> tasks) {
        return tasks.stream().collect(Collectors.groupingBy(Task::getStatus));
    }

    public List<Task> completed(List<Task> tasks) {
        return tasks.stream().filter(t -> t.getStatus() == TaskStatus.COMPLETED).toList();
    }

    public List<Task> pending(List<Task> tasks) {
        return tasks.stream().filter(t -> t.getStatus().isOpen() && !t.isOverdue()).toList();
    }

    public List<Task> overdue(List<Task> tasks) {
        return tasks.stream().filter(Task::isOverdue).toList();
    }

    public String digestFor(User lead) {
        List<Project> projects = projectService.ledBy(lead);
        StringBuilder sb = new StringBuilder();
        sb.append("Report for ").append(LocalDate.now().format(DATE)).append("\n\n");

        if (projects.isEmpty()) {
            sb.append("You are not leading any projects yet.");
            return sb.toString();
        }
        for (Project p : projects) {
            ProjectHealth h = projectService.health(p);
            sb.append(p.getName()).append(" — ").append(h.getProgressPercent()).append("% average progress, ")
              .append(h.getCompletedTasks()).append("/").append(h.getTotalTasks()).append(" tasks done");
            if (h.getOverdueTasks() > 0) sb.append(", ").append(h.getOverdueTasks()).append(" overdue");
            sb.append(". Risk: ").append(h.getRiskLevel()).append(".\n");
        }
        List<Task> dueSoon = taskRepository.findDueBetween(LocalDate.now(), LocalDate.now().plusDays(3));
        if (!dueSoon.isEmpty()) {
            sb.append("\nDue in the next three days:\n");
            dueSoon.forEach(t -> sb.append("  ").append(t.getTitle())
                    .append(" — ").append(t.getAssignee() == null ? "unassigned"
                            : t.getAssignee().getFullName())
                    .append(", ").append(t.getDeadline().format(DATE)).append("\n"));
        }
        return sb.toString();
    }
}
