package com.btvn.CaoMinhHuy.services.impl;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.ReviewRepository;
import com.btvn.CaoMinhHuy.repositories.UserCartItemRepository;
import com.btvn.CaoMinhHuy.repositories.WishlistRepository;
import com.btvn.CaoMinhHuy.services.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    private final UserCartItemRepository userCartItemRepository;

    @Override
    public List<Book> findAll() {
        return bookRepository.findAll();
    }

    @Override
    public Book findById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách id=" + id));
    }

    @Override
    public Book save(Book book) {
        return bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            return;
        }
        // Xóa các tham chiếu để tránh lỗi FK
        reviewRepository.deleteByBook_Id(id);
        wishlistRepository.deleteByBook_Id(id);
        userCartItemRepository.deleteByBookId(id);
        bookRepository.deleteById(id);
    }

    @Override
    public Page<Book> search(String keyword, Long categoryId, Double minPrice, Double maxPrice, Pageable pageable) {
        String safeKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Double safeMin = minPrice;
        Double safeMax = maxPrice;
        if (safeMin != null && safeMax != null && safeMin > safeMax) {
            // Ensure min is not greater than max to avoid empty results
            double tmp = safeMin;
            safeMin = safeMax;
            safeMax = tmp;
        }

        return bookRepository.search(safeKeyword, categoryId, safeMin, safeMax, pageable);
    }

    @Override
    public List<Book> related(Long bookId, Long categoryId, int limit) {
        if (categoryId == null) {
            return bookRepository.findTop5ByOrderByIdDesc()
                    .stream()
                    .filter(b -> !b.getId().equals(bookId))
                    .limit(limit)
                    .toList();
        }
        return bookRepository.findTop5ByCategory_IdAndIdNotOrderByIdDesc(categoryId, bookId)
                .stream()
                .limit(limit)
                .toList();
    }

    @Override
    public List<Book> latest(int limit) {
        // Dùng pageable để lấy số lượng linh hoạt, fallback 5 nếu limit nhỏ hơn
        int size = Math.max(1, limit);
        return bookRepository.findAll(PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "id")))
                .getContent();
    }
}
