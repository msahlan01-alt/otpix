package com.example.demo.Controller;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;
import com.example.demo.Service.UserService;
import com.example.demo.Service.WardrobeService;
import java.security.Principal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WardrobeController {

    private final UserService userService;
    private final WardrobeService wardrobeService;

    public WardrobeController(UserService userService, WardrobeService wardrobeService) {
        this.userService = userService;
        this.wardrobeService = wardrobeService;
    }

    @GetMapping("/wardrobe")
    public String wardrobe(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("items", wardrobeService.findAllByUser(user));

        return "wardrobe";
    }

    @GetMapping("/wardrobe/add")
    public String addWardrobeItem(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());

        return "wardrobe-add";
    }

    @PostMapping("/wardrobe/add")
    public String storeWardrobeItem(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String conditionStatus,
            @RequestParam(required = false) String imageUrl,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(principal.getName());

        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Nama item wajib diisi.");
            return "redirect:/wardrobe/add";
        }

        if (category == null || category.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Kategori wajib dipilih.");
            return "redirect:/wardrobe/add";
        }

        wardrobeService.create(
                user,
                name.trim(),
                category.trim(),
                color != null ? color.trim() : "",
                conditionStatus != null ? conditionStatus.trim() : "",
                imageUrl != null ? imageUrl.trim() : "");

        redirectAttributes.addFlashAttribute("success", "Item berhasil ditambahkan.");
        return "redirect:/wardrobe";
    }

    @GetMapping("/wardrobe/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Principal principal) {
        User user = userService.getByUsername(principal.getName());
        ClothingItem item = wardrobeService.findOwnedItem(id, user);

        model.addAttribute("username", user.getUsername());
        model.addAttribute("item", item);

        return "wardrobe-detail";
    }

    @PostMapping("/wardrobe/{id}/favorite")
    public String toggleFavorite(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(principal.getName());
        wardrobeService.toggleFavorite(id, user);

        redirectAttributes.addFlashAttribute("success", "Status favorit diperbarui.");
        return "redirect:/wardrobe";
    }

    @PostMapping("/wardrobe/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        User user = userService.getByUsername(principal.getName());
        wardrobeService.delete(id, user);

        redirectAttributes.addFlashAttribute("success", "Item berhasil dihapus.");
        return "redirect:/wardrobe";
    }
}