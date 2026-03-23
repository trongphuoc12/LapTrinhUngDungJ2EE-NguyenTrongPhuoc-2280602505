package com.example.bai2.model;

public enum PriceType {
    RETAIL("Bán lẻ"),
    WHOLESALE("Bán sỉ"),
    ONLINE("Online"),
    PROMOTION("Khuyến mãi");

    private final String label;

    PriceType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
