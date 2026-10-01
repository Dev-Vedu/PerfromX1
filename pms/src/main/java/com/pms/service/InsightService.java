package com.pms.service;

import com.pms.dto.ProductivityScore;
import com.pms.dto.ProjectHealth;
import com.pms.entity.PerformanceReview;
import com.pms.entity.Task;
import com.pms.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/** Written performance insights and project risk narratives. */
@Service
@RequiredArgsConstructor
public class InsightService {

    private static final String PERF_SYSTEM = """
            You are an engineering manager writing a short, fair performance note for a
            teammate, based only on the metrics given. Be specific and practical.
            Do not invent numbers that are not in the input. Do not mention salary.

            Write exactly three short paragraphs with no headings:
            1. What the numbers show about delivery and reliability.
            2. The single biggest thing holding this person back, stated kindly.
            3. Two concrete habits to try over the next month.

            Keep the whole reply under 180 words. Address the person as "you".
            """;

    private static final String RISK_SYSTEM = """
            You are a delivery manager. Given a project's task metrics, explain in at most
            four sentences what is putting the project at risk and what the project lead
            should do this week. Be concrete, no filler, no headings, no bullet points.
            """;

    private final ClaudeClient claude;
    private final PerformanceService performanceService;

    public String performanceNote(User employee, ProductivityScore s, List<PerformanceReview> reviews) {
        String prompt = """
                Name: %s
                Tasks assigned: %d
                Tasks completed: %d
                Currently open: %d
                Currently overdue: %d
                Completion rate: %.1f%%
                Finished on or before deadline: %.1f%% of completed tasks
                Deadline extensions requested: %d
                Average review rating: %.1f out of 5
                Days active in the last 30: %d
                Recent review comments: %s
                """.formatted(employee.getFullName(), s.getTotalTasks(), s.getCompletedTasks(),
                s.getOpenTasks(), s.getOverdueTasks(), s.getCompletionRate(), s.getOnTimeRate(),
                s.getExtensionsRequested(), s.getAverageRating(), s.getAttendanceDays(),
                reviews.stream().limit(5)
                        .map(PerformanceReview::getComments)
                        .filter(c -> c != null && !c.isBlank())
                        .collect(Collectors.joining(" | ")));

        String reply = claude.ask(PERF_SYSTEM, prompt);
        return reply.isBlank() ? offlinePerformanceNote(s) : reply;
    }

    public String riskNote(ProjectHealth h, List<Task> overdue) {
        String prompt = """
                Project: %s
                Tasks: %d total, %d completed, %d overdue, %d critical still open
                Average progress: %d%%
                Computed risk score: %d out of 100 (%s)
                Overdue task titles: %s
                """.formatted(h.getProject().getName(), h.getTotalTasks(), h.getCompletedTasks(),
                h.getOverdueTasks(), h.getCriticalOpen(), h.getProgressPercent(), h.getRiskScore(), h.getRiskLevel(),
                overdue.stream().map(Task::getTitle).limit(8).collect(Collectors.joining(", ")));

        String reply = claude.ask(RISK_SYSTEM, prompt);
        return reply.isBlank() ? offlineRiskNote(h) : reply;
    }

    public String performanceNote(User employee) {
        return performanceNote(employee, performanceService.scoreFor(employee),
                performanceService.reviewsFor(employee));
    }

    private String offlinePerformanceNote(ProductivityScore s) {
        if (s.getTotalTasks() == 0) {
            return "Nothing to analyse yet. Once tasks are assigned and closed, this note fills in "
                    + "with delivery, timeliness and rating trends.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("You have closed ").append(s.getCompletedTasks()).append(" of ").append(s.getTotalTasks())
          .append(" tasks, a completion rate of ").append(s.getCompletionRate()).append("%. ")
          .append("Of the work you finished, ").append(s.getOnTimeRate())
          .append("% landed on or before the deadline.\n\n");

        if (s.getOverdueTasks() > 0) {
            sb.append("The clearest drag right now is the ").append(s.getOverdueTasks())
              .append(" overdue task(s) sitting open. Clearing those first will move the score most.\n\n");
        } else if (s.getExtensionsRequested() > 2) {
            sb.append("You have asked for ").append(s.getExtensionsRequested())
              .append(" deadline extensions. That usually points at optimistic estimates "
                      + "rather than slow work.\n\n");
        } else {
            sb.append("Nothing is blocking you sharply. Holding this pattern is the goal.\n\n");
        }
        sb.append("Two things to try this month: break any task longer than three days into "
                + "checkpoints you can update daily, and post a progress update before you log off "
                + "so the lead never has to ask.");
        return sb.toString();
    }

    private String offlineRiskNote(ProjectHealth h) {
        if (h.getRiskScore() >= 60) {
            return h.getProject().getName() + " is at high risk: " + h.getOverdueTasks()
                    + " task(s) are past deadline and average progress is " + h.getProgressPercent()
                    + "%. Reassign or rescope the overdue items this week.";
        }
        if (h.getRiskScore() >= 30) {
            return h.getProject().getName() + " needs watching. " + h.getHeadline()
                    + " Check in with owners of the critical tasks before the next deadline.";
        }
        return h.getProject().getName() + " looks healthy at " + h.getProgressPercent() + "% average progress.";
    }
}
