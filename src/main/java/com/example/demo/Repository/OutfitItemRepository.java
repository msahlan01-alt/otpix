package com.example.demo.Repository;

import com.example.demo.Model.OutfitItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutfitItemRepository extends JpaRepository<OutfitItem, Long> {}