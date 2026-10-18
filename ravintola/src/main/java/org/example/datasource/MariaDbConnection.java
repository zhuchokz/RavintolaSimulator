package org.example.datasource;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


/**
 * Utility class providing static access to MariaDB database connections using JDBC.
 * <p>
 * Manages database driver instantiation and connection string parameters for communicating
 * with the local {@code pizzeria_db} database instance.
 * </p>
 *
 * @author Daniel
 * @version 1.0
 */
public class MariaDbConnection {

    /**
     * Establishes and returns an active {@link Connection} to the local MariaDB database.
     * <p>
     * Dynamically verifies the presence of the {@code org.mariadb.jdbc.Driver} class before attempting
     * connection via {@link DriverManager}.
     * </p>
     *
     * @return An active database {@link Connection} instance.
     * @throws SQLException If the driver class is missing or a database access error occurs.
     */
    public static Connection getConnection() throws SQLException {

        try {
            Class.forName("org.mariadb.jdbc.Driver");
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