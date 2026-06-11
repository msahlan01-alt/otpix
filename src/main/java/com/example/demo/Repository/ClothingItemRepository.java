package com.example.demo.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.Model.ClothingItem;
import com.example.demo.Model.User;

public interface ClothingItemRepository extends JpaRepository<ClothingItem, Long> {

        List<ClothingItem> findByUser(User user);

        List<ClothingItem> findByUserAndFavoriteTrue(User user);

        List<ClothingItem> findByUserAndCategory(User user, String category);

        Optional<ClothingItem> findByIdAndUser(Long id, User user);

        List<ClothingItem> findByUserAndFormalityLevelBetween(User user, int min, int max);

        List<ClothingItem> findByUserOrderByTimesWornDesc(User user);

        @Query("SELECT c FROM ClothingItem c JOIN c.tags t WHERE c.user = :user AND t.name = :tagName")
        List<ClothingItem> findByUserAndTagName(@Param("user") User user, @Param("tagName") String tagName);
}