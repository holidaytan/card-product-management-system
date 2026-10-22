package com.assessment.card_product_management.cardproduct.dto;

public record CardProductSummaryResponse(
        long totalProducts,
        long totalAvailableInventory
) {
}
