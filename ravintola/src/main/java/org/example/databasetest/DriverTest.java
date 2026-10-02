package org.example.databasetest;

public class DriverTest {

    public static void main(String[] args) {

        try {
            Class.forName("org.mariadb.jdbc.Driver");

            System.out.println("MariaDB JDBC driver is available!");

        } catch (ClassNotFoundException e) {

            System.out.println("MariaDB JDBC driver is NOT available!");
            e.printStackTrace();
        }
    }
}