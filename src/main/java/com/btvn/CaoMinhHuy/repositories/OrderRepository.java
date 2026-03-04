package com.btvn.CaoMinhHuy.repositories;

import com.btvn.CaoMinhHuy.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser_UsernameOrderByCreatedAtDesc(String username);
    List<Order> findAllByOrderByCreatedAtDesc();
}
