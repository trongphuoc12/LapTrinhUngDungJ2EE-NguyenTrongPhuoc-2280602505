package com.example.bai2.service;

import com.example.bai2.dto.ProductCatalogItem;
import com.example.bai2.dto.ProductCatalogPage;
import com.example.bai2.model.Category;
import com.example.bai2.model.PriceEntry;
import com.example.bai2.model.PriceType;
import com.example.bai2.model.Product;
import com.example.bai2.repository.PriceEntryRepository;
import com.example.bai2.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ProductService {

    public static final int PRODUCT_PAGE_SIZE = 5;

    private final ProductRepository productRepository;
    private final PriceEntryRepository priceEntryRepository;
    private final CategoryService categoryService;

    public ProductService(
            ProductRepository productRepository,
            PriceEntryRepository priceEntryRepository,
            CategoryService categoryService
    ) {
        this.productRepository = productRepository;
        this.priceEntryRepository = priceEntryRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public List<Product> getAllProducts(Long categoryId) {
        if (categoryId == null) {
            return productRepository.findAllByOrderByCreatedAtDesc();
        }
        return productRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
    }

    @Transactional(readOnly = true)
    public ProductCatalogPage getProductCatalog(String keyword, Long categoryId, String sort, int page) {
        List<Product> filteredProducts = findProductsByCriteria(keyword, categoryId);
        Map<Long, PriceEntry> displayPrices = getDisplayPriceEntries(extractProductIds(filteredProducts));
        List<ProductCatalogItem> catalogItems = new ArrayList<>();

        for (Product product : filteredProducts) {
            PriceEntry displayPrice = displayPrices.get(product.getId());
            catalogItems.add(new ProductCatalogItem(
                    product.getId(),
                    product.getSku(),
                    product.getName(),
                    product.getDescription(),
                    product.getCategory().getName(),
                    product.isActive(),
                    product.getUpdatedAt(),
                    displayPrice == null ? null : displayPrice.getPrice(),
                    displayPrice == null ? null : displayPrice.getCurrency()
            ));
        }

        sortCatalogItems(catalogItems, sort);

        int totalItems = catalogItems.size();
        int totalPages = Math.max(1, (int) Math.ceil(totalItems / (double) PRODUCT_PAGE_SIZE));
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = Math.min((currentPage - 1) * PRODUCT_PAGE_SIZE, totalItems);
        int toIndex = Math.min(fromIndex + PRODUCT_PAGE_SIZE, totalItems);

        return new ProductCatalogPage(
                new ArrayList<>(catalogItems.subList(fromIndex, toIndex)),
                currentPage,
                totalPages,
                totalItems,
                PRODUCT_PAGE_SIZE
        );
    }

    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Map<Long, PriceEntry> getDisplayPriceEntries(Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }

        List<PriceEntry> candidates = priceEntryRepository
                .findByProductIdInAndEffectiveDateLessThanEqualOrderByProductIdAscEffectiveDateDescCreatedAtDesc(
                        productIds,
                        LocalDate.now()
                );

        Map<Long, PriceEntry> latestAny = new HashMap<>();
        Map<Long, PriceEntry> latestRetail = new HashMap<>();

        for (PriceEntry candidate : candidates) {
            Long productId = candidate.getProduct().getId();
            latestAny.putIfAbsent(productId, candidate);
            if (candidate.getPriceType() == PriceType.RETAIL) {
                latestRetail.putIfAbsent(productId, candidate);
            }
        }

        Map<Long, PriceEntry> resolved = new HashMap<>();
        for (Long productId : productIds) {
            PriceEntry preferred = latestRetail.get(productId);
            if (preferred == null) {
                preferred = latestAny.get(productId);
            }
            if (preferred != null) {
                resolved.put(productId, preferred);
            }
        }
        return resolved;
    }

    @Transactional(readOnly = true)
    public Optional<PriceEntry> getDisplayPriceEntry(Long productId) {
        return Optional.ofNullable(getDisplayPriceEntries(List.of(productId)).get(productId));
    }

    @Transactional
    public Product saveProduct(Product product, Long categoryId) {
        Category category = categoryService.getCategoryById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại."));
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countProducts() {
        return productRepository.count();
    }

    @Transactional(readOnly = true)
    public long countActiveProducts() {
        return productRepository.countByActiveTrue();
    }

    private List<Product> findProductsByCriteria(String keyword, Long categoryId) {
        String normalizedKeyword = normalizeKeyword(keyword);

        if (categoryId == null && normalizedKeyword == null) {
            return productRepository.findAllByOrderByCreatedAtDesc();
        }
        if (categoryId == null) {
            return productRepository.findByNameContainingIgnoreCaseOrderByCreatedAtDesc(normalizedKeyword);
        }
        if (normalizedKeyword == null) {
            return productRepository.findByCategoryIdOrderByCreatedAtDesc(categoryId);
        }
        return productRepository.findByCategoryIdAndNameContainingIgnoreCaseOrderByCreatedAtDesc(categoryId, normalizedKeyword);
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }
        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private List<Long> extractProductIds(List<Product> products) {
        return products.stream().map(Product::getId).toList();
    }

    private void sortCatalogItems(List<ProductCatalogItem> items, String sort) {
        switch (sort) {
            case "priceAsc" -> items.sort(this::compareByPriceAsc);
            case "priceDesc" -> items.sort(this::compareByPriceDesc);
            default -> items.sort(Comparator
                    .comparing(ProductCatalogItem::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                    .thenComparing(ProductCatalogItem::getName, String.CASE_INSENSITIVE_ORDER));
        }
    }

    private int compareByPriceAsc(ProductCatalogItem left, ProductCatalogItem right) {
        BigDecimal leftPrice = left.getCurrentPrice();
        BigDecimal rightPrice = right.getCurrentPrice();

        if (leftPrice == null && rightPrice == null) {
            return left.getName().compareToIgnoreCase(right.getName());
        }
        if (leftPrice == null) {
            return 1;
        }
        if (rightPrice == null) {
            return -1;
        }

        int result = leftPrice.compareTo(rightPrice);
        if (result != 0) {
            return result;
        }
        return left.getName().compareToIgnoreCase(right.getName());
    }

    private int compareByPriceDesc(ProductCatalogItem left, ProductCatalogItem right) {
        BigDecimal leftPrice = left.getCurrentPrice();
        BigDecimal rightPrice = right.getCurrentPrice();

        if (leftPrice == null && rightPrice == null) {
            return left.getName().compareToIgnoreCase(right.getName());
        }
        if (leftPrice == null) {
            return 1;
        }
        if (rightPrice == null) {
            return -1;
        }

        int result = rightPrice.compareTo(leftPrice);
        if (result != 0) {
            return result;
        }
        return left.getName().compareToIgnoreCase(right.getName());
    }
}
