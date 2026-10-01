package com.pms.controller;

import com.pms.service.PerformanceService;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardController {

    private final PerformanceService performanceService;
    private final UserService userService;

    @GetMapping
    public String rewards(Model model) {
        model.addAttribute("rewards", performanceService.rewardsFor(userService.employees()));
        return "perf/rewards";
    }
}
