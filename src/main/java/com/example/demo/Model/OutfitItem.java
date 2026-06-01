package com.example.demo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "outfit_items")
public class OutfitItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_recommendation_id", nullable = false)
    private OutfitRecommendation recommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clothing_item_id", nullable = false)
    private ClothingItem clothingItem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Slot slot;

    public enum Slot { atasan, bawahan, outer, sepatu, aksesoris }

    // getters, setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public OutfitRecommendation getRecommendation() { return recommendation; }
    public void setRecommendation(OutfitRecommendation recommendation) { this.recommendation = recommendation; }
    public ClothingItem getClothingItem() { return clothingItem; }
    public void setClothingItem(ClothingItem clothingItem) { this.clothingItem = clothingItem; }
    public Slot getSlot() { return slot; }
    public void setSlot(Slot slot) { this.slot = slot; }
}