package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Book;
import com.btvn.CaoMinhHuy.models.Cart;

public interface CartStorageService {
    Cart loadForUser(String username);
    void addItem(String username, Book book);
    void decreaseItem(String username, Long bookId);
    void clear(String username);
}
