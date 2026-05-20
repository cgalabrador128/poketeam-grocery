package com.grocery.util;

import java.time.LocalDate;

public class Product {
    private int id;
    private String itemName;
    private String batchNum;
    private double price;
    private LocalDate expiryDate;
    private int noOfStock;

    public Product(int id, String itemName, String batchNum, double price, LocalDate expiryDate, int noOfStock) {
        this.id = id;
        this.itemName = itemName;
        this.batchNum = batchNum;
        this.price = price;
        this.expiryDate = expiryDate;
        this.noOfStock = noOfStock;
    }

    // Getters
    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public String getBatchNum() { return batchNum; }
    public double getPrice() { return price; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getNoOfStock() { return noOfStock; }
}