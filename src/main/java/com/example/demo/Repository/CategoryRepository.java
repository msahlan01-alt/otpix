package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.Model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {}