package com.assessment.card_product_management.cardproduct;

import com.assessment.card_product_management.cardproduct.dto.CardProductPageResponse;
import com.assessment.card_product_management.cardproduct.dto.CardProductResponse;
import com.assessment.card_product_management.cardproduct.dto.CreateCardProductRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/api/v1/card-products")
public class CardProductController {

    private final CardProductService cardProductService;

    public CardProductController(CardProductService cardProductService) {
        this.cardProductService = cardProductService;
    }

    @GetMapping
    public ResponseEntity<CardProductPageResponse> getCardProducts(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "page must be zero or greater") int page
    ) {
        return ResponseEntity.ok(cardProductService.getCardProducts(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CardProductResponse> getCardProduct(@PathVariable @Min(1) long id) {
        return ResponseEntity.ok(cardProductService.getCardProduct(id));
    }

    @PostMapping
    public ResponseEntity<CardProductResponse> createCardProduct(
            @Valid @RequestBody CreateCardProductRequest request,
            @RequestHeader(name = "Idempotency-Key", required = false)
            @Pattern(regexp = "^[!-~]{1,255}$", message = "Idempotency-Key must contain 1 to 255 visible ASCII characters")
            String idempotencyKey
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cardProductService.createCardProduct(request, idempotencyKey));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CardProductResponse> updateCardProduct(
            @PathVariable @Min(1) long id,
            @Valid @RequestBody com.assessment.card_product_management.cardproduct.dto.UpdateCardProductRequest request
    ) {
        return ResponseEntity.ok(cardProductService.updateCardProduct(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCardProduct(@PathVariable @Min(1) long id) {
        cardProductService.deleteCardProduct(id);
        return ResponseEntity.noContent().build();
    }
}
