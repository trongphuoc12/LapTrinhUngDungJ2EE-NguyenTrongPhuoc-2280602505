package com.example.bai2.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ProductCatalogItem {

    private final Long id;
    private final String sku;
    private final String name;
    private final String description;
    private final String categoryName;
    private final boolean active;
    private final LocalDateTime updatedAt;
    private final BigDecimal currentPrice;
    private final String currency;

    public ProductCatalogItem(
            Long id,
            String sku,
            String name,
            String description,
            String categoryName,
            boolean active,
            LocalDateTime updatedAt,
            BigDecimal currentPrice,
            String currency
    ) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.active = active;
        this.updatedAt = updatedAt;
        this.currentPrice = currentPrice;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean hasPrice() {
        return currentPrice != null && currency != null;
    }
}
