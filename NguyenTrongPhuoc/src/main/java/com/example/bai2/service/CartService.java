package com.example.bai2.service;

import com.example.bai2.dto.CartItemView;
import com.example.bai2.dto.CartView;
import com.example.bai2.model.Order;
import com.example.bai2.model.OrderDetail;
import com.example.bai2.model.PriceEntry;
import com.example.bai2.model.Product;
import com.example.bai2.repository.OrderRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartService {

    private static final String CART_SESSION_KEY = "shoppingCart";

    private final ProductService productService;
    private final OrderRepository orderRepository;

    public CartService(ProductService productService, OrderRepository orderRepository) {
        this.productService = productService;
        this.orderRepository = orderRepository;
    }

    public void addToCart(Long productId, int quantity, HttpSession session) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        }

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
        if (productService.getDisplayPriceEntry(product.getId()).isEmpty()) {
            throw new IllegalArgumentException("Sản phẩm này chưa có giá để đặt hàng.");
        }

        Map<Long, Integer> cart = getOrCreateCart(session);
        cart.merge(productId, quantity, Integer::sum);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void updateQuantity(Long productId, int quantity, HttpSession session) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        if (!cart.containsKey(productId)) {
            return;
        }

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.put(productId, quantity);
        }
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public void removeFromCart(Long productId, HttpSession session) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        cart.remove(productId);
        session.setAttribute(CART_SESSION_KEY, cart);
    }

    public int getCartItemCount(HttpSession session) {
        return getOrCreateCart(session).values().stream().mapToInt(Integer::intValue).sum();
    }

    public CartView getCart(HttpSession session) {
        Map<Long, Integer> cart = getOrCreateCart(session);
        if (cart.isEmpty()) {
            return new CartView(List.of(), 0, BigDecimal.ZERO, "VND");
        }

        List<Long> productIds = new ArrayList<>(cart.keySet());
        Map<Long, PriceEntry> displayPrices = productService.getDisplayPriceEntries(productIds);
        List<CartItemView> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        String currency = "VND";

        for (Long productId : productIds) {
            Product product = productService.getProductById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm trong giỏ không còn tồn tại."));
            PriceEntry priceEntry = displayPrices.get(productId);
            if (priceEntry == null) {
                throw new IllegalArgumentException("Sản phẩm trong giỏ chưa có giá hợp lệ.");
            }

            int quantity = cart.get(productId);
            BigDecimal lineTotal = priceEntry.getPrice().multiply(BigDecimal.valueOf(quantity));
            items.add(new CartItemView(
                    product.getId(),
                    product.getName(),
                    product.getSku(),
                    priceEntry.getPrice(),
                    priceEntry.getCurrency(),
                    quantity,
                    lineTotal
            ));

            totalAmount = totalAmount.add(lineTotal);
            totalQuantity += quantity;
            currency = priceEntry.getCurrency();
        }

        return new CartView(items, totalQuantity, totalAmount, currency);
    }

    @Transactional
    public Order checkout(HttpSession session, String accountUsername) {
        CartView cartView = getCart(session);
        if (cartView.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng đang trống.");
        }

        Order order = new Order();
        order.setPaid(false);
        order.setAccountUsername(accountUsername);
        order.setTotalAmount(cartView.getTotalAmount());

        for (CartItemView item : cartView.getItems()) {
            Product product = productService.getProductById(item.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không còn tồn tại."));

            OrderDetail detail = new OrderDetail();
            detail.setProduct(product);
            detail.setPrice(item.getUnitPrice());
            detail.setQuantity(item.getQuantity());
            order.addDetail(detail);
        }

        Order savedOrder = orderRepository.save(order);
        clearCart(session);
        return savedOrder;
    }

    @Transactional(readOnly = true)
    public Optional<Order> getDetailedOrder(Long orderId) {
        return orderRepository.findDetailedById(orderId);
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
    }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> getOrCreateCart(HttpSession session) {
        Object existing = session.getAttribute(CART_SESSION_KEY);
        if (existing instanceof Map<?, ?> existingMap) {
            return (Map<Long, Integer>) existingMap;
        }
        Map<Long, Integer> cart = new LinkedHashMap<>();
        session.setAttribute(CART_SESSION_KEY, cart);
        return cart;
    }
}
