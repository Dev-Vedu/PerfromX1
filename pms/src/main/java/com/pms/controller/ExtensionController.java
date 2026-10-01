package com.pms.controller;

import com.pms.entity.Role;
import com.pms.entity.User;
import com.pms.service.ExtensionService;
import com.pms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/extensions")
@RequiredArgsConstructor
public class ExtensionController {

    private final ExtensionService extensionService;
    private final UserService userService;

    @GetMapping
    public String list(Model model, Principal principal) {
        User me = userService.current(principal);
        if (me.getRole() == Role.LEAD) {
            model.addAttribute("requests", extensionService.pendingForLead(me));
            model.addAttribute("forLead", true);
        } else {
            model.addAttribute("requests", extensionService.raisedBy(me));
            model.addAttribute("forLead", false);
        }
        return "task/extensions";
    }

    @PostMapping("/{id}/decide")
    public String decide(@PathVariable Long id,
                         @RequestParam boolean approve,
                         @RequestParam(required = false) String comment,
                         RedirectAttributes flash) {
        extensionService.decide(id, approve, comment);
        flash.addFlashAttribute("flash", approve ? "Extension approved" : "Extension rejected");
        return "redirect:/extensions";
    }
}
