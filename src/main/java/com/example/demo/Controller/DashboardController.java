package com.example.demo.Controller;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;
import com.example.demo.Service.ScheduleService;
import com.example.demo.Service.UserService;
import com.example.demo.Service.WardrobeService;
import java.security.Principal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

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

        List<ClothingItem> recentItems = wardrobeService.findRecentByUser(user);

        Map<String, Long> categoryStats = new LinkedHashMap<>();
        for (ClothingItem item : wardrobeService.findAllByUser(user)) {
            String category = item.getCategory() != null ? item.getCategory() : "ITEM";
            categoryStats.put(category, categoryStats.getOrDefault(category, 0L) + 1);
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("itemCount", wardrobeService.countByUser(user));
        model.addAttribute("favoriteCount", wardrobeService.countFavoriteByUser(user));
        model.addAttribute("agendaCount", scheduleService.countByUser(user));
        model.addAttribute("upcomingCount", scheduleService.countUpcomingByUser(user));
        model.addAttribute("recentItems", recentItems);
        model.addAttribute("upcomingAgendas", scheduleService.findUpcomingByUser(user));
        model.addAttribute("categoryStats", categoryStats);

        return "dashboard";
    }
}
