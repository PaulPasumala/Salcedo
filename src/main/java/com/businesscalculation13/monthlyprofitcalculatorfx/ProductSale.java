package com.businesscalculation13.monthlyprofitcalculatorfx;

import java.io.Serializable;

public class ProductSale implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private double unitPrice;
    private int quantity;

    public ProductSale(String name, double unitPrice, int quantity) {
        this.name = name;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public String getName() { return name; }
    public double getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }

    public double getIncome() {
        return unitPrice * quantity;
    }
}