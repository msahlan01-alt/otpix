package com.example.demo.Repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

        List<Schedule> findByUserOrderByEventDateAsc(User user);

        List<Schedule> findByUserAndEventDateBetweenOrderByEventDateAsc(
                        User user, LocalDate start, LocalDate end);

        List<Schedule> findTop10ByUserAndEventDateGreaterThanEqualOrderByEventDateAsc(
                        User user, LocalDate from);
}