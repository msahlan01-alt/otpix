package com.example.demo.Controller;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Service.UserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // ==================== LOGIN ====================

    @GetMapping("/auth/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null)
            model.addAttribute("error", "Username atau password salah.");
        if (logout != null)
            model.addAttribute("success", "Kamu berhasil logout.");
        return "Auth/login";
    }

    @PostMapping("/auth/login")
    public String loginStep1(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            session.setAttribute("pendingLoginUsername", username);
            session.setAttribute("pendingLoginPassword", password);
            userService.sendLoginOtp(username);
            return "redirect:/auth/login/otp";
        } catch (org.springframework.security.core.AuthenticationException e) {
            redirectAttributes.addFlashAttribute("error", "Username atau password salah.");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/auth/login/otp")
    public String loginOtpPage(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("pendingLoginUsername") == null) {
            redirectAttributes.addFlashAttribute("error", "Sesi tidak valid. Silakan login ulang.");
            return "redirect:/auth/login";
        }
        return "Auth/login-otp";
    }

    @PostMapping("/auth/login/otp")
    public String loginStep2(
            @RequestParam String otp,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String username = (String) session.getAttribute("pendingLoginUsername");
        String password = (String) session.getAttribute("pendingLoginPassword");

        if (username == null || password == null) {
            redirectAttributes.addFlashAttribute("error", "Sesi tidak valid. Silakan login ulang.");
            return "redirect:/auth/login";
        }

        try {
            userService.verifyLoginOtp(username, otp);
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));
            SecurityContextHolder.getContext().setAuthentication(auth);
            session.removeAttribute("pendingLoginUsername");
            session.removeAttribute("pendingLoginPassword");
            return "redirect:/dashboard";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/login/otp";
        }
    }

    // ==================== REGISTER ====================

    @GetMapping("/auth/register")
    public String registerPage() {
        return "Auth/register";
    }

    @PostMapping("/auth/register")
    public String register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        if (name.isBlank() || email.isBlank() || username.isBlank() || password.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Semua field wajib diisi.");
            return "redirect:/auth/register";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Password dan konfirmasi password tidak sama.");
            return "redirect:/auth/register";
        }

        try {
            userService.register(name, email, username, password);
            session.setAttribute("pendingVerifyUsername", username);
            redirectAttributes.addFlashAttribute("success", "Registrasi berhasil! Cek email kamu untuk kode OTP.");
            return "redirect:/auth/verify";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/auth/verify")
    public String verifyPage(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("pendingVerifyUsername") == null) {
            redirectAttributes.addFlashAttribute("error", "Sesi tidak valid.");
            return "redirect:/auth/register";
        }
        return "Auth/verify-otp";
    }

    @PostMapping("/auth/verify")
    public String verifyOtp(
            @RequestParam String otp,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String username = (String) session.getAttribute("pendingVerifyUsername");
        if (username == null) {
            redirectAttributes.addFlashAttribute("error", "Sesi tidak valid.");
            return "redirect:/auth/register";
        }

        try {
            userService.verifyRegisterOtp(username, otp);
            session.removeAttribute("pendingVerifyUsername");
            redirectAttributes.addFlashAttribute("success", "Email berhasil diverifikasi! Silakan login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/verify";
        }
    }

    @PostMapping("/auth/resend-otp")
    public String resendOtp(HttpSession session, RedirectAttributes redirectAttributes) {
        String username = (String) session.getAttribute("pendingVerifyUsername");
        if (username == null) {
            return "redirect:/auth/register";
        }
        try {
            userService.sendLoginOtp(username);
            redirectAttributes.addFlashAttribute("success", "OTP baru telah dikirim ke email kamu.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal mengirim OTP. Coba lagi.");
        }
        return "redirect:/auth/verify";
    }

    // ==================== FORGOT PASSWORD ====================

    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage() {
        return "Auth/forgot-password";
    }

    @PostMapping("/auth/forgot-password")
    public String forgotPassword(
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            userService.sendForgotPasswordOtp(email);
            session.setAttribute("resetEmail", email);
            redirectAttributes.addFlashAttribute("success", "Kode OTP telah dikirim ke email kamu.");
            return "redirect:/auth/reset-password";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/forgot-password";
        }
    }

    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("resetEmail") == null) {
            redirectAttributes.addFlashAttribute("error", "Sesi tidak valid.");
            return "redirect:/auth/forgot-password";
        }
        return "Auth/reset-password";
    }

    @PostMapping("/auth/reset-password")
    public String resetPassword(
            @RequestParam String otp,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        String email = (String) session.getAttribute("resetEmail");
        if (email == null) {
            redirectAttributes.addFlashAttribute("error", "Sesi tidak valid.");
            return "redirect:/auth/forgot-password";
        }
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Password tidak sama.");
            return "redirect:/auth/reset-password";
        }

        try {
            userService.resetPassword(email, otp, newPassword);
            session.removeAttribute("resetEmail");
            redirectAttributes.addFlashAttribute("success", "Password berhasil direset! Silakan login.");
            return "redirect:/auth/login";
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/reset-password";
        }
    }
}