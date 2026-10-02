package org.example.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MariaDbConnection {

    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.mariadb.jdbc.Driver");
            System.out.println("MariaDB driver found!");
        } catch (ClassNotFoundException e) {
            System.out.println("MariaDB driver NOT found!");
            e.printStackTrace();
            throw new SQLException("MariaDB JDBC driver is missing from the classpath.", e);
        }

        return DriverManager.getConnection(
                "jdbc:mariadb://localhost:3306/pizzeria_db",
                "appuser",
                "password"
        );
    }
}