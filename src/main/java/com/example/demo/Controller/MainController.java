package com.example.demo.Controller;

import com.example.demo.Model.*;
import com.example.demo.Repository.CategoryRepository;
import com.example.demo.Repository.TagRepository;
import com.example.demo.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
public class MainController {
    private final UserService userService;
    private final WardrobeService wardrobeService;
    private final AgendaService agendaService;
    private final GenerateService generateService;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    @Value("${app.upload.dir}")
    private String uploadDir;

    public MainController(UserService userService, WardrobeService wardrobeService,
            AgendaService agendaService, GenerateService generateService,
            CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.userService = userService;
        this.wardrobeService = wardrobeService;
        this.agendaService = agendaService;
        this.generateService = generateService;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            throw new RuntimeException("Not logged in");
        return userService.findById(userId);
    }

    @GetMapping("/register")
    public String registerForm() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name, @RequestParam String email,
            @RequestParam String password, HttpSession session, Model model) {
        try {
            User user = userService.register(name, email, password);
            session.setAttribute("userId", user.getId());
            return "redirect:/";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
            HttpSession session, Model model) {
        Optional<User> userOpt = userService.login(email, password);
        if (userOpt.isPresent()) {
            session.setAttribute("userId", userOpt.get().getId());
            return "redirect:/";
        }
        model.addAttribute("error", "Invalid credentials");
        return "login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null)
            return "redirect:/login";
        model.addAttribute("user", userService.findById(userId));
        return "home";
    }

    @GetMapping("/wardrobe")
    public String wardrobe(HttpSession session,
            @RequestParam(required = false) Long edit,
            Model model) {
        User user = getCurrentUser(session);
        model.addAttribute("items", wardrobeService.getAllItems(user.getId()));
        model.addAttribute("allTags", tagRepository.findAll());
        model.addAttribute("categories", categoryRepository.findAll());
        if (edit != null) {
            ClothingItem editItem = wardrobeService.findById(edit);
            if (editItem.getUser().getId().equals(user.getId())) {
                model.addAttribute("editItem", editItem);
            }
        }
        return "wardrobe";
    }

    @PostMapping("/wardrobe/add")
    public String addItem(@ModelAttribute ClothingItem item,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session) throws IOException {
        User user = getCurrentUser(session);
        item.setUser(user);
        item.setTimesWorn(0);
        if (!imageFile.isEmpty()) {
            item.setImageUrl(saveImage(imageFile));
        }
        if (tagIds != null) {
            item.setTags(new HashSet<>(tagRepository.findAllById(tagIds)));
        }
        wardrobeService.saveItem(item);
        return "redirect:/wardrobe";
    }

    @PostMapping("/wardrobe/edit/{id}")
    public String editItem(@PathVariable Long id,
            @ModelAttribute ClothingItem updated,
            @RequestParam(required = false) List<Long> tagIds,
            @RequestParam("imageFile") MultipartFile imageFile,
            HttpSession session) throws IOException {
        ClothingItem existing = wardrobeService.findById(id);
        if (!existing.getUser().getId().equals(getCurrentUser(session).getId())) {
            return "redirect:/wardrobe";
        }
        existing.setName(updated.getName());
        existing.setType(updated.getType());
        existing.setColor(updated.getColor());
        existing.setSecondaryColor(updated.getSecondaryColor());
        existing.setCondition(updated.getCondition());
        existing.setFavorite(updated.isFavorite());
        if (!imageFile.isEmpty()) {
            existing.setImageUrl(saveImage(imageFile));
        }
        if (tagIds != null) {
            existing.setTags(new HashSet<>(tagRepository.findAllById(tagIds)));
        } else {
            existing.getTags().clear();
        }
        wardrobeService.saveItem(existing);
        return "redirect:/wardrobe";
    }

    @GetMapping("/wardrobe/delete/{id}")
    public String deleteItem(@PathVariable Long id, HttpSession session) {
        ClothingItem item = wardrobeService.findById(id);
        if (item.getUser().getId().equals(getCurrentUser(session).getId())) {
            wardrobeService.deleteItem(id);
        }
        return "redirect:/wardrobe";
    }

    @GetMapping("/schedule")
    public String schedule(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        model.addAttribute("agendas", agendaService.getAllAgendas(user.getId()));
        return "schedule";
    }

    @PostMapping("/schedule/add")
    public String addAgenda(@ModelAttribute Agenda agenda, HttpSession session) {
        agenda.setUser(getCurrentUser(session));
        agendaService.saveAgenda(agenda);
        return "redirect:/schedule";
    }

    @PostMapping("/schedule/edit/{id}")
    public String editAgenda(@PathVariable Long id, @ModelAttribute Agenda updated, HttpSession session) {
        Agenda existing = agendaService.findById(id);
        if (!existing.getUser().getId().equals(getCurrentUser(session).getId())) {
            return "redirect:/schedule";
        }
        agendaService.updateAgenda(existing, updated);
        return "redirect:/schedule";
    }

    @GetMapping("/schedule/delete/{id}")
    public String deleteAgenda(@PathVariable Long id, HttpSession session) {
        Agenda agenda = agendaService.findById(id);
        if (agenda.getUser().getId().equals(getCurrentUser(session).getId())) {
            agendaService.deleteAgenda(id);
        }
        return "redirect:/schedule";
    }

    @GetMapping("/generate")
    public String generateForm(HttpSession session, Model model) {
        User user = getCurrentUser(session);
        model.addAttribute("agendas", agendaService.getAllAgendas(user.getId()));
        return "generate";
    }

    @PostMapping("/generate")
    public String generate(@RequestParam Long agendaId, HttpSession session, Model model) {
        User user = getCurrentUser(session);
        OutfitRecommendation rec = generateService.generate(agendaId, user);
        model.addAttribute("recommendation", rec);
        model.addAttribute("agendas", agendaService.getAllAgendas(user.getId()));
        return "generate";
    }

    private String saveImage(MultipartFile file) throws IOException {
        String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);
        Files.copy(file.getInputStream(), uploadPath.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + filename;
    }
}