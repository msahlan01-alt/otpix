package com.example.demo.Controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Model.User;
import com.example.demo.Service.UserService;

@Controller
public class TutorialController {

    private final UserService userService;

    public TutorialController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/welcome")
    public String welcomePage(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        if (user.isTutorialSeen()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("username", user.getUsername());
        return "welcome";
    }

    @GetMapping("/tutorial")
    public String tutorialPage(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        if (user.isTutorialSeen()) {
            return "redirect:/dashboard";
        }
        model.addAttribute("username", user.getUsername());
        return "tutorial";
    }

    @PostMapping("/tutorial/complete")
    public String completeTutorial(Principal principal) {
        userService.markTutorialSeen(principal.getName());
        return "redirect:/dashboard";
    }

    @GetMapping("/tutorial/skip")
    public String skipTutorial(Principal principal) {
        userService.markTutorialSeen(principal.getName());
        return "redirect:/dashboard";
    }
}