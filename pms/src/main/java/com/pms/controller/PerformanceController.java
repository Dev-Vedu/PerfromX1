package com.pms.controller;

import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/performance")
@RequiredArgsConstructor
public class PerformanceController {

    private final UserService userService;
    private final PerformanceService performanceService;
    private final InsightService insightService;

    @GetMapping
    @PreAuthorize("hasAnyRole('LEAD','HR')")
    public String team(Model model) {
        List<Object[]> rows = new ArrayList<>();
        for (User e : userService.employees()) {
            rows.add(new Object[]{e, performanceService.scoreFor(e)});
        }
        model.addAttribute("rows", rows);
        return "perf/team";
    }

    @GetMapping("/me")
    public String me(Model model, Principal principal) {
        return employee(userService.current(principal).getId(), model, principal);
    }

    @GetMapping("/{id}")
    public String employee(@PathVariable Long id, Model model, Principal principal) {
        User me = userService.current(principal);
        User employee = userService.byId(id);
        boolean self = me.getId().equals(employee.getId());
        if (!self && me.getRole() == Role.EMPLOYEE) {
            return "redirect:/performance/me";
        }
        model.addAttribute("employee", employee);
        model.addAttribute("score", performanceService.scoreFor(employee));
        model.addAttribute("reviews", performanceService.reviewsFor(employee));
        model.addAttribute("insight", insightService.performanceNote(employee));
        return "perf/employee";
    }
}
