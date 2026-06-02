package com.example.demo.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.OutfitItem;
import com.example.demo.Model.OutfitRecommendation;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClothingItemRepository;
import com.example.demo.Repository.OutfitRecommendationRepository;

@Service
public class GenerateService {

    private final ClothingItemRepository itemRepo;
    private final OutfitRecommendationRepository recRepo;

    public GenerateService(ClothingItemRepository itemRepo,
            OutfitRecommendationRepository recRepo) {
        this.itemRepo = itemRepo;
        this.recRepo = recRepo;
    }

    public Optional<OutfitRecommendation> getByAgendaId(Long agendaId) {
        return recRepo.findByAgenda_Id(agendaId);
    }

    @Transactional
    public OutfitRecommendation generate(Schedule agenda, User user) {

        recRepo.findByAgenda_Id(agenda.getId()).ifPresent(recRepo::delete);

        int[] fRange = formalityRange(agenda.getEventType(), agenda.getDressCode());
        int minF = fRange[0], maxF = fRange[1];

        OutfitRecommendation rec = new OutfitRecommendation();
        rec.setAgenda(agenda);
        rec.setGeneratedAt(LocalDateTime.now());

        List<OutfitItem> picks = new ArrayList<>();
        Set<Long> usedIds = new HashSet<>();

        boolean chooseDress = maxF >= 4 && random50();
        if (chooseDress) {
            pick(user, "DRESSES", minF, maxF, usedIds)
                    .ifPresent(ci -> picks.add(buildOutfitItem(rec, ci, "DRESS")));
        } else {
            pick(user, "TOPS", minF, maxF, usedIds)
                    .ifPresent(ci -> picks.add(buildOutfitItem(rec, ci, "TOP")));
            pick(user, "BOTTOMS", minF, maxF, usedIds)
                    .ifPresent(ci -> picks.add(buildOutfitItem(rec, ci, "BOTTOM")));
        }

        boolean needOuter = "Outdoor".equalsIgnoreCase(agenda.getLocation()) || minF >= 4;
        if (needOuter || random50()) {
            pick(user, "OUTERWEAR", minF, maxF, usedIds)
                    .ifPresent(ci -> picks.add(buildOutfitItem(rec, ci, "OUTERWEAR")));
        }

        pick(user, "SHOES", minF, maxF, usedIds)
                .ifPresent(ci -> picks.add(buildOutfitItem(rec, ci, "SHOES")));

        pick(user, "ACCESSORIES", minF, maxF, usedIds)
                .ifPresent(ci -> picks.add(buildOutfitItem(rec, ci, "ACCESSORIES")));

        rec.setNotes(buildNote(agenda, picks.size()));
        rec.setOutfitItems(picks);

        OutfitRecommendation saved = recRepo.save(rec);

        picks.forEach(oi -> {
            ClothingItem ci = oi.getClothingItem();
            ci.setFavorite(true);
            itemRepo.save(ci);
        });

        return saved;
    }

    private Optional<ClothingItem> pick(User user, String catName,
            int minF, int maxF,
            Set<Long> used) {
        List<ClothingItem> candidates = itemRepo.findByUserOrderByIdDesc(user)
                .stream()
                .filter(ci -> ci.getCategory() != null && ci.getCategory().equalsIgnoreCase(catName))
                .filter(ci -> ci.getConditionStatus() != null)
                .filter(ci -> {
                    int f = switch (ci.getConditionStatus().toUpperCase()) {
                        case "SPORTY" -> 1;
                        case "STREET STYLE",
                                "CASUAL" ->
                            2;
                        case "SMART CASUAL",
                                "BUSINESS CASUAL" ->
                            3;
                        case "FORMAL" -> 4;
                        case "BLACK TIE" -> 5;
                        default -> 3;
                    };
                    return f >= minF && f <= maxF;
                })
                .toList();

        if (candidates.isEmpty()) {
            candidates = itemRepo.findByUserOrderByIdDesc(user)
                    .stream()
                    .filter(ci -> ci.getCategory() != null && ci.getCategory().equalsIgnoreCase(catName))
                    .toList();
        }

        return candidates.stream()
                .filter(ci -> !used.contains(ci.getId()))
                .findFirst()
                .map(ci -> {
                    used.add(ci.getId());
                    return ci;
                });
    }

    private OutfitItem buildOutfitItem(OutfitRecommendation rec, ClothingItem ci, String role) {
        OutfitItem oi = new OutfitItem();
        oi.setRecommendation(rec);
        oi.setClothingItem(ci);
        oi.setRole(role);
        return oi;
    }

    private int[] formalityRange(String eventType, String dressCode) {
        if (dressCode != null && !dressCode.isBlank()) {
            return switch (dressCode) {
                case "Sporty" -> new int[] { 1, 2 };
                case "Street Style",
                        "Casual" ->
                    new int[] { 2, 3 };
                case "Smart Casual",
                        "Business Casual" ->
                    new int[] { 3, 4 };
                case "Formal" -> new int[] { 4, 5 };
                case "Black Tie" -> new int[] { 5, 5 };
                default -> formalityFromEvent(eventType);
            };
        }
        return formalityFromEvent(eventType);
    }

    private int[] formalityFromEvent(String eventType) {
        if (eventType == null)
            return new int[] { 2, 4 };
        return switch (eventType.toUpperCase()) {
            case "SPORT" -> new int[] { 1, 2 };
            case "CASUAL" -> new int[] { 2, 3 };
            case "OUTDOOR" -> new int[] { 2, 3 };
            case "DATE" -> new int[] { 3, 4 };
            case "PARTY" -> new int[] { 3, 5 };
            case "BUSINESS" -> new int[] { 4, 5 };
            case "FORMAL" -> new int[] { 5, 5 };
            default -> new int[] { 2, 4 };
        };
    }

    private String buildNote(Schedule agenda, int count) {
        return String.format(
                "Outfit dihasilkan untuk '%s' (%s) tanggal %s. " +
                        "Dress code: %s | Lokasi: %s | %d item dipilih.",
                agenda.getTitle(), agenda.getEventType(),
                agenda.getEventDate(), agenda.getDressCode(),
                agenda.getLocation(), count);
    }

    private boolean random50() {
        return Math.random() > 0.5;
    }
}