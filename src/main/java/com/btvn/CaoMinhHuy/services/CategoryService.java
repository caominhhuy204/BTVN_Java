package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Category;

import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    List<Category> findAll();
    Page<Category> findAll(Pageable pageable);
    Category findById(Long id);
    Category save(Category category);
    void delete(Long id, Long targetCategoryId);
    long countBooks(Long categoryId);
    Map<Long, Long> bookCountByCategory();
    int deleteEmptyCategories();
}
