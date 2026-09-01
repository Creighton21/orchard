package com.creightoncornelison.persistence;

import org.flywaydb.core.Flyway;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    public Database() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/orchard";
        String username = "orchard";
        String password = "orchard";

        // 1. Run Flyway migration
        Flyway flyway = Flyway.configure()
                .dataSource(url, username, password)
                .load();
        flyway.migrate();

        // 2. Open standard connection
        Connection connection = DriverManager.getConnection(url, username, password);

        Statement statement = connection.createStatement();
        System.out.println("Database connected: " + statement.execute("SELECT 1"));

        connection.close();
    }
}
