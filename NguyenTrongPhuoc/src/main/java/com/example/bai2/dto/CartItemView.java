package com.example.bai2.dto;

import java.math.BigDecimal;

public class CartItemView {

    private final Long productId;
    private final String productName;
    private final String sku;
    private final BigDecimal unitPrice;
    private final String currency;
    private final int quantity;
    private final BigDecimal lineTotal;

    public CartItemView(
            Long productId,
            String productName,
            String sku,
            BigDecimal unitPrice,
            String currency,
            int quantity,
            BigDecimal lineTotal
    ) {
        this.productId = productId;
        this.productName = productName;
        this.sku = sku;
        this.unitPrice = unitPrice;
        this.currency = currency;
        this.quantity = quantity;
        this.lineTotal = lineTotal;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public String getSku() {
        return sku;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
