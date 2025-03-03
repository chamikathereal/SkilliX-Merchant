package com.skillix.merchant.model;

public class Subscription {
    private String id;  // Unique identifier for the subscription
    private String name; // Subscription name (1 month, 2 months, 3 months)
    private String price; // Price of the subscription
    private String description; // Any description of the subscription

    // Constructor, getters and setters
    public Subscription(String id, String name, String price, String description) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

