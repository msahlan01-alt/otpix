package com.example.demo.Controller;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;
import com.example.demo.Service.UserService;
import com.example.demo.Service.WardrobeService;
import java.security.Principal;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GenerateController {

    private final UserService userService;
    private final WardrobeService wardrobeService;

    public GenerateController(UserService userService, WardrobeService wardrobeService) {
        this.userService = userService;
        this.wardrobeService = wardrobeService;
    }

    @GetMapping("/generate")
    public String generate(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());

        model.addAttribute("username", user.getUsername());

        return "generate";
    }

    @PostMapping("/generate")
    public String generateOutfit(
            @RequestParam String occasion,
            @RequestParam String mood,
            Principal principal,
            Model model) {
        User user = userService.getByUsername(principal.getName());
        List<ClothingItem> items = wardrobeService.findAllByUser(user);

        String recommendation;

        if (items.isEmpty()) {
            recommendation = "Wardrobe kamu masih kosong. Tambahkan item dulu agar rekomendasi outfit lebih akurat.";
        } else {
            recommendation = buildRecommendation(occasion, mood, items);
        }

        model.addAttribute("username", user.getUsername());
        model.addAttribute("occasion", occasion);
        model.addAttribute("mood", mood);
        model.addAttribute("recommendation", recommendation);
        model.addAttribute("items", items);

        return "generate";
    }

    private String buildRecommendation(String occasion, String mood, List<ClothingItem> items) {
        String top = findFirstByCategory(items, "TOPS");
        String bottom = findFirstByCategory(items, "BOTTOMS");
        String shoes = findFirstByCategory(items, "SHOES");
        String outer = findFirstByCategory(items, "OUTERWEAR");
        String accessory = findFirstByCategory(items, "ACCESSORIES");

        StringBuilder builder = new StringBuilder();

        builder.append("Untuk ")
                .append(occasion)
                .append(" dengan mood ")
                .append(mood)
                .append(", coba kombinasi: ");

        if (top != null) {
            builder.append(top);
        } else {
            builder.append("atasan favoritmu");
        }

        if (bottom != null) {
            builder.append(", ").append(bottom);
        }

        if (outer != null) {
            builder.append(", layer ").append(outer);
        }

        if (shoes != null) {
            builder.append(", dan ").append(shoes);
        }

        if (accessory != null) {
            builder.append(". Lengkapi dengan ").append(accessory);
        }

        builder.append(". Pastikan warna dan kenyamanan sesuai aktivitasmu.");

        return builder.toString();
    }

    private String findFirstByCategory(List<ClothingItem> items, String category) {
        return items.stream()
                .filter(item -> item.getCategory() != null)
                .filter(item -> item.getCategory().equalsIgnoreCase(category))
                .map(ClothingItem::getName)
                .findFirst()
                .orElse(null);
    }
}