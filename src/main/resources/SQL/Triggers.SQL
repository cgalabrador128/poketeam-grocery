DELIMITER $$

CREATE EVENT IF NOT EXISTS check_inventory_alerts
ON SCHEDULE EVERY 1 DAY
STARTS (CURRENT_DATE + INTERVAL 1 DAY)
DO
BEGIN
    -- 1. Expired Check
INSERT INTO alert (product_serial_number, alert_type, alert_time)
SELECT i.product_serial_number, 'expired', NOW()
FROM inventory i
WHERE i.product_expiry_date < CURDATE()
  AND NOT EXISTS (
    SELECT 1 FROM alert a
    WHERE a.product_serial_number = i.product_serial_number
      AND a.alert_type = 'expired'
);

-- 2. Low Stock Check
INSERT INTO alert (product_serial_number, alert_type, alert_time)
SELECT i.product_serial_number, 'low stock', NOW()
FROM inventory i
WHERE i.product_stock_count <= i.low_stock_threshold
  AND NOT EXISTS (
    SELECT 1 FROM alert a
    WHERE a.product_serial_number = i.product_serial_number
      AND a.alert_type = 'low stock'
);
END$$

DELIMITER ;