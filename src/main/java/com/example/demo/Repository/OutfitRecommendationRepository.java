package com.example.demo.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.OutfitRecommendation;

public interface OutfitRecommendationRepository
        extends JpaRepository<OutfitRecommendation, Long> {

    Optional<OutfitRecommendation> findByAgenda_Id(Long agendaId);
    void deleteByAgenda_Id(Long agendaId);

}