package com.grocery.data;

import java.time.LocalDate;

public class Product {
    private long id;
    private String itemName;
    private int batchNum;
    private double price;
    private LocalDate expiryDate;
    private int noOfStock;

    public Product(long id, String itemName, int batchNum, double price, LocalDate expiryDate, int noOfStock) {
        this.id = id;
        this.itemName = itemName;
        this.batchNum = batchNum;
        this.price = price;
        this.expiryDate = expiryDate;
        this.noOfStock = noOfStock;
    }

    // Getters
    public long getId() { return id; }
    public String getItemName() { return itemName; }
    public int getBatchNum() { return batchNum; }
    public double getPrice() { return price; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getNoOfStock() { return noOfStock; }
}