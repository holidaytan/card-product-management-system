IF OBJECT_ID(N'idempotency_records', N'U') IS NULL
BEGIN
    CREATE TABLE idempotency_records (
        idempotency_key NVARCHAR(255) NOT NULL,
        request_hash VARCHAR(64) NOT NULL,
        card_product_id BIGINT NOT NULL,
        created_at DATETIME2(3) NOT NULL CONSTRAINT df_idempotency_records_created_at DEFAULT SYSUTCDATETIME(),
        CONSTRAINT pk_idempotency_records PRIMARY KEY (idempotency_key),
        CONSTRAINT fk_idempotency_records_card_product
            FOREIGN KEY (card_product_id) REFERENCES card_products(id) ON DELETE CASCADE
    );
END;
