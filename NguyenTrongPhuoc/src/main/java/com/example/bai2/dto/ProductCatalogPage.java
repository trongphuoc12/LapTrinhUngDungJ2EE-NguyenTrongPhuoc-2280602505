package com.example.bai2.dto;

import java.util.List;

public class ProductCatalogPage {

    private final List<ProductCatalogItem> items;
    private final int currentPage;
    private final int totalPages;
    private final long totalItems;
    private final int pageSize;

    public ProductCatalogPage(List<ProductCatalogItem> items, int currentPage, int totalPages, long totalItems, int pageSize) {
        this.items = items;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
        this.pageSize = pageSize;
    }

    public List<ProductCatalogItem> getItems() {
        return items;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public int getPageSize() {
        return pageSize;
    }

    public boolean isHasPrevious() {
        return currentPage > 1;
    }

    public boolean isHasNext() {
        return currentPage < totalPages;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
