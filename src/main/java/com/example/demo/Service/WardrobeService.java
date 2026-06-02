package com.example.demo.Service;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;
import com.example.demo.Repository.ClothingItemRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class WardrobeService {

    private final ClothingItemRepository clothingItemRepository;

    public WardrobeService(ClothingItemRepository clothingItemRepository) {
        this.clothingItemRepository = clothingItemRepository;
    }

    public List<ClothingItem> findAllByUser(User user) {
        return clothingItemRepository.findByUserOrderByIdDesc(user);
    }

    public List<ClothingItem> findRecentByUser(User user) {
        return clothingItemRepository.findTop6ByUserOrderByIdDesc(user);
    }

    public long countByUser(User user) {
        return clothingItemRepository.countByUser(user);
    }

    public long countFavoriteByUser(User user) {
        return clothingItemRepository.countByUserAndFavoriteTrue(user);
    }

    public ClothingItem create(
            User user,
            String name,
            String category,
            String color,
            String conditionStatus,
            String imageUrl) {
        ClothingItem item = new ClothingItem();
        item.setUser(user);
        item.setName(name);
        item.setCategory(category);
        item.setColor(color);
        item.setConditionStatus(conditionStatus);
        item.setImageUrl(imageUrl);
        item.setFavorite(false);

        return clothingItemRepository.save(item);
    }

    public ClothingItem findOwnedItem(Long id, User user) {
        ClothingItem item = clothingItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item tidak ditemukan."));

        if (!item.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Kamu tidak punya akses ke item ini.");
        }

        return item;
    }

    public void toggleFavorite(Long id, User user) {
        ClothingItem item = findOwnedItem(id, user);
        item.setFavorite(!item.isFavorite());
        clothingItemRepository.save(item);
    }

    public void delete(Long id, User user) {
        ClothingItem item = findOwnedItem(id, user);
        clothingItemRepository.delete(item);
    }
}
