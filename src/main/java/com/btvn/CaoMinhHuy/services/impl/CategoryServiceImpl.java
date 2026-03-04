package com.btvn.CaoMinhHuy.services.impl;

import com.btvn.CaoMinhHuy.entities.Category;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.CategoryRepository;
import com.btvn.CaoMinhHuy.services.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    @Override
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục id=" + id));
    }

    @Override
    public Category save(Category category) {
        String trimmedName = category.getName() != null ? category.getName().trim() : null;
        if (trimmedName == null || trimmedName.isBlank()) {
            throw new IllegalArgumentException("Tên danh mục không được để trống");
        }
        category.setName(trimmedName);

        boolean duplicated = category.getId() == null
                ? categoryRepository.existsByNameIgnoreCase(trimmedName)
                : categoryRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, category.getId());
        if (duplicated) {
            throw new IllegalStateException("Tên danh mục đã tồn tại");
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void delete(Long id, Long targetCategoryId) {
        long relatedBooks = bookRepository.countByCategory_Id(id);
        if (relatedBooks > 0) {
            if (targetCategoryId == null) {
                throw new IllegalStateException("Danh mục còn " + relatedBooks + " sách. Chọn danh mục thay thế trước khi xóa.");
            }
            if (id.equals(targetCategoryId)) {
                throw new IllegalStateException("Danh mục thay thế phải khác danh mục cần xóa.");
            }
            Category target = categoryRepository.findById(targetCategoryId)
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục thay thế không tồn tại"));

            bookRepository.findByCategory_Id(id).forEach(b -> {
                b.setCategory(target);
                bookRepository.save(b);
            });
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public long countBooks(Long categoryId) {
        return bookRepository.countByCategory_Id(categoryId);
    }

    @Override
    public Map<Long, Long> bookCountByCategory() {
        return categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getId, c -> bookRepository.countByCategory_Id(c.getId())));
    }

    @Override
    @Transactional
    public int deleteEmptyCategories() {
        List<Category> all = categoryRepository.findAll();
        int removed = 0;
        for (Category c : all) {
            if (bookRepository.countByCategory_Id(c.getId()) == 0) {
                categoryRepository.delete(c);
                removed++;
            }
        }
        return removed;
    }
}
