package com.example.demo.Controller;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import com.example.demo.Model.User;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Service.UserService;

@Controller
public class TutorialController {

    private final UserService userService;
    private final UserRepository userRepository;

    public TutorialController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
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
        User user = userService.getByUsername(principal.getName());
        user.setTutorialSeen(true);
        userRepository.save(user);
        return "redirect:/dashboard";
    }

    // Untuk tombol "Lewati" — GET agar bisa dipakai dengan <a href>
    @GetMapping("/skip-tutorial")
    public String skipTutorial(Principal principal) {
        User user = userService.getByUsername(principal.getName());
        user.setTutorialSeen(true);
        userRepository.save(user);
        return "redirect:/dashboard";
    }
}
