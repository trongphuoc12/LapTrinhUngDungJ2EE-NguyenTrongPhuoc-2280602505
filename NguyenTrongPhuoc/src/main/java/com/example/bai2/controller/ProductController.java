package com.example.bai2.controller;

import com.example.bai2.model.Product;
import com.example.bai2.service.CartService;
import com.example.bai2.service.CategoryService;
import com.example.bai2.service.ProductService;
import jakarta.servlet.http.HttpSession;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;

    public ProductController(ProductService productService, CategoryService categoryService, CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "newest") String sort,
            @RequestParam(defaultValue = "1") int page,
            Model model,
            HttpSession session
    ) {
        model.addAttribute("catalogPage", productService.getProductCatalog(keyword, categoryId, sort, page));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("keyword", keyword == null ? "" : keyword.trim());
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedSort", sort);
        model.addAttribute("cartItemCount", cartService.getCartItemCount(session));
        return "products";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createProductForm(Model model) {
        prepareProductForm(model, new Product(), null, "Thêm sản phẩm", "Lưu sản phẩm", "/products/new");
        return "product-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createProduct(
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (categoryId == null) {
            bindingResult.rejectValue("category", "product.category", "Vui lòng chọn danh mục.");
        }

        if (bindingResult.hasErrors()) {
            prepareProductForm(model, product, categoryId, "Thêm sản phẩm", "Lưu sản phẩm", "/products/new");
            return "product-form";
        }

        try {
            productService.saveProduct(product, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm thành công.");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("category", "product.category", ex.getMessage());
            prepareProductForm(model, product, categoryId, "Thêm sản phẩm", "Lưu sản phẩm", "/products/new");
            return "product-form";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("sku", "product.sku", "SKU đã tồn tại.");
            prepareProductForm(model, product, categoryId, "Thêm sản phẩm", "Lưu sản phẩm", "/products/new");
            return "product-form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return productService.getProductById(id)
                .map(product -> {
                    Long categoryId = product.getCategory() == null ? null : product.getCategory().getId();
                    prepareProductForm(model, product, categoryId, "Cập nhật sản phẩm", "Cập nhật", "/products/" + id + "/edit");
                    return "product-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm cần sửa.");
                    return "redirect:/products";
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("product") Product product,
            BindingResult bindingResult,
            @RequestParam(required = false) Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (categoryId == null) {
            bindingResult.rejectValue("category", "product.category", "Vui lòng chọn danh mục.");
        }

        if (bindingResult.hasErrors()) {
            prepareProductForm(model, product, categoryId, "Cập nhật sản phẩm", "Cập nhật", "/products/" + id + "/edit");
            return "product-form";
        }

        product.setId(id);
        try {
            productService.saveProduct(product, categoryId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật sản phẩm.");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("category", "product.category", ex.getMessage());
            prepareProductForm(model, product, categoryId, "Cập nhật sản phẩm", "Cập nhật", "/products/" + id + "/edit");
            return "product-form";
        } catch (DataIntegrityViolationException ex) {
            bindingResult.rejectValue("sku", "product.sku", "SKU đã tồn tại.");
            prepareProductForm(model, product, categoryId, "Cập nhật sản phẩm", "Cập nhật", "/products/" + id + "/edit");
            return "product-form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa sản phẩm này.");
        }
        return "redirect:/products";
    }

    private void prepareProductForm(
            Model model,
            Product product,
            Long selectedCategoryId,
            String formTitle,
            String submitLabel,
            String formAction
    ) {
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("selectedCategoryId", selectedCategoryId);
        model.addAttribute("formTitle", formTitle);
        model.addAttribute("submitLabel", submitLabel);
        model.addAttribute("formAction", formAction);
    }
}
