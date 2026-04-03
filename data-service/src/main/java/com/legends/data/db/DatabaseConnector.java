package com.legends.data.db;

import java.sql.*;

public class DatabaseConnector {
    private static final String URL  = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://mysql:3306/losw");
    private static final String USER = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASS = System.getenv().getOrDefault("DB_PASSWORD", "root");
    private static DatabaseConnector instance;

    private DatabaseConnector() {}
    public static DatabaseConnector getInstance() {
        if (instance==null) instance=new DatabaseConnector(); return instance;
    }
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
