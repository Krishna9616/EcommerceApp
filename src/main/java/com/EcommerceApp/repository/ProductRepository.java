package com.EcommerceApp.repository;

import java.util.List;

import com.EcommerceApp.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Integer> {


    List<Product> findByIsActiveTrue();

    Page<Product> findByIsActiveTrue(Pageable pageable);


    List<Product> findByCategory(String category);

    Page<Product> findByCategory(String category, Pageable pageable);


    List<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2);

    Page<Product> findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(String ch, String ch2, Pageable pageable);


    Page<Product> findByIsActiveTrueAndTitleContainingIgnoreCaseOrIsActiveTrueAndCategoryContainingIgnoreCase(
            String ch,
            String ch2,
            Pageable pageable
    );
}
