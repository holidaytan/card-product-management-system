-- Fictional demonstration pricing only. These values do not represent real card products or rates.
-- Each row is identified by the identity value and original V2 seed values so user-created rows are not updated.
UPDATE dbo.card_products
SET annual_fee = CASE id
        WHEN 1 THEN 80.00
        WHEN 2 THEN 150.00
        WHEN 3 THEN 300.00
        WHEN 4 THEN 800.00
        WHEN 5 THEN 450.00
        WHEN 6 THEN 120.00
        WHEN 7 THEN 180.00
        WHEN 8 THEN 250.00
        WHEN 9 THEN 100.00
        WHEN 10 THEN 100.00
        WHEN 11 THEN 90.00
        WHEN 12 THEN 0.00
        WHEN 13 THEN 350.00
        WHEN 14 THEN 500.00
        WHEN 15 THEN 50.00
        WHEN 16 THEN 0.00
        WHEN 17 THEN 10.00
        WHEN 18 THEN 30.00
        WHEN 19 THEN 0.00
        WHEN 20 THEN 0.00
        WHEN 21 THEN 0.00
        WHEN 22 THEN 0.00
        WHEN 23 THEN 0.00
        WHEN 24 THEN 100.00
        WHEN 25 THEN 20.00
        WHEN 26 THEN 0.00
        WHEN 27 THEN 0.00
        WHEN 28 THEN 0.00
        WHEN 29 THEN 0.00
        WHEN 30 THEN 0.00
    END,
    annual_interest_rate = CASE id
        WHEN 1 THEN 15.00
        WHEN 2 THEN 15.50
        WHEN 3 THEN 16.00
        WHEN 4 THEN 17.00
        WHEN 5 THEN 16.50
        WHEN 6 THEN 15.00
        WHEN 7 THEN 15.50
        WHEN 8 THEN 16.00
        WHEN 9 THEN 15.00
        WHEN 10 THEN 15.00
        WHEN 11 THEN 15.00
        WHEN 12 THEN 12.00
        WHEN 13 THEN 17.00
        WHEN 14 THEN 18.00
        WHEN 15 THEN 18.00
        ELSE NULL
    END
WHERE ((id = 1 AND product_name = N'Classic Credit Card' AND card_type = 'CREDIT')
   OR (id = 2 AND product_name = N'Gold Credit Card' AND card_type = 'CREDIT')
   OR (id = 3 AND product_name = N'Platinum Credit Card' AND card_type = 'CREDIT')
   OR (id = 4 AND product_name = N'Infinite Credit Card' AND card_type = 'CREDIT')
   OR (id = 5 AND product_name = N'Signature Credit Card' AND card_type = 'CREDIT')
   OR (id = 6 AND product_name = N'Cashback Credit Card' AND card_type = 'CREDIT')
   OR (id = 7 AND product_name = N'Rewards Credit Card' AND card_type = 'CREDIT')
   OR (id = 8 AND product_name = N'Travel Credit Card' AND card_type = 'CREDIT')
   OR (id = 9 AND product_name = N'Dining Credit Card' AND card_type = 'CREDIT')
   OR (id = 10 AND product_name = N'Online Shopping Credit Card' AND card_type = 'CREDIT')
   OR (id = 11 AND product_name = N'Petrol Credit Card' AND card_type = 'CREDIT')
   OR (id = 12 AND product_name = N'Student Credit Card' AND card_type = 'CREDIT')
   OR (id = 13 AND product_name = N'Business Credit Card' AND card_type = 'CREDIT')
   OR (id = 14 AND product_name = N'Corporate Credit Card' AND card_type = 'CREDIT')
   OR (id = 15 AND product_name = N'Secured Credit Card' AND card_type = 'CREDIT')
   OR (id = 16 AND product_name = N'Classic Debit Card' AND card_type = 'DEBIT')
   OR (id = 17 AND product_name = N'Gold Debit Card' AND card_type = 'DEBIT')
   OR (id = 18 AND product_name = N'Premium Debit Card' AND card_type = 'DEBIT')
   OR (id = 19 AND product_name = N'Contactless Debit Card' AND card_type = 'DEBIT')
   OR (id = 20 AND product_name = N'Virtual Debit Card' AND card_type = 'DEBIT')
   OR (id = 21 AND product_name = N'Youth Debit Card' AND card_type = 'DEBIT')
   OR (id = 22 AND product_name = N'Salary Debit Card' AND card_type = 'DEBIT')
   OR (id = 23 AND product_name = N'Payroll Debit Card' AND card_type = 'DEBIT')
   OR (id = 24 AND product_name = N'Business Debit Card' AND card_type = 'DEBIT')
   OR (id = 25 AND product_name = N'International Debit Card' AND card_type = 'DEBIT')
   OR (id = 26 AND product_name = N'Prepaid Debit Card' AND card_type = 'DEBIT')
   OR (id = 27 AND product_name = N'Campus Debit Card' AND card_type = 'DEBIT')
   OR (id = 28 AND product_name = N'Everyday Debit Card' AND card_type = 'DEBIT')
   OR (id = 29 AND product_name = N'Family Debit Card' AND card_type = 'DEBIT')
   OR (id = 30 AND product_name = N'Basic Debit Card' AND card_type = 'DEBIT'))
  AND annual_fee = 0.00
  AND annual_interest_rate IS NULL
  AND currency_code = 'MYR';
