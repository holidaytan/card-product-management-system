package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
import com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CardProductService {

    public static final int PAGE_SIZE = 10;

    private final CardProductRepository cardProductRepository;

    public CardProductService(CardProductRepository cardProductRepository) {
        this.cardProductRepository = cardProductRepository;
    }

    @Transactional(readOnly = true)
    public CardProductPageResponse getCardProducts(int page) {
        Page<CardProduct> products = cardProductRepository.findAll(
                PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.ASC, "id"))
        );

        return new CardProductPageResponse(
                products.getContent().stream().map(CardProductResponse::from).toList(),
                products.getNumber(),
                products.getSize(),
                products.getTotalElements(),
                products.getTotalPages(),
                products.isFirst(),
                products.isLast()
        );
    }

    @Transactional
    public CardProductResponse createCardProduct(CreateCardProductRequest request) {
        CardProduct product = new CardProduct(
                request.productName().trim(),
                request.cardType(),
                request.quantity()
        );
        return CardProductResponse.from(cardProductRepository.save(product));
    }
}
