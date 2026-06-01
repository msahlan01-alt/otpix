package com.example.demo.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.Model.Agenda;
import com.example.demo.Repository.AgendaRepository;

@Service
@Transactional
public class AgendaService {
    private final AgendaRepository agendaRepository;

    public AgendaService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public List<Agenda> getAllAgendas(Long userId) {
        return agendaRepository.findByUserIdOrderByEventDateAsc(userId);
    }

    public Agenda findById(Long id) {
        return agendaRepository.findById(id).orElseThrow(() -> new RuntimeException("Agenda not found"));
    }

    public void saveAgenda(Agenda agenda) {
        agendaRepository.save(agenda);
    }

    public void deleteAgenda(Long id) {
        agendaRepository.deleteById(id);
    }

    public void updateAgenda(Agenda existing, Agenda updated) {
        existing.setTitle(updated.getTitle());
        existing.setEventDate(updated.getEventDate());
        existing.setStartTime(updated.getStartTime());
        existing.setEndTime(updated.getEndTime());
        existing.setEventType(updated.getEventType());
        existing.setDressCode(updated.getDressCode());
        existing.setLocationType(updated.getLocationType());
        agendaRepository.save(existing);
    }
}