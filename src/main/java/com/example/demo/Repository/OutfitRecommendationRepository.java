package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.OutfitRecommendation;

public interface OutfitRecommendationRepository extends JpaRepository<OutfitRecommendation, Long> {
    List<OutfitRecommendation> findByUserIdOrderByGeneratedAtDesc(Long userId);
}