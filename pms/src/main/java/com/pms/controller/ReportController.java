package com.pms.controller;

import com.pms.entity.Project;
import com.pms.entity.Role;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;

    @GetMapping
    public String reports(@RequestParam(required = false) Long projectId,
                          Model model, Principal principal) {
        User me = userService.current(principal);
        List<Project> projects = projectService.visibleTo(me);

        List<Task> tasks = new ArrayList<>();
        if (projectId != null) {
            Project p = projectService.byId(projectId);
            tasks.addAll(taskService.forProject(p));
            model.addAttribute("selected", p);
        } else {
            projects.forEach(p -> tasks.addAll(taskService.forProject(p)));
        }

        model.addAttribute("projects", projects);
        model.addAttribute("completed", reportService.completed(tasks));
        model.addAttribute("pending", reportService.pending(tasks));
        model.addAttribute("overdue", reportService.overdue(tasks));
        model.addAttribute("digest", me.getRole() == Role.LEAD ? reportService.digestFor(me) : null);
        return "reports";
    }
}
