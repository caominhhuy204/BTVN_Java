package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Review;

import java.util.List;

public interface ReviewService {
    Review addReview(Long bookId, String username, Integer rating, String comment);
    Review addReply(Long bookId, Long parentId, String username, Integer rating, String comment);
    List<Review> listByBook(Long bookId);
    double averageRating(Long bookId);
}
