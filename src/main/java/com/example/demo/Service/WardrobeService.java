package com.example.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Repository.ClothingItemRepository;

@Service
@Transactional
public class WardrobeService {
    private final ClothingItemRepository clothingItemRepository;

    public WardrobeService(ClothingItemRepository clothingItemRepository) {
        this.clothingItemRepository = clothingItemRepository;
    }

    public List<ClothingItem> getAllItems(Long userId) {
        return clothingItemRepository.findByUserId(userId);
    }

    public ClothingItem findById(Long id) {
        return clothingItemRepository.findById(id).orElseThrow(() -> new RuntimeException("Item not found"));
    }

    public void saveItem(ClothingItem item) {
        clothingItemRepository.save(item);
    }

    public void deleteItem(Long id) {
        clothingItemRepository.deleteById(id);
    }
}