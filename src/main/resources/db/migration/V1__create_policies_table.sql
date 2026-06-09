-- V1__create_policies_table.sql
-- Compatible with MySQL 8+ and H2 (MODE=MySQL)

CREATE SCHEMA IF NOT EXISTS my_sql;

CREATE TABLE IF NOT EXISTS my_sql.policies (
    id                VARCHAR(36)    NOT NULL,
    policy_number     VARCHAR(20)    NOT NULL,
    policyholder_name VARCHAR(200)   NOT NULL,
    line_of_business  VARCHAR(20)    NOT NULL,
    status            VARCHAR(20)    NOT NULL,
    premium_amount    DECIMAL(15,2)  NOT NULL,
    currency          VARCHAR(3)     NOT NULL,
    effective_date    DATE           NOT NULL,
    expiry_date       DATE           NOT NULL,
    region            VARCHAR(50)    NOT NULL,
    underwriter       VARCHAR(100)   NOT NULL,
    flagged_for_review BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at        TIMESTAMP      NOT NULL,
    updated_at        TIMESTAMP      NOT NULL,

    CONSTRAINT pk_policies PRIMARY KEY (id),
    CONSTRAINT uq_policy_number UNIQUE (policy_number),
    CONSTRAINT chk_status CHECK (status IN ('Active','Expired','Pending','Cancelled')),
    CONSTRAINT chk_lob CHECK (line_of_business IN ('Property','Casualty','ANH','Marine')),
    CONSTRAINT chk_premium CHECK (premium_amount > 0),
    CONSTRAINT chk_dates CHECK (expiry_date > effective_date)
);

CREATE INDEX IF NOT EXISTS idx_policies_status        ON my_sql.policies (status);
CREATE INDEX IF NOT EXISTS idx_policies_lob           ON my_sql.policies (line_of_business);
CREATE INDEX IF NOT EXISTS idx_policies_region        ON my_sql.policies (region);
CREATE INDEX IF NOT EXISTS idx_policies_effective     ON my_sql.policies (effective_date);
CREATE INDEX IF NOT EXISTS idx_policies_expiry        ON my_sql.policies (expiry_date);
CREATE INDEX IF NOT EXISTS idx_policies_flagged       ON my_sql.policies (flagged_for_review);
