package com.example.demo.Repository;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

        List<ClothingItem> findByUserOrderByIdDesc(User user);

        List<ClothingItem> findTop6ByUserOrderByIdDesc(User user);

        long countByUser(User user);

        long countByUserAndFavoriteTrue(User user);
}