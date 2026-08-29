package com.creightoncornelison.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    public Database() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/orchard";
        String username = "orchard";
        String password = "orchard";
        Connection connection =
                DriverManager.getConnection(url, username, password);

        // SELECT 1; and print result
        Statement statement = connection.createStatement();
        System.out.println("Database connected: " + statement.execute("SELECT 1"));

        connection.close();
    }
}
