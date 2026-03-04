package com.btvn.CaoMinhHuy.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    private Long bookId;
    private String title;
    private String author;
    private Double price;
    private Integer quantity;

    public Double getLineTotal() {
        double p = price != null ? price : 0;
        int q = quantity != null ? quantity : 0;
        return p * q;
    }
}
