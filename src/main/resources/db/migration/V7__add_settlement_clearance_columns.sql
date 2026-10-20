ALTER TABLE monthly_hisab
    ADD COLUMN opening_balance DECIMAL(10, 2) NOT NULL DEFAULT 0.00 AFTER half_days,
    ADD COLUMN settlement_amount DECIMAL(10, 2) NULL AFTER final_money,
    ADD COLUMN carry_forward DECIMAL(10, 2) NOT NULL DEFAULT 0.00 AFTER settlement_amount;

-- Update existing settled hisabs so settlement_amount reflects final_money and carry_forward is 0
UPDATE monthly_hisab
SET settlement_amount = final_money, carry_forward = 0.00
WHERE status = 'SETTLED' AND settlement_amount IS NULL;
