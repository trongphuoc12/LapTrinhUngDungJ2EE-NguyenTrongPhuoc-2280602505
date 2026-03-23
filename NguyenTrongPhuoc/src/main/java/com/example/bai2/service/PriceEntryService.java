package com.example.bai2.service;

import com.example.bai2.model.PriceEntry;
import com.example.bai2.model.Product;
import com.example.bai2.repository.PriceEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PriceEntryService {

    private final PriceEntryRepository priceEntryRepository;
    private final ProductService productService;

    public PriceEntryService(PriceEntryRepository priceEntryRepository, ProductService productService) {
        this.priceEntryRepository = priceEntryRepository;
        this.productService = productService;
    }

    @Transactional(readOnly = true)
    public List<PriceEntry> getAllPriceEntries(Long productId) {
        if (productId == null) {
            return priceEntryRepository.findAllByOrderByEffectiveDateDescCreatedAtDesc();
        }
        return priceEntryRepository.findByProductIdOrderByEffectiveDateDescCreatedAtDesc(productId);
    }

    @Transactional(readOnly = true)
    public Optional<PriceEntry> getPriceEntryById(Long id) {
        return priceEntryRepository.findById(id);
    }

    @Transactional
    public PriceEntry savePriceEntry(PriceEntry priceEntry, Long productId) {
        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại."));
        priceEntry.setProduct(product);
        return priceEntryRepository.save(priceEntry);
    }

    @Transactional
    public void deletePriceEntry(Long id) {
        priceEntryRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long countPriceEntries() {
        return priceEntryRepository.count();
    }
}
