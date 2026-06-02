package com.example.demo.Repository;

import com.example.demo.Model.OutfitRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OutfitRecommendationRepository
        extends JpaRepository<OutfitRecommendation, Long> {

    Optional<OutfitRecommendation> findByAgenda_Id(Long agendaId);
    void deleteByAgenda_Id(Long agendaId);
}