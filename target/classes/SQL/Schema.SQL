use grocery;

create table users (
    user_id int auto_increment primary key,
    user_name varchar(255) unique not null,
    user_password varchar(255) not null,
    user_role enum('manager', 'staff') not null,
    has_admin bool default false
);

create table inventory (
    product_serial_number bigint primary key,
    product_name varchar(255) unique not null,
    product_stock_count int not null default 0,
    product_batch_no int unique not null,
    product_expiry_date date not null,
    product_price double not null,
    low_stock_threshold int default 5
);

create table alert (
    alert_id int auto_increment primary key,
    product_serial_number bigint not null,
    alert_type enum('unregistered', 'low stock', 'flagged for expiry', 'expired') not null,
    alert_time timestamp default current_timestamp
);

create table archive (
    record_id int auto_increment primary key,
    product_name varchar(255) not null,
    product_stock_count int not null,
    product_price double not null,
    total_added int not null default 0,
    total_removed int not null default 0,
    archive_date DATE NOT NULL,
    archive_hour INT NOT NULL,
    archived_at timestamp default current_timestamp,

    unique key hourly_log (product_name, archive_date, archive_hour)
);
select * from users;
