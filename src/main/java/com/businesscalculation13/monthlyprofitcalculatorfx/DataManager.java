package com.businesscalculation13.monthlyprofitcalculatorfx;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DataManager {

    // --- AUTHENTICATION ---

    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a user is found
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean registerUser(String username, String password) {
        String query = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Likely duplicate username
            return false;
        }
    }

    // --- EXPENSES ---

    public void addExpense(ExpenseRecord expense) {
        String query = "INSERT INTO expenses (week, amount, note) VALUES (?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, expense.getWeek());
            stmt.setDouble(2, expense.getAmount());
            stmt.setString(3, expense.getNote());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ExpenseRecord> loadExpenses() {
        List<ExpenseRecord> list = new ArrayList<>();
        String query = "SELECT * FROM expenses";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new ExpenseRecord(
                        rs.getString("week"),
                        rs.getDouble("amount"),
                        rs.getString("note")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // --- SALES ---

    public void addSale(ProductSale sale) {
        String query = "INSERT INTO sales (product_name, unit_price, quantity, total_income) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sale.getName());
            stmt.setDouble(2, sale.getUnitPrice());
            stmt.setInt(3, sale.getQuantity());
            stmt.setDouble(4, sale.getIncome());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ProductSale> loadSales() {
        List<ProductSale> list = new ArrayList<>();
        String query = "SELECT * FROM sales";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new ProductSale(
                        rs.getString("product_name"),
                        rs.getDouble("unit_price"),
                        rs.getInt("quantity")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}