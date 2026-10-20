package com.assessment.card_product_management.cardproduct.dto;

import java.util.List;

public record CardProductPageResponse(
        List<CardProductResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
