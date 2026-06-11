package com.example.demo.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.OutfitItem;
import com.example.demo.Model.OutfitRecommendation;
import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClothingItemRepository;
import com.example.demo.Repository.OutfitItemRepository;
import com.example.demo.Repository.OutfitRecommendationRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Service
public class GenerateService {

    private final ClothingItemRepository itemRepo;
    private final OutfitRecommendationRepository recRepo;
    private final OutfitItemRepository outfitItemRepo;
    private final GeminiService geminiService;

    @PersistenceContext
    private EntityManager entityManager;

    public GenerateService(ClothingItemRepository itemRepo,
            OutfitRecommendationRepository recRepo,
            OutfitItemRepository outfitItemRepo,
            GeminiService geminiService) {
        this.itemRepo = itemRepo;
        this.recRepo = recRepo;
        this.outfitItemRepo = outfitItemRepo;
        this.geminiService = geminiService;
    }

    public Optional<OutfitRecommendation> getByAgendaId(Long agendaId) {
        return recRepo.findByAgenda_Id(agendaId);
    }

    @Transactional
    public OutfitRecommendation generate(Schedule schedule, User user) throws Exception {
        // Hapus rekomendasi lama kalau ada
        recRepo.findByAgenda_Id(schedule.getId()).ifPresent(recRepo::delete);

        // Ambil semua item milik user
        List<ClothingItem> allItems = itemRepo.findByUser(user);

        if (allItems.isEmpty()) {
            throw new RuntimeException("Wardrobe kamu masih kosong. Tambahkan item dulu.");
        }

        // Buat summary untuk dikirim ke Gemini (tidak perlu kirim semua field)
        List<GeminiService.ClothingItemSummary> summaries = allItems.stream()
                .map(item -> new GeminiService.ClothingItemSummary(
                        item.getId(),
                        item.getName(),
                        item.getCategory(),
                        item.getFormalityLevel(),
                        item.getTags().stream()
                                .map(tag -> tag.getName())
                                .collect(Collectors.toList())))
                .collect(Collectors.toList());

        // Panggil Gemini
        GeminiService.OutfitResult result = geminiService.generateOutfit(schedule, summaries);

        // Bangun OutfitRecommendation
        OutfitRecommendation rec = new OutfitRecommendation();
        rec.setUser(user);
        rec.setAgenda(schedule);
        rec.setGeneratedAt(LocalDateTime.now());
        rec.setNotes(buildNotes(result, schedule));

        OutfitRecommendation saved = recRepo.save(rec);

        // Resolve item dari ID yang dikembalikan Gemini
        for (Long itemId : result.itemIds) {
            itemRepo.findByIdAndUser(itemId, user).ifPresent(item -> {
                // Simpan OutfitItem
                OutfitItem outfitItem = new OutfitItem();
                outfitItem.setRecommendation(saved);
                outfitItem.setClothingItem(item);
                outfitItem.setRole(item.getCategory());
                outfitItemRepo.save(outfitItem);

                // Increment timesWorn
                item.incrementTimesWorn();
                itemRepo.save(item);
            });
        }

        return saved;
    }

    private String buildNotes(GeminiService.OutfitResult result, Schedule schedule) {
        return String.format(
                "%s\n\nAlasan: %s\n\nTips: %s\n\n(Dibuat untuk agenda '%s' pada %s)",
                result.outfitName,
                result.reason,
                result.tips,
                schedule.getTitle(),
                schedule.getEventDate());
    }
}