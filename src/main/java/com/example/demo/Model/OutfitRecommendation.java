package com.example.demo.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "outfit_recommendations")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = { "agenda", "outfitItems" })
public class OutfitRecommendation {
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "user_id", nullable = false)
private User user;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agenda_id", nullable = false, unique = true)
    private Schedule agenda;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt = LocalDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "recommendation", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OutfitItem> outfitItems = new ArrayList<>();

    public void addOutfitItem(ClothingItem item) {
        OutfitItem outfitItem = new OutfitItem();
        outfitItem.setClothingItem(item);
        outfitItem.setRecommendation(this);
        outfitItems.add(outfitItem);
    }

    public void save() {
        for (OutfitItem outfitItem : outfitItems) {
            outfitItem.setRecommendation(this);
        }
    }
}