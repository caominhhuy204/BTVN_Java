package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.dtos.AdminDashboardData;
import com.btvn.CaoMinhHuy.dtos.CategoryCountDto;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.CategoryRepository;
import com.btvn.CaoMinhHuy.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public AdminDashboardData getOverview() {
        List<CategoryCountDto> breakdown = bookRepository.countByCategory();
        long maxCategoryCount = breakdown.stream()
                .mapToLong(CategoryCountDto::total)
                .max()
                .orElse(0);

        return new AdminDashboardData(
                bookRepository.count(),
                bookRepository.totalRevenue(),
                categoryRepository.count(),
                userRepository.count(),
                bookRepository.avgPrice(),
                bookRepository.maxPrice(),
                bookRepository.minPrice(),
                breakdown,
                bookRepository.findTop5ByOrderByIdDesc(),
                maxCategoryCount
        );
    }
}
