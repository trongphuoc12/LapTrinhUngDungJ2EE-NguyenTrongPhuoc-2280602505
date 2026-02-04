package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.model.Category;

@Service
public class CategoryService {
    // Khởi tạo danh sách category mẫu
    private List<Category> listCategory = new ArrayList<>();

    public CategoryService() {
        // Mock dữ liệu ban đầu để khi chạy web có sẵn danh mục để chọn
        listCategory.add(new Category(1, "Điện thoại"));
        listCategory.add(new Category(2, "Máy tính bảng"));
        listCategory.add(new Category(3, "Laptop"));
    }

    // Lấy tất cả danh mục
    public List<Category> getAll() {
        return listCategory;
    }

    // Lấy một danh mục theo ID
    public Category get(int id) {
        return listCategory.stream()
                .filter(c -> c.getId() == id)
                .findFirst()
                .orElse(null);
    }

    // Thêm danh mục mới
    public void add(Category category) {
        int maxId = listCategory.stream()
                .mapToInt(Category::getId)
                .max()
                .orElse(0);
        category.setId(maxId + 1);
        listCategory.add(category);
    }
}