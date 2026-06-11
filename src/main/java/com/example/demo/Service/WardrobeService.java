package com.example.demo.Service;

import com.example.demo.Dto.ClothingItemForm;
import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.Tag;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClothingItemRepository;
import com.example.demo.Repository.TagRepository;
import com.example.demo.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class WardrobeService {

    private final ClothingItemRepository clothingItemRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final GeminiService geminiService;

    private static final String UPLOAD_DIR = "uploads/clothing/";

    private static final List<String> VALID_CATEGORIES = List.of(
            "Atasan", "Bawahan", "Luaran", "Sepatu", "Aksesori", "Tas", "Lainnya");

    public WardrobeService(ClothingItemRepository clothingItemRepository,
            TagRepository tagRepository,
            UserRepository userRepository,
            GeminiService geminiService) {
        this.clothingItemRepository = clothingItemRepository;
        this.tagRepository = tagRepository;
        this.userRepository = userRepository;
        this.geminiService = geminiService;
    }

    // ===== READ =====

    public List<ClothingItem> getItemsByUser(String username) {
        User user = getUser(username);
        return clothingItemRepository.findByUser(user);
    }

    public List<ClothingItem> getFavoriteItems(String username) {
        User user = getUser(username);
        return clothingItemRepository.findByUserAndFavoriteTrue(user);
    }

    public List<ClothingItem> getItemsByCategory(String username, String category) {
        User user = getUser(username);
        return clothingItemRepository.findByUserAndCategory(user, category);
    }

    public ClothingItem getItemByIdAndUser(Long id, String username) {
        User user = getUser(username);
        return clothingItemRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan."));
    }

    // ===== CREATE =====

    @Transactional
    public ClothingItem addItem(ClothingItemForm form, String username) throws Exception {
        User user = getUser(username);

        if (!VALID_CATEGORIES.contains(form.getCategory())) {
            throw new RuntimeException("Kategori tidak valid.");
        }

        String imageUrl = saveImage(form.getImage());

        // Default metadata kalau Gemini gagal
        String color = "#000000";
        String secondaryColor = "#333333";
        String fit = "Regular";
        String conditionStatus = "Good";
        int formalityLevel = 2;
        Set<Tag> tags = new HashSet<>();

        try {
            GeminiService.ClothingMetadata metadata = geminiService.analyzeClothingImage(
                    form.getImage(), form.getName(), form.getCategory());
            color = metadata.color;
            secondaryColor = metadata.secondaryColor;
            fit = metadata.fit;
            conditionStatus = metadata.conditionStatus;
            formalityLevel = metadata.formalityLevel;
            tags = resolveTags(metadata.tags, user);
        } catch (Exception e) {
            // Gemini gagal → pakai default, tidak perlu crash
        }

        ClothingItem item = ClothingItem.builder()
                .user(user)
                .name(form.getName())
                .category(form.getCategory())
                .imageUrl(imageUrl)
                .favorite(form.isFavorite())
                .color(color)
                .secondaryColor(secondaryColor)
                .fit(fit)
                .conditionStatus(conditionStatus)
                .formalityLevel(formalityLevel)
                .tags(tags)
                .timesWorn(0)
                .build();

        return clothingItemRepository.save(item);
    }

    // ===== UPDATE =====

    @Transactional
    public ClothingItem updateItem(Long itemId, ClothingItemForm form, String username) throws Exception {
        User user = getUser(username);
        ClothingItem item = clothingItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan."));

        item.setName(form.getName());
        item.setCategory(form.getCategory());
        item.setFavorite(form.isFavorite());

        // Re-analyze dengan Gemini hanya kalau ada foto baru
        if (form.getImage() != null && !form.getImage().isEmpty()) {
            String imageUrl = saveImage(form.getImage());
            item.setImageUrl(imageUrl);

            try {
                GeminiService.ClothingMetadata metadata = geminiService.analyzeClothingImage(
                        form.getImage(), form.getName(), form.getCategory());
                item.setColor(metadata.color);
                item.setSecondaryColor(metadata.secondaryColor);
                item.setFit(metadata.fit);
                item.setConditionStatus(metadata.conditionStatus);
                item.setFormalityLevel(metadata.formalityLevel);
                item.setTags(resolveTags(metadata.tags, user));
            } catch (Exception e) {
                // Gemini gagal → biarkan metadata lama
            }
        }

        return clothingItemRepository.save(item);
    }

    // ===== DELETE =====

    @Transactional
    public void deleteItem(Long itemId, String username) {
        User user = getUser(username);
        ClothingItem item = clothingItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan."));
        clothingItemRepository.delete(item);
    }

    // ===== TOGGLE FAVORITE =====

    @Transactional
    public boolean toggleFavorite(Long itemId, String username) {
        User user = getUser(username);
        ClothingItem item = clothingItemRepository.findByIdAndUser(itemId, user)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan."));
        item.setFavorite(!item.isFavorite());
        clothingItemRepository.save(item);
        return item.isFavorite();
    }

    // ===== INCREMENT TIMES WORN (dipanggil dari GenerateService) =====

    @Transactional
    public void incrementTimesWorn(Long itemId) {
        clothingItemRepository.findById(itemId).ifPresent(item -> {
            item.incrementTimesWorn();
            clothingItemRepository.save(item);
        });
    }

    // Ambil 6 item terbaru berdasarkan createdAt
    public List<ClothingItem> getRecentItems(String username) {
        User user = getUser(username);
        return clothingItemRepository.findByUser(user)
                .stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null)
                        return 1;
                    if (b.getCreatedAt() == null)
                        return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(6)
                .toList();
    }

    public List<String> getValidCategories() {
        return VALID_CATEGORIES;
    }

    // ===== PRIVATE HELPERS =====

    private String saveImage(MultipartFile image) throws IOException {
        if (image == null || image.isEmpty()) {
            throw new RuntimeException("Foto pakaian wajib diupload.");
        }
        String extension = getExtension(image.getOriginalFilename());
        String fileName = UUID.randomUUID() + "." + extension;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath))
            Files.createDirectories(uploadPath);
        Files.copy(image.getInputStream(), uploadPath.resolve(fileName));
        return "/" + UPLOAD_DIR + fileName;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains("."))
            return "jpg";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }

    private Set<Tag> resolveTags(List<String> tagNames, User user) {
        Set<Tag> tags = new HashSet<>();
        for (String tagName : tagNames) {
            String normalized = tagName.toLowerCase().trim();
            Tag tag = tagRepository.findByUserAndNameIgnoreCase(user, normalized)
                    .orElseGet(() -> {
                        Tag newTag = new Tag(normalized, generateTagColor(normalized), user);
                        return tagRepository.save(newTag);
                    });
            tags.add(tag);
        }
        return tags;
    }

    private String generateTagColor(String tagName) {
        int hash = tagName.hashCode();
        int r = Math.max((hash & 0xFF0000) >> 16, 80);
        int g = Math.max((hash & 0x00FF00) >> 8, 80);
        int b = Math.max(hash & 0x0000FF, 80);
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan."));
    }
}