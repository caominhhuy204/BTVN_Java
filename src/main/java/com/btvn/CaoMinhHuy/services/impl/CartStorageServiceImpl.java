package com.btvn.CaoMinhHuy.services.impl;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.entities.User;
import com.btvn.CaoMinhHuy.entities.UserCartItem;
import com.btvn.CaoMinhHuy.models.Cart;
import com.btvn.CaoMinhHuy.models.CartItem;
import com.btvn.CaoMinhHuy.repositories.UserCartItemRepository;
import com.btvn.CaoMinhHuy.repositories.UserRepository;
import com.btvn.CaoMinhHuy.services.CartStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartStorageServiceImpl implements CartStorageService {

    private final UserCartItemRepository userCartItemRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Cart loadForUser(String username) {
        Cart cart = new Cart();
        if (username == null || username.isBlank()) return cart;

        var items = userCartItemRepository.findByUser_Username(username);
        cart.setItems(items.stream().collect(Collectors.toMap(
                UserCartItem::getBookId,
                it -> new CartItem(it.getBookId(), it.getTitle(), it.getPrice(), it.getQuantity()),
                (a, b) -> a,
                java.util.LinkedHashMap::new
        )));
        return cart;
    }

    @Override
    @Transactional
    public void addItem(String username, Book book) {
        if (username == null || username.isBlank()) return;
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user " + username));

        userCartItemRepository.findByUserAndBookId(user, book.getId())
                .ifPresentOrElse(it -> {
                    it.setQuantity(it.getQuantity() + 1);
                    userCartItemRepository.save(it);
                }, () -> {
                    UserCartItem it = new UserCartItem();
                    it.setUser(user);
                    it.setBookId(book.getId());
                    it.setTitle(book.getTitle());
                    it.setPrice(book.getPrice());
                    it.setQuantity(1);
                    userCartItemRepository.save(it);
                });
    }

    @Override
    @Transactional
    public void decreaseItem(String username, Long bookId) {
        if (username == null || username.isBlank()) return;
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user " + username));

        userCartItemRepository.findByUserAndBookId(user, bookId)
                .ifPresent(it -> {
                    int next = it.getQuantity() - 1;
                    if (next <= 0) {
                        userCartItemRepository.delete(it);
                    } else {
                        it.setQuantity(next);
                        userCartItemRepository.save(it);
                    }
                });
    }

    @Override
    @Transactional
    public void clear(String username) {
        if (username == null || username.isBlank()) return;
        userCartItemRepository.deleteByUser_Username(username);
    }
}
