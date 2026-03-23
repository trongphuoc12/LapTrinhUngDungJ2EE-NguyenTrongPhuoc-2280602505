package com.example.bai2.controller;

import com.example.bai2.model.Order;
import com.example.bai2.service.CartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Controller
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String viewCart(Model model, HttpSession session) {
        model.addAttribute("cart", cartService.getCart(session));
        model.addAttribute("cartItemCount", cartService.getCartItemCount(session));
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer page,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        try {
            cartService.addToCart(productId, quantity, session);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:" + buildProductsRedirect(keyword, categoryId, sort, page);
    }

    @PostMapping("/cart/update")
    public String updateCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        cartService.updateQuantity(productId, quantity, session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật số lượng trong giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(
            @RequestParam Long productId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        cartService.removeFromCart(productId, session);
        redirectAttributes.addFlashAttribute("successMessage", "Đã xóa sản phẩm khỏi giỏ hàng.");
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(
            HttpSession session,
            Authentication authentication,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Order order = cartService.checkout(session, authentication == null ? null : authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "Đặt hàng thành công.");
            return "redirect:/cart/success?orderId=" + order.getId();
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/cart";
        }
    }

    @GetMapping("/cart/success")
    public String checkoutSuccess(
            @RequestParam Long orderId,
            Model model,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        return cartService.getDetailedOrder(orderId)
                .map(order -> {
                    model.addAttribute("order", order);
                    model.addAttribute("cartItemCount", cartService.getCartItemCount(session));
                    return "checkout-success";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy đơn hàng vừa đặt.");
                    return "redirect:/cart";
                });
    }

    private String buildProductsRedirect(String keyword, Long categoryId, String sort, Integer page) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/products");
        if (keyword != null && !keyword.isBlank()) {
            builder.queryParam("keyword", keyword.trim());
        }
        if (categoryId != null) {
            builder.queryParam("categoryId", categoryId);
        }
        if (sort != null && !sort.isBlank() && !"newest".equals(sort)) {
            builder.queryParam("sort", sort);
        }
        if (page != null && page > 1) {
            builder.queryParam("page", page);
        }
        return builder.build().toUriString();
    }
}
