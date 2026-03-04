package com.btvn.CaoMinhHuy.repositories;

import com.btvn.CaoMinhHuy.entities.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUser_Id(Long userId);
    Optional<WishlistItem> findByUser_IdAndBook_Id(Long userId, Long bookId);
    void deleteByUser_IdAndBook_Id(Long userId, Long bookId);
    void deleteByBook_Id(Long bookId);
}
