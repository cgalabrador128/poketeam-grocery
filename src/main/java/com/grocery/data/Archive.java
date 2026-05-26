package com.grocery.data;

import java.time.LocalDate;


public class Archive {
    private String name;
    private int count;
    private double price;
    private int added;
    private int removed;

    public String getName() {
        return name;
    }

    public int getCount() {
        return count;
    }

    public double getPrice() {
        return price;
    }

    public int getAdded() {
        return added;
    }

    public int getRemoved() {
        return removed;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getHour() {
        return hour;
    }

    private LocalDate date;
    private int hour;

    public Archive(int hour, LocalDate date, int removed, int added, double price, int count, String name) {
        this.date = date;
        this.hour = hour;
        this.name = name;
        this.count = count;
        this.price = price;
        this.added = added;
        this.removed = removed;
    }
}
