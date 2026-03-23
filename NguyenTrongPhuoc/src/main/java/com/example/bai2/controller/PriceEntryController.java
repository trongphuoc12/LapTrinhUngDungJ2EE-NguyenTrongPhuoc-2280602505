package com.example.bai2.controller;

import com.example.bai2.model.PriceEntry;
import com.example.bai2.model.PriceType;
import com.example.bai2.service.PriceEntryService;
import com.example.bai2.service.ProductService;
import jakarta.validation.Valid;
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

import java.time.LocalDate;

@Controller
@RequestMapping("/prices")
public class PriceEntryController {

    private final PriceEntryService priceEntryService;
    private final ProductService productService;

    public PriceEntryController(PriceEntryService priceEntryService, ProductService productService) {
        this.priceEntryService = priceEntryService;
        this.productService = productService;
    }

    @GetMapping
    public String listPriceEntries(@RequestParam(required = false) Long productId, Model model) {
        model.addAttribute("priceEntries", priceEntryService.getAllPriceEntries(productId));
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("selectedProductId", productId);
        return "prices";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String createPriceForm(Model model) {
        PriceEntry priceEntry = new PriceEntry();
        priceEntry.setEffectiveDate(LocalDate.now());
        preparePriceForm(model, priceEntry, null, "Thêm dòng bảng giá", "Lưu giá", "/prices/new");
        return "price-form";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    public String createPriceEntry(
            @Valid @ModelAttribute("priceEntry") PriceEntry priceEntry,
            BindingResult bindingResult,
            @RequestParam(required = false) Long productId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (productId == null) {
            bindingResult.rejectValue("product", "priceEntry.product", "Vui lòng chọn sản phẩm.");
        }

        if (bindingResult.hasErrors()) {
            preparePriceForm(model, priceEntry, productId, "Thêm dòng bảng giá", "Lưu giá", "/prices/new");
            return "price-form";
        }

        try {
            priceEntryService.savePriceEntry(priceEntry, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm dòng bảng giá.");
            return "redirect:/prices";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("product", "priceEntry.product", ex.getMessage());
            preparePriceForm(model, priceEntry, productId, "Thêm dòng bảng giá", "Lưu giá", "/prices/new");
            return "price-form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}/edit")
    public String editPriceForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return priceEntryService.getPriceEntryById(id)
                .map(priceEntry -> {
                    Long productId = priceEntry.getProduct() == null ? null : priceEntry.getProduct().getId();
                    preparePriceForm(model, priceEntry, productId, "Cập nhật bảng giá", "Cập nhật", "/prices/" + id + "/edit");
                    return "price-form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy dòng bảng giá cần sửa.");
                    return "redirect:/prices";
                });
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/edit")
    public String updatePriceEntry(
            @PathVariable Long id,
            @Valid @ModelAttribute("priceEntry") PriceEntry priceEntry,
            BindingResult bindingResult,
            @RequestParam(required = false) Long productId,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (productId == null) {
            bindingResult.rejectValue("product", "priceEntry.product", "Vui lòng chọn sản phẩm.");
        }

        if (bindingResult.hasErrors()) {
            preparePriceForm(model, priceEntry, productId, "Cập nhật bảng giá", "Cập nhật", "/prices/" + id + "/edit");
            return "price-form";
        }

        priceEntry.setId(id);
        try {
            priceEntryService.savePriceEntry(priceEntry, productId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật dòng bảng giá.");
            return "redirect:/prices";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("product", "priceEntry.product", ex.getMessage());
            preparePriceForm(model, priceEntry, productId, "Cập nhật bảng giá", "Cập nhật", "/prices/" + id + "/edit");
            return "price-form";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/delete")
    public String deletePriceEntry(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            priceEntryService.deletePriceEntry(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa dòng bảng giá.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa dòng bảng giá này.");
        }
        return "redirect:/prices";
    }

    private void preparePriceForm(
            Model model,
            PriceEntry priceEntry,
            Long selectedProductId,
            String formTitle,
            String submitLabel,
            String formAction
    ) {
        model.addAttribute("priceEntry", priceEntry);
        model.addAttribute("products", productService.getAllProducts(null));
        model.addAttribute("selectedProductId", selectedProductId);
        model.addAttribute("priceTypes", PriceType.values());
        model.addAttribute("formTitle", formTitle);
        model.addAttribute("submitLabel", submitLabel);
        model.addAttribute("formAction", formAction);
    }
}
