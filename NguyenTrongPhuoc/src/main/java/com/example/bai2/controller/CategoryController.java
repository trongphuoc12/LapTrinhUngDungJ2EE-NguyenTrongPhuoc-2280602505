package com.example.bai2.controller;

import com.example.bai2.model.Category;
import com.example.bai2.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("formTitle", "Thêm danh mục");
        model.addAttribute("submitLabel", "Lưu danh mục");
        model.addAttribute("formAction", "/categories/new");
        return "category-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createCategory(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Thêm danh mục");
            model.addAttribute("submitLabel", "Lưu danh mục");
            model.addAttribute("formAction", "/categories/new");
            return "category-form";
        }

        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm danh mục thành công.");
            return "redirect:/categories";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("name", "category.name", "Tên danh mục đã tồn tại.");
            model.addAttribute("formTitle", "Thêm danh mục");
            model.addAttribute("submitLabel", "Lưu danh mục");
            model.addAttribute("formAction", "/categories/new");
            return "category-form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editCategoryForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return categoryService.getCategoryById(id)
                .map(category -> {
                    model.addAttribute("category", category);
                    model.addAttribute("formTitle", "Cập nhật danh mục");
                    model.addAttribute("submitLabel", "Cập nhật");
                    model.addAttribute("formAction", "/categories/" + id + "/edit");
                    return "category-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục cần sửa.");
                    return "redirect:/categories";
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateCategory(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("formTitle", "Cập nhật danh mục");
            model.addAttribute("submitLabel", "Cập nhật");
            model.addAttribute("formAction", "/categories/" + id + "/edit");
            return "category-form";
        }

        category.setId(id);
        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật danh mục.");
            return "redirect:/categories";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("name", "category.name", "Tên danh mục đã tồn tại.");
            model.addAttribute("formTitle", "Cập nhật danh mục");
            model.addAttribute("submitLabel", "Cập nhật");
            model.addAttribute("formAction", "/categories/" + id + "/edit");
            return "category-form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa danh mục.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa danh mục đang được sản phẩm sử dụng.");
        }
        return "redirect:/categories";
    }
}
