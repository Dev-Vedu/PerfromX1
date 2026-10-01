package com.pms.controller;

import com.pms.entity.CodeReview;
import com.pms.entity.Task;
import com.pms.entity.User;
import com.pms.service.CodeReviewService;
import com.pms.service.TaskService;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

/** The code optimization chatbot. */
@Controller
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final CodeReviewService codeReviewService;
    private final TaskService taskService;
    private final UserService userService;

    @GetMapping
    public String page(@RequestParam(required = false) Long taskId, Model model, Principal principal) {
        User me = userService.current(principal);
        model.addAttribute("history", codeReviewService.history(me, 10));
        model.addAttribute("myTasks", taskService.forAssignee(me).stream()
                .filter(t -> t.getStatus().isOpen()).toList());
        model.addAttribute("taskId", taskId);
        return "chatbot";
    }

    @PostMapping("/review")
    public String review(@RequestParam(required = false) Long taskId,
                         @RequestParam(required = false) String language,
                         @RequestParam String code,
                         Model model, Principal principal) {
        User me = userService.current(principal);
        Task task = taskId == null ? null : taskService.byId(taskId);
        CodeReview result = codeReviewService.review(me, task, language, code);

        model.addAttribute("result", result);
        model.addAttribute("history", codeReviewService.history(me, 10));
        model.addAttribute("myTasks", taskService.forAssignee(me).stream()
                .filter(t -> t.getStatus().isOpen()).toList());
        model.addAttribute("taskId", taskId);
        model.addAttribute("submittedCode", code);
        return "chatbot";
    }
}
