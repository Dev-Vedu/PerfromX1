package com.pms.controller;

import com.pms.dto.ProjectHealth;
import com.pms.entity.*;
import com.pms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final ExtensionService extensionService;
    private final PerformanceService performanceService;
    private final NotificationService notificationService;
    private final InsightService insightService;

    @GetMapping("/")
    public String root() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User me = userService.current(principal);

        if (me.getRole() == Role.LEAD) {
            List<Project> projects = projectService.ledBy(me);
            List<ProjectHealth> health = projectService.healthFor(projects);
            model.addAttribute("health", health);
            model.addAttribute("pendingExtensions", extensionService.pendingForLead(me));
            model.addAttribute("reviewQueue", taskService.awaitingReview(me));
            model.addAttribute("riskiest", health.isEmpty() ? null : health.get(0));
            return "dashboard-lead";
        }

        if (me.getRole() == Role.HR) {
            model.addAttribute("rewards", performanceService.rewardsFor(userService.employees()));
            model.addAttribute("health", projectService.healthFor(projectService.visibleTo(me)));
            return "dashboard-hr";
        }

        List<Task> mine = taskService.forAssignee(me);
        model.addAttribute("tasks", mine);
        model.addAttribute("openTasks", mine.stream().filter(t -> t.getStatus().isOpen()).toList());
        model.addAttribute("overdue", mine.stream().filter(Task::isOverdue).toList());
        model.addAttribute("dueSoon", mine.stream()
                .filter(t -> t.getStatus().isOpen() && t.daysLeft() >= 0 && t.daysLeft() <= 3).toList());
        model.addAttribute("score", performanceService.scoreFor(me));
        model.addAttribute("projects", projectService.visibleTo(me));
        return "dashboard-employee";
    }

    @GetMapping("/notifications")
    public String notifications(Model model, Principal principal) {
        User me = userService.current(principal);
        model.addAttribute("allNotifications", notificationService.recent(me, 60));
        notificationService.markAllSeen(me);
        return "notifications";
    }

    @PostMapping("/session/wrap-up")
    public String wrapUp() {
        return "redirect:/logout";
    }

    @GetMapping("/insights/me")
    public String myInsight(Model model, Principal principal) {
        User me = userService.current(principal);
        model.addAttribute("employee", me);
        model.addAttribute("score", performanceService.scoreFor(me));
        model.addAttribute("reviews", performanceService.reviewsFor(me));
        model.addAttribute("insight", insightService.performanceNote(me));
        return "perf/employee";
    }
}
