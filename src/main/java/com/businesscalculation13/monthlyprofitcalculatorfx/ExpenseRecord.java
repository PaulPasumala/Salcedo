package com.businesscalculation13.monthlyprofitcalculatorfx;

import java.io.Serializable;

public class ExpenseRecord implements Serializable {
    // This ID helps Java ensure the saved file matches the class version
    private static final long serialVersionUID = 1L;

    private String week;
    private double amount;
    private String note;

    public ExpenseRecord(String week, double amount, String note) {
        this.week = week;
        this.amount = amount;
        this.note = note;
    }

    public String getWeek() { return week; }
    public double getAmount() { return amount; }
    public String getNote() { return note; }

    // Override toString for easy display in ListView
    @Override
    public String toString() {
        return week + " | -$" + String.format("%.2f", amount) + " (" + note + ")";
    }
}