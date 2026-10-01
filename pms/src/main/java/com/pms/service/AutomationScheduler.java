package com.pms.service;

import com.pms.dto.ProjectHealth;
import com.pms.entity.*;
import com.pms.repository.ProjectRepository;
import com.pms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Everything that runs without anyone clicking: deadline reminders, approval nudges,
 * project risk alerts and the daily / weekly report notifications.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AutomationScheduler {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final ExtensionService extensionService;
    private final NotificationService notifications;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    /** Deadline reminders, every weekday at 09:00. */
    @Scheduled(cron = "0 0 9 * * MON-FRI")
    public void deadlineReminders() {
        LocalDate today = LocalDate.now();

        for (Task task : taskService.dueWithin(2)) {
            if (task.getAssignee() == null) continue;
            long days = task.daysLeft();
            String when = days <= 0 ? "today" : days == 1 ? "tomorrow" : "in " + days + " days";
            notifications.pushOnce(task.getAssignee(), "DEADLINE",
                    task.getTitle() + " is due " + when,
                    "/tasks/" + task.getId(),
                    "due-" + task.getId() + "-" + today);
        }

        for (Task task : taskService.overdue()) {
            if (task.getAssignee() != null) {
                notifications.pushOnce(task.getAssignee(), "DEADLINE",
                        task.getTitle() + " is past its deadline. Update it or request an extension.",
                        "/tasks/" + task.getId(),
                        "overdue-" + task.getId() + "-" + today);
            }
            notifications.pushOnce(task.getProject().getLead(), "RISK",
                    "Overdue: " + task.getTitle() + " in " + task.getProject().getName(),
                    "/projects/" + task.getProject().getId(),
                    "lead-overdue-" + task.getId() + "-" + today);
        }
        log.info("Deadline reminder sweep finished");
    }

    /** Nudges leads about approvals and reviews left hanging, twice a day. */
    @Scheduled(cron = "0 0 10,16 * * MON-FRI")
    public void pendingApprovalNudges() {
        LocalDate today = LocalDate.now();
        for (User lead : userRepository.findByRole(Role.LEAD)) {
            long pending = extensionService.pendingForLead(lead).size();
            if (pending > 0) {
                notifications.pushOnce(lead, "APPROVAL",
                        pending + " extension request" + (pending == 1 ? "" : "s") + " waiting on you",
                        "/extensions", "ext-nudge-" + lead.getId() + "-" + today);
            }
            long reviews = taskService.awaitingReview(lead).size();
            if (reviews > 0) {
                notifications.pushOnce(lead, "REVIEW",
                        reviews + " submitted task" + (reviews == 1 ? "" : "s") + " waiting for review",
                        "/tasks/review-queue", "rev-nudge-" + lead.getId() + "-" + today);
            }
        }
    }

    /** Project risk detection, every weekday at 08:00. */
    @Scheduled(cron = "0 0 8 * * MON-FRI")
    public void riskDetection() {
        LocalDate today = LocalDate.now();
        for (Project project : projectRepository.findByStatus(ProjectStatus.ACTIVE)) {
            ProjectHealth h = projectService.health(project);
            if (h.getRiskScore() >= 60) {
                notifications.pushOnce(project.getLead(), "RISK",
                        project.getName() + " is at high risk. " + h.getHeadline(),
                        "/projects/" + project.getId(),
                        "risk-" + project.getId() + "-" + today);
            }
        }
    }

    /** Daily digest at 18:00 and a weekly one on Friday. */
    @Scheduled(cron = "0 0 18 * * MON-FRI")
    public void dailyReport() {
        notifyLeads("Your daily project report is ready", "daily-report-");
    }

    @Scheduled(cron = "0 30 17 * * FRI")
    public void weeklyReport() {
        notifyLeads("Your weekly project report is ready", "weekly-report-");
    }

    private void notifyLeads(String message, String keyPrefix) {
        LocalDate today = LocalDate.now();
        List<User> leads = userRepository.findByRole(Role.LEAD);
        leads.forEach(lead -> notifications.pushOnce(lead, "REPORT", message, "/reports",
                keyPrefix + lead.getId() + "-" + today));
    }
}
