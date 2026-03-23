package com.example.bai2.dto;

import java.math.BigDecimal;
import java.util.List;

public class CartView {

    private final List<CartItemView> items;
    private final int totalQuantity;
    private final BigDecimal totalAmount;
    private final String currency;

    public CartView(List<CartItemView> items, int totalQuantity, BigDecimal totalAmount, String currency) {
        this.items = items;
        this.totalQuantity = totalQuantity;
        this.totalAmount = totalAmount;
        this.currency = currency;
    }

    public List<CartItemView> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
