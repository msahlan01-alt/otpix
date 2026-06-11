package com.example.demo.Repository;

import com.example.demo.Model.OutfitItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutfitItemRepository extends JpaRepository<OutfitItem, Long> {
    List<OutfitItem> findByRecommendation_Id(Long recommendationId);
}