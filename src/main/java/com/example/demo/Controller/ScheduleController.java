package com.example.demo.Controller;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Service.ScheduleService;
import com.example.demo.Service.UserService;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/schedule")
public class ScheduleController {

    private final UserService userService;
    private final ScheduleService scheduleService;

    public ScheduleController(UserService userService, ScheduleService scheduleService) {
        this.userService = userService;
        this.scheduleService = scheduleService;
    }

    @GetMapping
    public String schedule(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("agendas", scheduleService.getSchedulesByUser(principal.getName()));
        return "Schedule/schedule";
    }

    @GetMapping("/add")
    public String addPage(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        return "Schedule/schedule-add";
    }

    @PostMapping("/add")
    public String store(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) String dressCode,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String location,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (title == null || title.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Judul event wajib diisi.");
            return "redirect:/schedule/add";
        }
        if (eventDate == null) {
            redirectAttributes.addFlashAttribute("error", "Tanggal event wajib diisi.");
            return "redirect:/schedule/add";
        }

        scheduleService.create(
                principal.getName(),
                title.trim(),
                eventDate,
                startTime,
                endTime,
                dressCode != null ? dressCode.trim() : "",
                eventType != null && !eventType.isBlank() ? eventType.trim() : "Casual",
                location != null ? location.trim() : "");

        redirectAttributes.addFlashAttribute("success", "Event berhasil ditambahkan.");
        return "redirect:/schedule";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Principal principal) {
        User user = userService.getByUsername(principal.getName());
        Schedule agenda = scheduleService.getScheduleByIdAndUser(id, principal.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("agenda", agenda);
        return "Schedule/schedule-detail";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        scheduleService.delete(id, principal.getName());
        redirectAttributes.addFlashAttribute("success", "Event berhasil dihapus.");
        return "redirect:/schedule";
    }
}