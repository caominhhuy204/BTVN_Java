package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.WishlistItem;

import java.util.List;

public interface WishlistService {
    WishlistItem toggle(Long bookId, String username);
    List<WishlistItem> list(String username);
    boolean exists(Long bookId, String username);
}
