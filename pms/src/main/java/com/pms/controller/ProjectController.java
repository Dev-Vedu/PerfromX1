package com.pms.controller;

import com.pms.dto.ProjectForm;
import com.pms.entity.Project;
import com.pms.entity.User;
import com.pms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;
    private final InsightService insightService;

    @GetMapping
    public String list(Model model, Principal principal) {
        User me = userService.current(principal);
        model.addAttribute("health", projectService.healthFor(projectService.visibleTo(me)));
        return "project/list";
    }

    @GetMapping("/new")
    public String newProject(Model model) {
        model.addAttribute("form", new ProjectForm());
        model.addAttribute("people", userService.employees());
        return "project/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("form") ProjectForm form, BindingResult errors,
                       Model model, Principal principal, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            model.addAttribute("people", userService.employees());
            return "project/form";
        }
        Project saved = projectService.save(form, userService.current(principal));
        flash.addFlashAttribute("flash", "Saved " + saved.getName());
        return "redirect:/projects/" + saved.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Project project = projectService.byId(id);
        var health = projectService.health(project);
        model.addAttribute("project", project);
        model.addAttribute("health", health);
        model.addAttribute("tasks", taskService.forProject(project));
        model.addAttribute("candidates", userService.employees().stream()
                .filter(u -> project.getMembers().stream().noneMatch(m -> m.getId().equals(u.getId())))
                .collect(Collectors.toList()));
        model.addAttribute("riskNote", insightService.riskNote(health,
                taskService.forProject(project).stream().filter(t -> t.isOverdue()).toList()));
        return "project/detail";
    }

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Project p = projectService.byId(id);
        ProjectForm form = new ProjectForm();
        form.setId(p.getId());
        form.setName(p.getName());
        form.setDescription(p.getDescription());
        form.setStartDate(p.getStartDate());
        form.setTargetEndDate(p.getTargetEndDate());
        form.setStatus(p.getStatus());
        form.setMemberIds(p.getMembers().stream().map(User::getId).collect(Collectors.toList()));
        model.addAttribute("form", form);
        model.addAttribute("people", userService.employees());
        return "project/form";
    }

    @PostMapping("/{id}/members/add")
    public String addMember(@PathVariable Long id, @RequestParam Long userId, RedirectAttributes flash) {
        projectService.addMember(id, userId);
        flash.addFlashAttribute("flash", "Team member added");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/members/remove")
    public String removeMember(@PathVariable Long id, @RequestParam Long userId, RedirectAttributes flash) {
        projectService.removeMember(id, userId);
        flash.addFlashAttribute("flash", "Team member removed");
        return "redirect:/projects/" + id;
    }
}
