package com.btvn.CaoMinhHuy.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    private Long bookId;
    private String title;
    private Double price;
    private int quantity;

    public Double getTotal() {
        return price * quantity;
    }
}