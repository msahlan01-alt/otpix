package com.example.demo.Controller;

import com.example.demo.Model.OutfitRecommendation;
import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Service.GenerateService;
import com.example.demo.Service.ScheduleService;
import com.example.demo.Service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/generate")
public class GenerateController {

    private final GenerateService generateService;
    private final ScheduleService scheduleService;
    private final UserService userService;

    public GenerateController(GenerateService generateService,
            ScheduleService scheduleService,
            UserService userService) {
        this.generateService = generateService;
        this.scheduleService = scheduleService;
        this.userService = userService;
    }

    // Halaman utama generate — tampilkan daftar agenda user
    @GetMapping
    public String generatePage(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        List<Schedule> schedules = scheduleService.getSchedulesByUser(principal.getName());

        model.addAttribute("schedules", schedules);
        model.addAttribute("username", user.getUsername());
        return "Generate/generate";
    }

    // Generate outfit untuk agenda tertentu
    @PostMapping("/{scheduleId}")
    public String generate(
            @PathVariable Long scheduleId,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        User user = userService.getByUsername(principal.getName());

        try {
            Schedule schedule = scheduleService.getScheduleByIdAndUser(scheduleId, principal.getName());
            generateService.generate(schedule, user);
            redirectAttributes.addFlashAttribute("success", "Outfit berhasil digenerate!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Gagal menghubungi Gemini. Coba lagi.");
        }

        return "redirect:/generate/" + scheduleId + "/result";
    }

    // Halaman hasil rekomendasi
    @GetMapping("/{scheduleId}/result")
    public String result(
            @PathVariable Long scheduleId,
            Principal principal,
            Model model,
            RedirectAttributes redirectAttributes) {

        User user = userService.getByUsername(principal.getName());

        try {
            Schedule schedule = scheduleService.getScheduleByIdAndUser(scheduleId, principal.getName());
            OutfitRecommendation rec = generateService.getByAgendaId(scheduleId)
                    .orElse(null);

            model.addAttribute("schedule", schedule);
            model.addAttribute("recommendation", rec);
            model.addAttribute("username", user.getUsername());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Agenda tidak ditemukan.");
            return "redirect:/generate";
        }

        return "Generate/result";
    }
}