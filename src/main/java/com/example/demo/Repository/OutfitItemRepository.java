package com.example.demo.Repository;

import com.example.demo.Model.OutfitItem;
import com.example.demo.Model.OutfitRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OutfitItemRepository extends JpaRepository<OutfitItem, Long> {
    List<OutfitItem> findByRecommendationOrderByRole(OutfitRecommendation recommendation);
}