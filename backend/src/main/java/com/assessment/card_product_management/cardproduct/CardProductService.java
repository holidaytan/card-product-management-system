package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
import com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest;
import com.assessment.card_product_management.cardproduct.dto.UpdateCardProductRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
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

    @Transactional
    public CardProductResponse updateCardProduct(long id, UpdateCardProductRequest request) {
        CardProduct product = cardProductRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card product not found"));
        if (!product.getVersion().equals(request.version())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Card product has been modified; refresh and retry");
        }
        product.update(request.productName().trim(), request.cardType(), request.quantity());
        return CardProductResponse.from(cardProductRepository.saveAndFlush(product));
    }

    @Transactional
    public void deleteCardProduct(long id) {
        CardProduct product = cardProductRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card product not found"));
        cardProductRepository.delete(product);
    }
}
