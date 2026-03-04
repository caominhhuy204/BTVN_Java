package com.btvn.CaoMinhHuy.services;

import com.btvn.CaoMinhHuy.entities.Order;
import com.btvn.CaoMinhHuy.models.Cart;

import java.util.List;

public interface OrderService {
    Order createFromCart(Cart cart,
                         String username,
                         String shippingName,
                         String shippingPhone,
                         String shippingAddress,
                         String paymentMethod,
                         Double shippingFee,
                         Double discount,
                         String couponCode);

    Order updateStatus(Long id, String status, String adminNote);
    List<Order> findForUser(String username);
    List<Order> findAll();
    Order findById(Long id);
    Order findByIdForUser(Long id, String username);
}
