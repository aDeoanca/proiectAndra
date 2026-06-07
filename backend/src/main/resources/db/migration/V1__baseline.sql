CREATE TABLE users (
    id    BIGSERIAL    PRIMARY KEY,
    name  VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    role  VARCHAR(50)  NOT NULL
);

CREATE TABLE purchase_orders (
    id          BIGSERIAL      PRIMARY KEY,
    title       VARCHAR(255)   NOT NULL,
    description TEXT,
    amount      NUMERIC(12, 2) NOT NULL CHECK (amount > 0),
    currency    VARCHAR(10)    NOT NULL DEFAULT 'USD',
    category    VARCHAR(50)    NOT NULL,
    status      VARCHAR(50)    NOT NULL,
    creator_id  BIGINT         NOT NULL REFERENCES users (id),
    created_at  TIMESTAMP      NOT NULL,
    updated_at  TIMESTAMP      NOT NULL
);

CREATE TABLE po_history (
    id          BIGSERIAL   PRIMARY KEY,
    po_id       BIGINT      NOT NULL REFERENCES purchase_orders (id),
    actor_id    BIGINT      NOT NULL REFERENCES users (id),
    action      VARCHAR(50) NOT NULL,
    from_status VARCHAR(50),
    to_status   VARCHAR(50) NOT NULL,
    comment     TEXT,
    created_at  TIMESTAMP   NOT NULL
);

CREATE INDEX idx_purchase_orders_status ON purchase_orders (status);
CREATE INDEX idx_po_history_po_id ON po_history (po_id);
