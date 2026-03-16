package com.example.Bai5.controller;

import com.example.Bai5.model.Product;
import com.example.Bai5.model.Category;
import com.example.Bai5.service.CategoryService;
import com.example.Bai5.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

	private final ProductService productService;
	private final CategoryService categoryService;

	public ProductController(ProductService productService, CategoryService categoryService) {
		this.productService = productService;
		this.categoryService = categoryService;
	}

	// ================= LIST =================
	@GetMapping
	public String list(Model model) {
		List<Product> products = productService.getAll();
		model.addAttribute("products", products);
		return "product/list";
	}

	// ================= ADD =================
	@GetMapping("/add")
	public String addForm(Model model) {
		model.addAttribute("product", new Product());
		model.addAttribute("categories", categoryService.getAll());
		return "product/add";
	}

	@PostMapping
	public String create(@Valid @ModelAttribute Product product,
			BindingResult bindingResult,
			@RequestParam(value = "categoryId", required = false) Integer categoryId,
			@RequestParam(value = "imageFile", required = false) MultipartFile file,
			RedirectAttributes redirectAttributes,
			Model model) throws Exception {

		if (bindingResult.hasErrors()) {
			model.addAttribute("categories", categoryService.getAll());
			return "product/add";
		}

		// Kiểm tra categoryId thủ công
		if (categoryId == null) {
			bindingResult.rejectValue("category", "error.category.required", "Vui lòng chọn danh mục");
			model.addAttribute("categories", categoryService.getAll());
			return "product/add";
		}

		Category category = categoryService.getById(categoryId);
		if (category == null) {
			bindingResult.rejectValue("category", "error.category.invalid", "Danh mục không tồn tại");
			model.addAttribute("categories", categoryService.getAll());
			return "product/add";
		}

		product.setCategory(category);

		// Xử lý ảnh (nếu có)
		if (!file.isEmpty()) {
			String fileName = productService.uploadImage(file);
			product.setImage(fileName);
		}

		productService.save(product);

		redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công!");
		return "redirect:/products";
	}

	// ================= EDIT =================
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable Long id, Model model) {
		Product product = productService.getById(id);
		if (product == null) {
			return "redirect:/products"; // hoặc trả về trang lỗi
		}
		model.addAttribute("product", product);
		model.addAttribute("categories", categoryService.getAll());
		return "product/edit";
	}

	@PostMapping("/update/{id}") // Hoặc @PostMapping("/{id}")
	public String update(@PathVariable Long id,
			@ModelAttribute Product product,
			@RequestParam("categoryId") Integer categoryId,
			@RequestParam(value = "imageFile", required = false) MultipartFile file) throws Exception {

		Product existing = productService.getById(id);
		if (existing == null) {
			return "redirect:/products";
		}

		existing.setName(product.getName());
		existing.setPrice(product.getPrice());
		existing.setCategory(categoryService.getById(categoryId));

		if (file != null && !file.isEmpty()) {
			String fileName = productService.uploadImage(file);
			existing.setImage(fileName);
		}

		productService.save(existing);

		return "redirect:/products";
	}

	// ================= DELETE =================
	@DeleteMapping("/{id}")
	public String delete(@PathVariable Long id) {
		productService.delete(id);
		return "redirect:/products";
	}
}