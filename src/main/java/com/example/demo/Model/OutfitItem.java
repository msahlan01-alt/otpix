package com.example.demo.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "outfit_items")
@Getter @Setter @NoArgsConstructor
public class OutfitItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outfit_recommendation_id", nullable = false)
    private OutfitRecommendation recommendation;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "clothing_item_id", nullable = false)
    private ClothingItem clothingItem;
    
    @Column(length = 30)
    private String role;
}