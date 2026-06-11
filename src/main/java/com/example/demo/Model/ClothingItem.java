package com.example.demo.Model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clothing_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClothingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ===== Relasi =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "clothing_item_tags",
        joinColumns = @JoinColumn(name = "clothing_item_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    // ===== Info Dasar (diisi user) =====
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category; // Atasan, Bawahan, Luaran, Sepatu, Aksesori, Tas, Lainnya

    @Column(name = "image_url")
    private String imageUrl;

    // ===== Detail Visual (di-generate Gemini) =====
    @Builder.Default
    private String color = "#000000";

    @Column(name = "secondary_color")
    @Builder.Default
    private String secondaryColor = "#333333";

    // ===== Metadata Pakaian (di-generate Gemini) =====
    @Builder.Default
    private String fit = "Regular"; // Regular, Slim, Oversized, Loose

    @Column(name = "condition_status")
    @Builder.Default
    private String conditionStatus = "Good"; // Good, Fair, Poor

    @Column(name = "formality_level")
    @Builder.Default
    private int formalityLevel = 3; // 1 = sangat kasual, 5 = sangat formal

    // ===== Statistik =====
    @Column(name = "times_worn", nullable = false)
    @Builder.Default
    private int timesWorn = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean favorite = false;

    // ===== Timestamp =====
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ===== Lifecycle Hooks =====
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ===== Helper =====
    public void incrementTimesWorn() {
        this.timesWorn++;
    }
}