package com.grocery.data;

import java.time.LocalDate;

public class Product {
    private long id;
    private String itemName;
    private int batchNum;
    private double price;
    private LocalDate expiryDate;
    private int noOfStock;
    public boolean isModified;



    public Product(long id, String itemName, int batchNum, double price, LocalDate expiryDate, int noOfStock) {
        this.id = id;
        this.itemName = itemName;
        this.batchNum = batchNum;
        this.price = price;
        this.expiryDate = expiryDate;
        this.noOfStock = noOfStock;
        this.isModified = false;
    }

    // Getters
    public long getId() { return id; }
    public String getItemName() { return itemName; }
    public int getBatchNum() { return batchNum; }
    public double getPrice() { return price; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public int getNoOfStock() { return noOfStock; }
    public boolean isModified() {return this.isModified;}

    //setters
    public void setModified(boolean modified){this.isModified = modified;}
    public void setId(long id) {this.id = id;}
    public void setItemName(String itemName) {this.itemName = itemName;}
    public void setBatchNum(int batchNum) {this.batchNum = batchNum;}
    public void setPrice(double price) {this.price = price;}
    public void setExpiryDate(LocalDate expiryDate) {this.expiryDate = expiryDate;}
    public void setNoOfStock(int noOfStock) {this.noOfStock = noOfStock;}

}