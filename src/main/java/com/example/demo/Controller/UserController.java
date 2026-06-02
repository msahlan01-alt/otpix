package com.example.demo.Controller;

import com.example.demo.Model.User;
import com.example.demo.Service.UserService;
import java.security.Principal;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/complete-tutorial")
    public ResponseEntity<String> completeTutorial(Principal principal) {
        if (principal != null) {
            User user = userService.getByUsername(principal.getName());

            user.setHasSeenTutorial(true);
            userService.save(user);

            return ResponseEntity.ok("Status tutorial diperbarui.");
        }
        return ResponseEntity.badRequest().body("User tidak terautentikasi.");
    }
}