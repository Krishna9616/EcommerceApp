package com.EcommerceApp.repository;

import java.util.List;

import com.EcommerceApp.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Boolean existsByName(String name);

    List<Category> findByIsActiveTrue();
}
