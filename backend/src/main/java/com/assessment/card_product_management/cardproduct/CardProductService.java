package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductSummaryResponse;
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
import java.time.Clock;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Service
public class CardProductService {

    public static final int PAGE_SIZE = 10;

    private final CardProductRepository cardProductRepository;
    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final Clock applicationClock;

    public CardProductService(
            CardProductRepository cardProductRepository,
            IdempotencyRecordRepository idempotencyRecordRepository,
            Clock applicationClock
    ) {
        this.cardProductRepository = cardProductRepository;
        this.idempotencyRecordRepository = idempotencyRecordRepository;
        this.applicationClock = applicationClock;
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, key = "#page + ':' + (#name ?: '') + ':' + (#cardType ?: '')", sync = true)
    public CardProductPageResponse getCardProducts(int page, String name, CardType cardType) {
        Page<CardProduct> products = cardProductRepository.findAllByFilters(
                name,
                cardType,
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

    @Transactional(readOnly = true)
    public CardProductPageResponse getCardProducts(int page) {
        return getCardProducts(page, null, null);
    }

    @Transactional(readOnly = true)
    public CardProductResponse getCardProduct(long id) {
        CardProduct product = cardProductRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card product not found"));
        return CardProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public CardProductSummaryResponse getCardProductSummary() {
        CardProductSummaryProjection summary = cardProductRepository.getSummary();
        return new CardProductSummaryResponse(
                summary.getTotalProducts(),
                summary.getTotalAvailableInventory()
        );
    }

    @Transactional
    public CardProductResponse createCardProduct(CreateCardProductRequest request) {
        return createCardProduct(request, null);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, allEntries = true)
    public CardProductResponse createCardProduct(CreateCardProductRequest request, String idempotencyKey) {
        validatePricing(request.cardType(), request.annualFee(), request.annualInterestRate());
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
                request.quantity(),
                request.annualFee(),
                request.annualInterestRate(),
                LocalDateTime.now(applicationClock)
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
        validatePricing(request.cardType(), request.annualFee(), request.annualInterestRate());
        product.update(
                request.productName().trim(),
                request.cardType(),
                request.quantity(),
                request.annualFee(),
                request.annualInterestRate(),
                LocalDateTime.now(applicationClock)
        );
        return CardProductResponse.from(cardProductRepository.saveAndFlush(product));
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CARD_PRODUCT_PAGES_CACHE, allEntries = true)
    public void deleteCardProduct(long id) {
        CardProduct product = cardProductRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Card product not found"));
        if (product.getQuantity() > 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Card product can only be deleted when quantity is zero"
            );
        }
        cardProductRepository.delete(product);
    }

    private String requestHash(CreateCardProductRequest request) {
        String normalizedRequest = String.join(
                "|",
                request.productName().trim(),
                request.cardType().name(),
                request.quantity().toString(),
                request.annualFee().stripTrailingZeros().toPlainString(),
                request.annualInterestRate() == null ? "" : request.annualInterestRate().stripTrailingZeros().toPlainString()
        );
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(normalizedRequest.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private void validatePricing(CardType cardType, BigDecimal annualFee, BigDecimal annualInterestRate) {
        if (annualFee == null || annualFee.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "annualFee must be zero or greater");
        }
        if (annualInterestRate != null && (annualInterestRate.signum() < 0
                || annualInterestRate.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "annualInterestRate must be between 0 and 100");
        }
        if (cardType == CardType.DEBIT && annualInterestRate != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "annualInterestRate must be null for DEBIT cards");
        }
    }
}
