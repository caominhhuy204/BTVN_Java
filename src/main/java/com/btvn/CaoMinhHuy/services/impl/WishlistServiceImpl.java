package com.btvn.CaoMinhHuy.services.impl;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.User;
import com.btvn.CaoMinhHuy.entities.WishlistItem;
import com.btvn.CaoMinhHuy.repositories.BookRepository;
import com.btvn.CaoMinhHuy.repositories.UserRepository;
import com.btvn.CaoMinhHuy.repositories.WishlistRepository;
import com.btvn.CaoMinhHuy.services.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WishlistServiceImpl implements WishlistService {
    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public WishlistItem toggle(Long bookId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách"));

        var existing = wishlistRepository.findByUser_IdAndBook_Id(user.getId(), bookId);
        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());
            return null;
        }

        WishlistItem item = new WishlistItem();
        item.setUser(user);
        item.setBook(book);
        try {
            return wishlistRepository.save(item);
        } catch (DataIntegrityViolationException ex) {
            // Nếu có request song song tạo trùng, trả lại bản ghi hiện hữu thay vì ném lỗi
            return wishlistRepository.findByUser_IdAndBook_Id(user.getId(), bookId).orElseThrow(() -> ex);
        }
    }

    @Override
    public List<WishlistItem> list(String username) {
        return userRepository.findByUsername(username)
                .map(u -> wishlistRepository.findByUser_Id(u.getId()))
                .orElse(List.of());
    }

    @Override
    public boolean exists(Long bookId, String username) {
        return userRepository.findByUsername(username)
                .map(u -> wishlistRepository.findByUser_IdAndBook_Id(u.getId(), bookId).isPresent())
                .orElse(false);
    }
}
