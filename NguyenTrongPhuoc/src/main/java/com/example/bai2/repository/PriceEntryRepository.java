package com.example.bai2.repository;

import com.example.bai2.model.PriceEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface PriceEntryRepository extends JpaRepository<PriceEntry, Long> {
    List<PriceEntry> findAllByOrderByEffectiveDateDescCreatedAtDesc();

    List<PriceEntry> findByProductIdOrderByEffectiveDateDescCreatedAtDesc(Long productId);

    List<PriceEntry> findByProductIdInAndEffectiveDateLessThanEqualOrderByProductIdAscEffectiveDateDescCreatedAtDesc(
            Collection<Long> productIds,
            LocalDate effectiveDate
    );
}
