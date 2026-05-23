package com.grocery.data;

public class Employee {
    private int id;
    private String name;
    private String role;
    private Boolean permit;

    public Employee(int id, String name, String role, Boolean permit) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.permit = permit;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public Boolean getPermit() {return permit;}
}