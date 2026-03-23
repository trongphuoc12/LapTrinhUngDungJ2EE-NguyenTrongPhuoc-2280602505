package com.example.bai2.controller;

import com.example.bai2.service.CategoryService;
import com.example.bai2.service.PriceEntryService;
import com.example.bai2.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final PriceEntryService priceEntryService;

    public HomeController(ProductService productService, CategoryService categoryService, PriceEntryService priceEntryService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.priceEntryService = priceEntryService;
    }

    @GetMapping({"/", "/home"})
    public String index(Model model) {
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("activeProducts", productService.countActiveProducts());
        model.addAttribute("totalCategories", categoryService.countCategories());
        model.addAttribute("totalPriceEntries", priceEntryService.countPriceEntries());
        return "index";
    }
}
