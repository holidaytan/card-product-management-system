ALTER TABLE dbo.card_products
    ADD annual_fee DECIMAL(12, 2) NOT NULL
            CONSTRAINT df_card_products_annual_fee DEFAULT (0.00) WITH VALUES,
        annual_interest_rate DECIMAL(5, 2) NULL,
        currency_code VARCHAR(3) NOT NULL
            CONSTRAINT df_card_products_currency_code DEFAULT ('MYR') WITH VALUES;

GO

ALTER TABLE dbo.card_products
    ADD CONSTRAINT ck_card_products_annual_fee_non_negative
        CHECK (annual_fee >= 0),
        CONSTRAINT ck_card_products_annual_interest_rate_range
        CHECK (annual_interest_rate IS NULL OR annual_interest_rate BETWEEN 0 AND 100),
        CONSTRAINT ck_card_products_debit_has_no_interest_rate
        CHECK (card_type = 'CREDIT' OR annual_interest_rate IS NULL),
        CONSTRAINT ck_card_products_currency_code_myr
        CHECK (currency_code = 'MYR');
