package com.example.demo.Repository;

import com.example.demo.Model.Tag;
import com.example.demo.Model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {

    List<Tag> findByUserOrderByNameAsc(User user);

    Optional<Tag> findByUserAndNameIgnoreCase(User user, String name);
}