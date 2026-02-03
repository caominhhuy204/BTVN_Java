package com.btvn.Mybook.models;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class Cart {
    private Map<Long, CartItem> items = new LinkedHashMap<>();

    public void add(Long id, String title, Double price) {
        items.compute(id, (k, v) -> {
            if (v == null) return new CartItem(id, title, price, 1);
            v.setQuantity(v.getQuantity() + 1);
            return v;
        });
    }

    public double getGrandTotal() {
        return items.values().stream().mapToDouble(CartItem::getTotal).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }
}