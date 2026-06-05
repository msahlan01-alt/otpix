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
public class ScheduleController {

    private final UserService userService;
    private final ScheduleService scheduleService;

    public ScheduleController(UserService userService, ScheduleService scheduleService) {
        this.userService = userService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/schedule")
    public String schedule(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("agendas", scheduleService.findAllByUser(user));

        return "schedule";
    }

    @GetMapping("/schedule/add")
    public String addSchedule(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());

        return "schedule-add";
    }

    @PostMapping("/schedule/add")
    public String storeSchedule(
            @RequestParam String title,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime endTime,
            @RequestParam(required = false) String dressCode,
            @RequestParam(required = false) String eventType,
            @RequestParam(required = false) String location,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(principal.getName());

        if (title == null || title.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Judul event wajib diisi.");
            return "redirect:/schedule/add";
        }

        if (eventDate == null) {
            redirectAttributes.addFlashAttribute("error", "Tanggal event wajib diisi.");
            return "redirect:/schedule/add";
        }

        scheduleService.create(
                user,
                title.trim(),
                eventDate,
                startTime,
                endTime,
                dressCode != null ? dressCode.trim() : "",
                eventType != null && !eventType.trim().isEmpty() ? eventType.trim() : "CASUAL",
                location != null ? location.trim() : "");

        redirectAttributes.addFlashAttribute("success", "Event berhasil ditambahkan.");
        return "redirect:/schedule";
    }

    @GetMapping("/schedule/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Principal principal) {
        User user = userService.getByUsername(principal.getName());
        Schedule agenda = scheduleService.findOwnedAgenda(id, user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("agenda", agenda);

        return "schedule-detail";
    }

    @PostMapping("/schedule/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(principal.getName());
        scheduleService.delete(id, user);

        redirectAttributes.addFlashAttribute("success", "Event berhasil dihapus.");
        return "redirect:/schedule";
    }
}