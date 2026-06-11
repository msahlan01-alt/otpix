package com.example.demo.Controller;

import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Service.ScheduleService;
import com.example.demo.Service.UserService;
import com.example.demo.Service.WardrobeService;

@Controller
public class DashboardController {

    private final UserService userService;
    private final WardrobeService wardrobeService;
    private final ScheduleService scheduleService;

    public DashboardController(
            UserService userService,
            WardrobeService wardrobeService,
            ScheduleService scheduleService) {
        this.userService = userService;
        this.wardrobeService = wardrobeService;
        this.scheduleService = scheduleService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        String username = principal.getName();

        if (!user.isTutorialSeen()) {
            return "redirect:/welcome";
        }

        List<ClothingItem> allItems = wardrobeService.getItemsByUser(username);
        List<ClothingItem> recentItems = wardrobeService.getRecentItems(username);
        List<Schedule> upcomingAgendas = scheduleService.getUpcomingSchedules(username);

        // Hitung stats per kategori
        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (ClothingItem item : allItems) {
            String category = item.getCategory() != null ? item.getCategory() : "Lainnya";
            categoryStats.merge(category, 1L, (oldValue, newValue) -> oldValue + newValue);
        }

        long favoriteCount = allItems.stream().filter(ClothingItem::isFavorite).count();

        model.addAttribute("username", user.getUsername());
        model.addAttribute("itemCount", (long) allItems.size());
        model.addAttribute("favoriteCount", favoriteCount);
        model.addAttribute("agendaCount", (long) scheduleService.getSchedulesByUser(username).size());
        model.addAttribute("upcomingCount", (long) upcomingAgendas.size());
        model.addAttribute("recentItems", recentItems);
        model.addAttribute("upcomingAgendas", upcomingAgendas);
        model.addAttribute("categoryStats", categoryStats);

        return "dashboard";
    }
}