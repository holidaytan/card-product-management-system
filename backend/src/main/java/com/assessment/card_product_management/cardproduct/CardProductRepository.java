package com.assessment.card_product_management.cardproduct;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CardProductRepository extends JpaRepository<CardProduct, Long> {

    @Query("""
            select product
            from CardProduct product
            where (:name is null or lower(product.productName) like lower(concat('%', :name, '%')))
              and (:cardType is null or product.cardType = :cardType)
            """)
    Page<CardProduct> findAllByFilters(
            @Param("name") String name,
            @Param("cardType") CardType cardType,
            Pageable pageable
    );

    @Query("""
            select count(product) as totalProducts,
                   coalesce(sum(product.quantity), 0) as totalAvailableInventory
            from CardProduct product
            """)
    CardProductSummaryProjection getSummary();
}
