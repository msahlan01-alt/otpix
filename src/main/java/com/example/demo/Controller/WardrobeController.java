package com.example.demo.Controller;

import java.security.Principal;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;
import com.example.demo.Service.UserService;
import com.example.demo.Service.WardrobeService;

@Controller
@RequestMapping("/wardrobe")
public class WardrobeController {

    private final UserService userService;
    private final WardrobeService wardrobeService;

    public WardrobeController(UserService userService, WardrobeService wardrobeService) {
        this.userService = userService;
        this.wardrobeService = wardrobeService;
    }

    // ===== LIST =====
    @GetMapping
    public String wardrobe(
            @RequestParam(required = false) String category,
            Model model,
            Principal principal) {
        User user = userService.getByUsername(principal.getName());

        List<ClothingItem> items = (category != null && !category.isBlank())
                ? wardrobeService.getItemsByCategory(principal.getName(), category)
                : wardrobeService.getItemsByUser(principal.getName());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("items", items);
        model.addAttribute("categories", wardrobeService.getValidCategories());
        model.addAttribute("selectedCategory", category);
        return "Wardrobe/wardrobe";
    }

    // ===== ADD =====
    @GetMapping("/add")
    public String addPage(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("categories", wardrobeService.getValidCategories());
        return "Wardrobe/wardrobe-add";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(defaultValue = "false") boolean favorite,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (name == null || name.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Nama item wajib diisi.");
            return "redirect:/wardrobe/add";
        }
        if (category == null || category.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Kategori wajib dipilih.");
            return "redirect:/wardrobe/add";
        }
        if (image == null || image.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Foto pakaian wajib diupload.");
            return "redirect:/wardrobe/add";
        }

        try {
            com.example.demo.Dto.ClothingItemForm form = new com.example.demo.Dto.ClothingItemForm();
            form.setName(name.trim());
            form.setCategory(category.trim());
            form.setImage(image);
            form.setFavorite(favorite);

            wardrobeService.addItem(form, principal.getName());
            redirectAttributes.addFlashAttribute("success",
                    "Item berhasil ditambahkan! Gemini sedang menganalisis pakaianmu.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/wardrobe/add";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Gagal menganalisis gambar. Item disimpan dengan metadata default.");
        }

        return "redirect:/wardrobe";
    }

    // ===== DETAIL =====
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model,
            Principal principal) {
        User user = userService.getByUsername(principal.getName());
        ClothingItem item = wardrobeService.getItemByIdAndUser(id, principal.getName());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("item", item);
        return "Wardrobe/wardrobe-detail";
    }

    // ===== EDIT =====
    @GetMapping("/{id}/edit")
    public String editPage(
            @PathVariable Long id,
            Model model,
            Principal principal) {
        User user = userService.getByUsername(principal.getName());
        ClothingItem item = wardrobeService.getItemByIdAndUser(id, principal.getName());

        model.addAttribute("username", user.getUsername());
        model.addAttribute("item", item);
        model.addAttribute("categories", wardrobeService.getValidCategories());
        return "Wardrobe/wardrobe-edit";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String category,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(defaultValue = "false") boolean favorite,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            com.example.demo.Dto.ClothingItemForm form = new com.example.demo.Dto.ClothingItemForm();
            form.setName(name.trim());
            form.setCategory(category.trim());
            form.setImage(image);
            form.setFavorite(favorite);

            wardrobeService.updateItem(id, form, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Item berhasil diupdate.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/wardrobe/" + id;
    }

    // ===== TOGGLE FAVORITE =====
    @PostMapping("/{id}/favorite")
    @ResponseBody
    public java.util.Map<String, Object> toggleFavorite(
            @PathVariable Long id,
            Principal principal) {
        boolean isFavorite = wardrobeService.toggleFavorite(id, principal.getName());
        return java.util.Map.of("favorite", isFavorite);
    }

    // ===== DELETE =====
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {
        try {
            wardrobeService.deleteItem(id, principal.getName());
            redirectAttributes.addFlashAttribute("success", "Item berhasil dihapus.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/wardrobe";
    }

    // ===== FAVORITES PAGE =====
    @GetMapping("/favorites")
    public String favorites(Model model, Principal principal) {
        User user = userService.getByUsername(principal.getName());
        model.addAttribute("username", user.getUsername());
        model.addAttribute("favorites", wardrobeService.getFavoriteItems(principal.getName()));
        return "Wardrobe/favorites";
    }
}