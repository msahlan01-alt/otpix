package com.example.demo.Repository;

import com.example.demo.Model.Schedule;
import com.example.demo.Model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgendaRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByUserOrderByEventDateAsc(User user);

    List<Schedule> findTop5ByUserAndEventDateGreaterThanEqualOrderByEventDateAsc(User user, LocalDate date);

    long countByUser(User user);

    long countByUserAndEventDateGreaterThanEqual(User user, LocalDate date);
}