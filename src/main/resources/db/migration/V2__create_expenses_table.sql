CREATE TABLE expenses (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    amount      DECIMAL(10,2) NOT NULL,
    category    VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    date        DATE NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_expenses_user_id ON expenses (user_id);
CREATE INDEX idx_expenses_date ON expenses (date);
CREATE INDEX idx_expenses_category ON expenses (category);
CREATE INDEX idx_expenses_user_date ON expenses (user_id, date);
