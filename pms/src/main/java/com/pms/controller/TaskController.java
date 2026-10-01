package com.pms.controller;

import com.pms.dto.ReviewForm;
import com.pms.dto.TaskForm;
import com.pms.entity.*;
import com.pms.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDate;

@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;
    private final ExtensionService extensionService;
    private final PerformanceService performanceService;
    private final CodeReviewService codeReviewService;

    @GetMapping
    public String myTasks(Model model, Principal principal) {
        User me = userService.current(principal);
        model.addAttribute("tasks", me.getRole() == Role.LEAD
                ? taskService.forAssignee(me)
                : taskService.forAssignee(me));
        return "task/my-tasks";
    }

    @GetMapping("/new")
    public String newTask(@RequestParam(required = false) Long projectId,
                          Model model, Principal principal) {
        TaskForm form = new TaskForm();
        form.setProjectId(projectId);
        form.setDeadline(LocalDate.now().plusDays(7));
        model.addAttribute("form", form);
        model.addAttribute("projects", projectService.ledBy(userService.current(principal)));
        model.addAttribute("people", userService.employees());
        return "task/form";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("form") TaskForm form, BindingResult errors,
                       Model model, Principal principal, RedirectAttributes flash) {
        if (errors.hasErrors()) {
            model.addAttribute("projects", projectService.ledBy(userService.current(principal)));
            model.addAttribute("people", userService.employees());
            return "task/form";
        }
        Task saved = taskService.assign(form);
        flash.addFlashAttribute("flash", "Task assigned: " + saved.getTitle());
        return "redirect:/tasks/" + saved.getId();
    }

    @GetMapping("/review-queue")
    public String reviewQueue(Model model, Principal principal) {
        model.addAttribute("tasks", taskService.awaitingReview(userService.current(principal)));
        return "task/review-queue";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Principal principal) {
        Task task = taskService.byId(id);
        User me = userService.current(principal);
        if (!taskService.canEmployeeSee(task, me)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This task is not shared with you");
        }
        model.addAttribute("task", task);
        model.addAttribute("logs", taskService.logs(task));
        model.addAttribute("extensions", extensionService.forTask(task));
        model.addAttribute("codeReviews", codeReviewService.forTask(task));
        model.addAttribute("reviewForm", new ReviewForm());
        model.addAttribute("isOwner", task.getAssignee() != null
                && task.getAssignee().getId().equals(me.getId()));
        model.addAttribute("isProjectLead", task.getProject().getLead().getId().equals(me.getId()));
        return "task/detail";
    }

    @PostMapping("/{id}/progress")
    public String progress(@PathVariable Long id, @RequestParam int progress,
                           @RequestParam(required = false) String note,
                           Principal principal, RedirectAttributes flash) {
        Task task = taskService.byId(id);
        taskService.updateProgress(task, userService.current(principal), progress, note);
        flash.addFlashAttribute("flash", "Progress updated to " + progress + "%");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/bulk-progress")
    public String bulkProgress(@RequestParam("taskId") Long[] taskIds,
                               @RequestParam("progress") int[] values,
                               Principal principal) {
        User me = userService.current(principal);
        for (int i = 0; i < taskIds.length && i < values.length; i++) {
            Task t = taskService.byId(taskIds[i]);
            taskService.updateProgress(t, me, values[i], "End of session update");
        }
        return "redirect:/logout";
    }

    @PostMapping("/{id}/submit")
    public String submit(@PathVariable Long id,
                         @RequestParam(required = false) String note,
                         @RequestParam(required = false) MultipartFile file,
                         Principal principal, RedirectAttributes flash) {
        Task task = taskService.byId(id);
        taskService.submitForReview(task, userService.current(principal), note, file);
        flash.addFlashAttribute("flash", "Sent to your lead for review");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, @RequestParam(required = false) String comment,
                           RedirectAttributes flash) {
        taskService.complete(taskService.byId(id), comment);
        flash.addFlashAttribute("flash", "Marked complete. Rate the work below.");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/revision")
    public String revision(@PathVariable Long id, @RequestParam(required = false) String comment,
                           RedirectAttributes flash) {
        taskService.requestRevision(taskService.byId(id), comment);
        flash.addFlashAttribute("flash", "Revision requested");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/rate")
    public String rate(@PathVariable Long id, @ModelAttribute ReviewForm form,
                       Principal principal, RedirectAttributes flash) {
        Task task = taskService.byId(id);
        performanceService.rate(form, task, userService.current(principal));
        flash.addFlashAttribute("flash", "Rating saved");
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/{id}/extension")
    public String extension(@PathVariable Long id,
                            @RequestParam String requestedDeadline,
                            @RequestParam String reason,
                            Principal principal, RedirectAttributes flash) {
        Task task = taskService.byId(id);
        extensionService.raise(task, userService.current(principal),
                LocalDate.parse(requestedDeadline), reason);
        flash.addFlashAttribute("flash", "Extension request sent to your lead");
        return "redirect:/tasks/" + id;
    }
}
