package com.pms.controller;

import com.pms.entity.Notification;
import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.service.AttendanceService;
import com.pms.service.NotificationService;
import com.pms.service.TaskService;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.security.Principal;
import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAdvice {

    private final UserService userService;
    private final NotificationService notificationService;
    private final TaskService taskService;
    private final AttendanceService attendanceService;

    @ModelAttribute
    public void common(Model model, Principal principal) {
        if (principal == null) return;
        User me = userService.current(principal);
        attendanceService.touch(me);

        List<Notification> notes = notificationService.recent(me, 8);
        model.addAttribute("me", me);
        model.addAttribute("notifications", notes);
        model.addAttribute("unreadCount", notificationService.unreadCount(me));
        model.addAttribute("isLead", me.getRole() == Role.LEAD);
        model.addAttribute("isHr", me.getRole() == Role.HR);

        if (me.getRole() == Role.EMPLOYEE) {
            model.addAttribute("openTasksForPrompt",
                    taskService.forAssignee(me).stream()
                            .filter(t -> t.getStatus().isOpen())
                            .limit(6).toList());
        }
    }
}
