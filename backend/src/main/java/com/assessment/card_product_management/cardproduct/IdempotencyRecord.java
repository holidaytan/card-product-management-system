package com.assessment.card_product_management.cardproduct;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "idempotency_records", schema = "dbo")
public class IdempotencyRecord {

    @Id
    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "card_product_id", nullable = false)
    private CardProduct cardProduct;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, String requestHash, CardProduct cardProduct) {
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.cardProduct = cardProduct;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public CardProduct getCardProduct() {
        return cardProduct;
    }
}
