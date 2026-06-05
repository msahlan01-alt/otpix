package com.example.demo.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Service.UserService;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/auth/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("error", "Username atau password salah.");
        if (logout != null)
            model.addAttribute("success", "Kamu berhasil logout.");
        return "auth/login";
    }

    @GetMapping("/auth/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/auth/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {
        // Validasi: semua field wajib diisi
        if (name == null || name.isBlank() ||
                email == null || email.isBlank() ||
                username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Semua field wajib diisi.");
            return "redirect:/auth/register";
        }

        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Password dan konfirmasi password tidak sama.");
            return "redirect:/auth/register";
        }

        try {
            userService.register(name, email, username, password);
            redirectAttributes.addFlashAttribute("success", "Registrasi berhasil. Silakan login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }
}