package com.assessment.card_product_management.cardproduct;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name = "card_products", schema = "dbo")
public class CardProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false, length = 20)
    private CardType cardType;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "annual_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualFee;

    @Column(name = "annual_interest_rate", precision = 5, scale = 2)
    private BigDecimal annualInterestRate;

    @Column(name = "currency_code", nullable = false, length = 3, updatable = false)
    private String currencyCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    protected CardProduct() {
    }

    public CardProduct(
            String productName,
            CardType cardType,
            int quantity,
            BigDecimal annualFee,
            BigDecimal annualInterestRate,
            LocalDateTime timestamp
    ) {
        this.productName = productName;
        this.cardType = cardType;
        this.quantity = quantity;
        this.annualFee = annualFee;
        this.annualInterestRate = annualInterestRate;
        this.currencyCode = "MYR";
        this.createdAt = timestamp;
        this.updatedAt = this.createdAt;
    }

    public void update(
            String productName,
            CardType cardType,
            int quantity,
            BigDecimal annualFee,
            BigDecimal annualInterestRate,
            LocalDateTime timestamp
    ) {
        this.productName = productName;
        this.cardType = cardType;
        this.quantity = quantity;
        this.annualFee = annualFee;
        this.annualInterestRate = annualInterestRate;
        this.updatedAt = timestamp;
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public CardType getCardType() {
        return cardType;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getAnnualFee() {
        return annualFee;
    }

    public BigDecimal getAnnualInterestRate() {
        return annualInterestRate;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }
}
