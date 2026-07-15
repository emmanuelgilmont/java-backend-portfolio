-- Trade settlements — EOD output table
-- Compatible with both H2 (local tests) and PostgreSQL (CI / production)
CREATE TABLE IF NOT EXISTS settlements (
    trade_id         VARCHAR(50)     NOT NULL,
    symbol           VARCHAR(50)     NOT NULL,
    side             VARCHAR(10)     NOT NULL,
    quantity         INT             NOT NULL,
    price            DECIMAL(18, 6)  NOT NULL,
    settlement_amount DECIMAL(18, 6) NOT NULL,
    currency         VARCHAR(3)      NOT NULL,
    trade_date       DATE            NOT NULL,
    settlement_date  DATE            NOT NULL,
    status           VARCHAR(20)     NOT NULL,
    processed_at     TIMESTAMP       NOT NULL,
    CONSTRAINT pk_settlements PRIMARY KEY (trade_id)
);