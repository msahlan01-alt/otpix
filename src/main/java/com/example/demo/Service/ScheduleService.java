package com.example.demo.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Repository.ScheduleRepository;
import com.example.demo.Repository.UserRepository;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    public ScheduleService(ScheduleRepository scheduleRepository,
            UserRepository userRepository) {
        this.scheduleRepository = scheduleRepository;
        this.userRepository = userRepository;
    }

    // ===== READ =====

    public List<Schedule> getSchedulesByUser(String username) {
        User user = getUser(username);
        return scheduleRepository.findByUserOrderByEventDateAsc(user);
    }

    public Schedule getScheduleByIdAndUser(Long id, String username) {
        User user = getUser(username);
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event tidak ditemukan."));
        if (!s.getUser().getId().equals(user.getId()))
            throw new RuntimeException("Akses ditolak.");
        return s;
    }

    public List<Schedule> getSchedulesByMonth(String username, int month, int year) {
        User user = getUser(username);
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return scheduleRepository.findByUserAndEventDateBetweenOrderByEventDateAsc(user, start, end);
    }

    public List<Schedule> getUpcomingSchedules(String username) {
        User user = getUser(username);
        return scheduleRepository.findTop10ByUserAndEventDateGreaterThanEqualOrderByEventDateAsc(
                user, LocalDate.now());
    }

    // ===== CREATE =====

    public Schedule create(String username, String title, LocalDate eventDate,
            java.time.LocalTime startTime, java.time.LocalTime endTime,
            String dressCode, String eventType, String location) {
        User user = getUser(username);
        Schedule s = new Schedule();
        s.setUser(user);
        s.setTitle(title);
        s.setEventDate(eventDate);
        s.setStartTime(startTime);
        s.setEndTime(endTime);
        s.setDressCode(dressCode);
        s.setEventType(eventType);
        s.setLocation(location);
        return scheduleRepository.save(s);
    }

    // ===== DELETE =====

    public void delete(Long id, String username) {
        Schedule s = getScheduleByIdAndUser(id, username);
        scheduleRepository.delete(s);
    }

    // ===== PRIVATE =====

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan."));
    }
}