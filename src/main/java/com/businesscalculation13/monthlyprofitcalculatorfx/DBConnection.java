package com.businesscalculation13.monthlyprofitcalculatorfx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // Default XAMPP credentials
    private static final String URL = "jdbc:mysql://localhost:3306/retro_store";
    private static final String USER = "root";
    private static final String PASS = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}