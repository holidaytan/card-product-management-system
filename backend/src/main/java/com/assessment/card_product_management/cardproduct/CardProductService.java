package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
import com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest;
import com.assessment.card_product_management.cardproduct.dto.UpdateCardProductRequest;
import com.assessment.card_product_management.config.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class CardProductService {

    public static final int PAGE_SIZE = 10;

    private final CardProductRepository cardProductRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public CardProductService(
            CardProductRepository cardProductRepository,
            IdempotencyRecordRepository idempotencyRecordRepository
    ) {
        this.cardProductRepository = cardProductRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, key = "#page", sync = true)
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
        return createCardProduct(request, null);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, allEntries = true)
    public CardProductResponse createCardProduct(CreateCardProductRequest request, String idempotencyKey) {
        String requestHash = idempotencyKey == null ? null : requestHash(request);
        if (idempotencyKey != null) {
            var existingRecord = idempotencyRecordRepository.findById(idempotencyKey);
            if (existingRecord.isPresent()) {
                if (!existingRecord.get().getRequestHash().equals(requestHash)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Idempotency-Key was already used with a different request"
                    );
                }
                return CardProductResponse.from(existingRecord.get().getCardProduct());
            }
        }

        CardProduct product = new CardProduct(
                request.productName().trim(),
                request.cardType(),
                request.quantity()
        );
        CardProduct savedProduct = cardProductRepository.saveAndFlush(product);

        if (idempotencyKey != null) {
            idempotencyRecordRepository.saveAndFlush(
                    new IdempotencyRecord(idempotencyKey, requestHash, savedProduct)
            );
        }

        return CardProductResponse.from(savedProduct);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, allEntries = true)
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
    @CacheEvict(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, allEntries = true)
    public void deleteCardProduct(long id) {
        CardProduct product = cardProductRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card product not found"));
        cardProductRepository.delete(product);
    }

    private String requestHash(CreateCardProductRequest request) {
        String normalizedRequest = String.join(
                "|",
                request.productName().trim(),
                request.cardType().name(),
                request.quantity().toString()
        );
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalizedRequest.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
