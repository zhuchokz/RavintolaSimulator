package org.example.databasetest;

import org.example.datasource.MariaDbConnection;

import java.sql.Connection;

public class DatabaseTest {

    public static void main(String[] args) {

        try {
            Connection connection = MariaDbConnection.getConnection();

            System.out.println("Database connection SUCCESSFUL!");

            connection.close();

        } catch (Exception e) {
            System.out.println("Database connection FAILED!");
            e.printStackTrace();
        }
    }
}