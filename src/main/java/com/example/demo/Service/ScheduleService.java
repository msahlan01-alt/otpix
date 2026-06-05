package com.example.demo.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import com.example.demo.Repository.ScheduleRepository;

@Service
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;

    public ScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }

    public List<Schedule> findAllByUser(User user) {
        return scheduleRepository.findByUserOrderByEventDateAsc(user);
    }

    public void create(User user, String title, LocalDate eventDate,
            java.time.LocalTime startTime, java.time.LocalTime endTime,
            String dressCode, String eventType, String location) {
        Schedule s = new Schedule();
        s.setUser(user);
        s.setTitle(title);
        s.setEventDate(eventDate);
        s.setStartTime(startTime);
        s.setEndTime(endTime);
        s.setDressCode(dressCode);
        s.setEventType(eventType);
        s.setLocation(location);
        scheduleRepository.save(s);
    }

    public Schedule findOwnedAgenda(Long id, User user) {
        Schedule s = scheduleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Event tidak ditemukan."));
        if (!s.getUser().getId().equals(user.getId()))
            throw new SecurityException("Akses ditolak.");
        return s;
    }

    public void delete(Long id, User user) {
        Schedule s = findOwnedAgenda(id, user);
        scheduleRepository.delete(s);
    }

    public List<Schedule> findByUserAndMonth(User user, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return scheduleRepository.findByUserAndEventDateBetweenOrderByEventDateAsc(user, start, end);
    }

    /** Event mendatang dari hari ini ke depan (max 10) */
    public List<Schedule> findUpcomingByUser(User user) {
        return scheduleRepository.findTop10ByUserAndEventDateGreaterThanEqualOrderByEventDateAsc(
                user, LocalDate.now());
    }
}