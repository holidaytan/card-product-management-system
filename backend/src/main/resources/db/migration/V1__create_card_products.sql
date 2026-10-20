CREATE TABLE dbo.card_products (
    id BIGINT IDENTITY(1, 1) NOT NULL,
    product_name NVARCHAR(150) NOT NULL,
    card_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL
        CONSTRAINT DF_card_products_quantity DEFAULT (0),
    created_at DATETIME2(3) NOT NULL
        CONSTRAINT DF_card_products_created_at DEFAULT (SYSUTCDATETIME()),
    updated_at DATETIME2(3) NOT NULL
        CONSTRAINT DF_card_products_updated_at DEFAULT (SYSUTCDATETIME()),
    version BIGINT NOT NULL
        CONSTRAINT DF_card_products_version DEFAULT (0),

    CONSTRAINT PK_card_products PRIMARY KEY (id),
    CONSTRAINT CK_card_products_card_type
        CHECK (card_type IN ('CREDIT', 'DEBIT')),
    CONSTRAINT CK_card_products_quantity_non_negative
        CHECK (quantity >= 0)
);