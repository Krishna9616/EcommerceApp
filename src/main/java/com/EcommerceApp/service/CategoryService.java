package com.EcommerceApp.service;

import java.util.List;

import com.EcommerceApp.model.Category;
import org.springframework.data.domain.Page;

public interface CategoryService {

    Category saveCategory(Category category);

    Boolean existCategory(String name);

    List<Category> getAllCategory();

    Boolean deleteCategory(int id);

    Category getCategoryById(int id);

    List<Category> getAllActiveCategory();

    Page<Category> getAllCategoryPagination(Integer pageNo, Integer pageSize);


    // Boolean updateCategoryStatus(Integer id, Boolean status);
}
