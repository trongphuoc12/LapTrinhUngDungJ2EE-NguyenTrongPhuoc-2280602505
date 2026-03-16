package com.example.Bai5.service;

import com.example.Bai5.model.Category;
import com.example.Bai5.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryService(CategoryRepository categoryRepository) {
		this.categoryRepository = categoryRepository;
	}

	public List<Category> getAll() {
		return categoryRepository.findAll();
	}

	public Category getById(Integer id) {
		return categoryRepository.findById(id).orElse(null);
	}
}