package com.btvn.CaoMinhHuy.services.impl;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.Review;
import com.btvn.CaoMinhHuy.entities.User;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.ReviewRepository;
import com.btvn.CaoMinhHuy.repositories.UserRepository;
import com.btvn.CaoMinhHuy.services.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Review addReview(Long bookId, String username, Integer rating, String comment) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));

        Review review = new Review();
        review.setBook(book);
        review.setRating(rating);
        review.setComment(comment);

        userRepository.findByUsername(username).ifPresent(review::setUser);
        review.setUsername(username);

        return reviewRepository.save(review);
    }

    @Override
    @Transactional
    public Review addReply(Long bookId, Long parentId, String username, Integer rating, String comment) {
        if (parentId == null) {
            throw new IllegalArgumentException("Thiếu thông tin bình luận gốc để trả lời");
        }
        Review parent = reviewRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bình luận để trả lời"));
        if (!parent.getBook().getId().equals(bookId)) {
            throw new IllegalArgumentException("Bình luận gốc không thuộc sách này");
        }
        if (parent.getParent() != null) {
            throw new IllegalArgumentException("Chỉ cho phép trả lời tối đa 1 cấp");
        }
        return addReviewWithParent(bookId, parent, username, rating, comment);
    }

    private Review addReviewWithParent(Long bookId, Review parent, String username, Integer rating, String comment) {
        if (rating != null && (rating < 1 || rating > 5)) {
            throw new IllegalArgumentException("Điểm đánh giá phải từ 1 đến 5");
        }
        Review review = new Review();
        review.setBook(parent.getBook());
        review.setParent(parent);
        review.setRating(rating);
        review.setComment(comment);

        userRepository.findByUsername(username).ifPresent(review::setUser);
        review.setUsername(username);

        return reviewRepository.save(review);
    }

    @Override
    public List<Review> listByBook(Long bookId) {
        // Lấy root reviews, sau đó gắn replies (1 cấp) để render dạng thread đơn giản
        List<Review> roots = reviewRepository.findByBook_IdAndParentIsNullOrderByCreatedAtDesc(bookId);
        for (Review r : roots) {
            List<Review> replies = reviewRepository.findByParent_IdOrderByCreatedAtAsc(r.getId());
            r.setReplies(replies);
        }
        return roots;
    }

    @Override
    public double averageRating(Long bookId) {
        Double avg = reviewRepository.averageRating(bookId);
        return avg == null ? 0 : avg;
    }
}
