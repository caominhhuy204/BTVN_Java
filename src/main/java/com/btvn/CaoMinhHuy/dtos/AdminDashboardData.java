package com.btvn.CaoMinhHuy.dtos;

import com.btvn.CaoMinhHuy.entities.Book;

import java.util.List;

public record AdminDashboardData(
        long totalBooks,
        Double totalRevenue,
        long totalCategories,
        long totalUsers,
        Double averagePrice,
        Double maxPrice,
        Double minPrice,
        List<CategoryCountDto> categoryBreakdown,
        List<Book> latestBooks,
        long maxCategoryCount
) {}
