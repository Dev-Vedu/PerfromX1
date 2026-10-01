package com.pms.controller;

import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.service.PerformanceService;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final PerformanceService performanceService;

    @GetMapping("/people")
    public String people(Model model) {
        model.addAttribute("people", userService.all());
        model.addAttribute("roles", Role.values());
        return "admin/people";
    }

    @PostMapping("/people")
    public String create(@RequestParam String username, @RequestParam String fullName,
                         @RequestParam String email, @RequestParam Role role,
                         @RequestParam String designation, @RequestParam String password,
                         RedirectAttributes flash) {
        userService.create(User.builder()
                .username(username).fullName(fullName).email(email)
                .role(role).designation(designation).build(), password);
        flash.addFlashAttribute("flash", fullName + " can now sign in");
        return "redirect:/admin/people";
    }

    @PostMapping("/people/{id}/active")
    public String toggle(@PathVariable Long id, @RequestParam boolean active,
                         RedirectAttributes flash) {
        userService.setActive(id, active);
        flash.addFlashAttribute("flash", active ? "Account enabled" : "Account disabled");
        return "redirect:/admin/people";
    }

    @PostMapping("/people/{id}/password")
    public String password(@PathVariable Long id, @RequestParam String password,
                           RedirectAttributes flash) {
        userService.resetPassword(id, password);
        flash.addFlashAttribute("flash", "Password reset");
        return "redirect:/admin/people";
    }
}
