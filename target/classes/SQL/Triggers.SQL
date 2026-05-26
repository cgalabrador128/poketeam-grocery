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


DELIMITER //

drop trigger if exists after_inventory_update; //
create trigger after_inventory_update
    after update on inventory
    for each row
begin
    declare quantity_change int;

    -- case a: stock went up (addition)
    if new.product_stock_count > old.product_stock_count then
        set quantity_change = new.product_stock_count - old.product_stock_count;

    insert into archive (product_name, product_stock_count, product_price, total_added, total_removed, archive_date, archive_hour)
    values (new.product_name, new.product_stock_count, new.product_price, quantity_change, 0, curdate(), hour(now()))
        on duplicate key update
                             product_stock_count = new.product_stock_count,
                             product_price = new.product_price,
                             total_added = total_added + quantity_change;

    -- case b: stock went down (removal)
    elseif new.product_stock_count < old.product_stock_count then
        set quantity_change = old.product_stock_count - new.product_stock_count;

    insert into archive (product_name, product_stock_count, product_price, total_added, total_removed, archive_date, archive_hour)
    values (new.product_name, new.product_stock_count, new.product_price, 0, quantity_change, curdate(), hour(now()))
        on duplicate key update
                             product_stock_count = new.product_stock_count,
                             product_price = new.product_price,
                             total_removed = total_removed + quantity_change;
end if;
end; //


drop trigger if exists after_inventory_insert; //
create trigger after_inventory_insert
    after insert on inventory
    for each row
begin
    insert into archive (product_name, product_stock_count, product_price, total_added, total_removed, archive_date, archive_hour)
    values (new.product_name, new.product_stock_count, new.product_price, new.product_stock_count, 0, curdate(), hour(now()))
        on duplicate key update
                             product_stock_count = new.product_stock_count,
                             total_added = total_added + new.product_stock_count;
end; $$

DELIMITER ;
