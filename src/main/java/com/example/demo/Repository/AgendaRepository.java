package com.example.demo.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Agenda;

public interface AgendaRepository extends JpaRepository<Agenda, Long> {
    List<Agenda> findByUserIdOrderByEventDateAsc(Long userId);
}