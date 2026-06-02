package com.example.demo.Service;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Repository.AgendaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ScheduleService {

    private final AgendaRepository agendaRepository;

    public ScheduleService(AgendaRepository agendaRepository) {
        this.agendaRepository = agendaRepository;
    }

    public List<Schedule> findAllByUser(User user) {
        return agendaRepository.findByUserOrderByEventDateAsc(user);
    }

    public List<Schedule> findUpcomingByUser(User user) {
        return agendaRepository.findTop5ByUserAndEventDateGreaterThanEqualOrderByEventDateAsc(user, LocalDate.now());
    }

    public long countByUser(User user) {
        return agendaRepository.countByUser(user);
    }

    public long countUpcomingByUser(User user) {
        return agendaRepository.countByUserAndEventDateGreaterThanEqual(user, LocalDate.now());
    }

    public Schedule create(
            User user,
            String title,
            LocalDate eventDate,
            LocalTime startTime,
            LocalTime endTime,
            String dressCode,
            String eventType,
            String location) {
        Schedule agenda = new Schedule();
        agenda.setUser(user);
        agenda.setTitle(title);
        agenda.setEventDate(eventDate);
        agenda.setStartTime(startTime);
        agenda.setEndTime(endTime);
        agenda.setDressCode(dressCode);
        agenda.setEventType(eventType);
        agenda.setLocation(location);

        return agendaRepository.save(agenda);
    }

    public Schedule findOwnedAgenda(Long id, User user) {
        Schedule agenda = agendaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Agenda tidak ditemukan."));

        if (!agenda.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Kamu tidak punya akses ke agenda ini.");
        }

        return agenda;
    }

    public void delete(Long id, User user) {
        Schedule agenda = findOwnedAgenda(id, user);
        agendaRepository.delete(agenda);
    }
}