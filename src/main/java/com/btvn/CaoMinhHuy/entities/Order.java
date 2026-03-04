package com.btvn.CaoMinhHuy.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Thông tin giao hàng/thanh toán
    private String shippingName;

    @Column(columnDefinition = "TEXT")
    private String shippingAddress;

    private String shippingPhone;
    private String paymentMethod; // COD, BANK_TRANSFER, ...
    private Double shippingFee = 0.0;
    private Double discount = 0.0;
    private String couponCode;

    private Double totalAmount;

    private String status;
    private String adminNote;
    private LocalDateTime statusUpdatedAt;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = "CREATED";
        }
        if (statusUpdatedAt == null) {
            statusUpdatedAt = LocalDateTime.now();
        }
        if (shippingFee == null) {
            shippingFee = 0.0;
        }
        if (discount == null) {
            discount = 0.0;
        }
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
