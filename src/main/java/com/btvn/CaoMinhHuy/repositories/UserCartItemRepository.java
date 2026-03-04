package com.btvn.CaoMinhHuy.repositories;

import com.btvn.CaoMinhHuy.entities.User;
import com.btvn.CaoMinhHuy.entities.UserCartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserCartItemRepository extends JpaRepository<UserCartItem, Long> {
    List<UserCartItem> findByUser_Username(String username);
    Optional<UserCartItem> findByUserAndBookId(User user, Long bookId);
    void deleteByUser_Username(String username);
    void deleteByBookId(Long bookId);
}
