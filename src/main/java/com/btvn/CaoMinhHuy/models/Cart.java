package com.btvn.CaoMinhHuy.models;

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

    public void decrease(Long id) {
        items.computeIfPresent(id, (k, v) -> {
            int nextQty = v.getQuantity() - 1;
            if (nextQty <= 0) return null; // remove item when quantity is zero
            v.setQuantity(nextQty);
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
