package com.example.demo.Service;

import com.example.demo.Model.*;
import com.example.demo.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GenerateService {
    private final ClothingItemRepository clothingItemRepository;
    private final AgendaRepository agendaRepository;
    private final OutfitRecommendationRepository recRepository;
    private final OutfitItemRepository outfitItemRepository;

    public GenerateService(ClothingItemRepository clothingItemRepository,
            AgendaRepository agendaRepository,
            OutfitRecommendationRepository recRepository,
            OutfitItemRepository outfitItemRepository) {
        this.clothingItemRepository = clothingItemRepository;
        this.agendaRepository = agendaRepository;
        this.recRepository = recRepository;
        this.outfitItemRepository = outfitItemRepository;
    }

    public OutfitRecommendation generate(Long agendaId, User user) {
        Agenda agenda = agendaRepository.findById(agendaId)
                .orElseThrow(() -> new RuntimeException("Agenda not found"));
        List<ClothingItem> allItems = clothingItemRepository.findByUserId(user.getId());

        // Filter: hanya item yang tidak rusak
        List<ClothingItem> available = allItems.stream()
                .filter(i -> i.getCondition() != ClothingItem.Condition.rusak)
                .collect(Collectors.toList());

        // Filter dress code (jika ada)
        if (agenda.getDressCode() != null && !agenda.getDressCode().isBlank()) {
            String tagName = agenda.getDressCode().trim().toLowerCase();
            available = available.stream()
                    .filter(i -> i.getTags().stream().anyMatch(t -> t.getName().equalsIgnoreCase(tagName)))
                    .collect(Collectors.toList());
        }

        // Tidak ada filter formality level karena field sudah dihapus

        String[] slots = { "atasan", "bawahan", "outer", "sepatu", "aksesoris" };
        Map<String, ClothingItem> chosen = new LinkedHashMap<>();

        for (String slot : slots) {
            List<ClothingItem> slotItems = available.stream()
                    .filter(i -> i.getType().name().equalsIgnoreCase(slot))
                    .sorted(Comparator.comparingInt(ClothingItem::getTimesWorn)
                            .thenComparing((a, b) -> {
                                if (a.getLastWorn() == null)
                                    return -1;
                                if (b.getLastWorn() == null)
                                    return 1;
                                return a.getLastWorn().compareTo(b.getLastWorn());
                            })
                            .thenComparing((a, b) -> Boolean.compare(b.isFavorite(), a.isFavorite())))
                    .collect(Collectors.toList());

            if (!slotItems.isEmpty()) {
                ClothingItem selected = slotItems.get(0);
                chosen.put(slot, selected);
                available.remove(selected);
            }
        }

        OutfitRecommendation rec = new OutfitRecommendation();
        rec.setUser(user);
        rec.setAgenda(agenda);
        rec = recRepository.save(rec);

        for (Map.Entry<String, ClothingItem> entry : chosen.entrySet()) {
            OutfitItem oi = new OutfitItem();
            oi.setRecommendation(rec);
            oi.setClothingItem(entry.getValue());
            oi.setSlot(OutfitItem.Slot.valueOf(entry.getKey()));
            outfitItemRepository.save(oi);

            ClothingItem item = entry.getValue();
            item.setTimesWorn(item.getTimesWorn() + 1);
            item.setLastWorn(LocalDate.now());
            clothingItemRepository.save(item);
        }

        return rec;
    }
}