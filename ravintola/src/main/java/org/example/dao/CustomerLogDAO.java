package org.example.dao;

import org.example.datasource.MariaDbConnection;
import org.example.model.CustomerLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) responsible for performing CRUD operations on the
 * {@code customer_logs} database table in MariaDB.
 * <p>
 * Provides persistence and retrieval logic for individual customer metrics
 * recorded during simulation runs, including entry/exit timestamps and total stay durations.
 * </p>
 *
 * @author Daniel
 * @version 2.0
 */
public class CustomerLogDAO {

    /**
     * Inserts a new customer log record into the database and assigns the auto-generated primary key ID back to the log object.
     *
     * @param log The {@link CustomerLog} entity containing individual customer timestamps and metrics.
     * @return The auto-generated {@code log_id} primary key if insertion succeeds; {@code -1} if an error occurs.
     */
    public int saveCustomerLog(CustomerLog log) {

        String sql = """
                INSERT INTO customer_logs
                (run_id, customer_id, customer_type,
                 arrival_time, exit_time, total_stay_time)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, log.getRunId());
            statement.setInt(2, log.getCustomerId());
            statement.setString(3, log.getCustomerType());
            statement.setDouble(4, log.getArrivalTime());
            statement.setDouble(5, log.getExitTime());
            statement.setDouble(6, log.getTotalStayTime());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    int logId = keys.getInt(1);
                    log.setLogId(logId);
                    return logId;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    /**
     * Retrieves all customer logs associated with a specific simulation run ID, ordered by customer ID.
     *
     * @param runId The unique identifier of the simulation run.
     * @return A {@link List} of {@link CustomerLog} entities corresponding to the given run ID; empty list if none found.
     */
    public List<CustomerLog> getLogsByRunId(int runId) {

        List<CustomerLog> logs = new ArrayList<>();

        String sql = """
                SELECT *
                FROM customer_logs
                WHERE run_id = ?
                ORDER BY customer_id
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, runId);

            try (ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    logs.add(mapResultSetToCustomerLog(result));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return logs;
    }


    /**
     * Retrieves a single customer log entry by its primary key ID.
     *
     * @param logId The unique primary key identifier of the customer log.
     * @return The matching {@link CustomerLog} object if found; {@code null} otherwise.
     */
    public CustomerLog getLogById(int logId) {

        String sql = """
                SELECT *
                FROM customer_logs
                WHERE log_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, logId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapResultSetToCustomerLog(result);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Maps the current row of an active SQL {@link ResultSet} to a new {@link CustomerLog} domain entity.
     *
     * @param result The active {@link ResultSet} positioned at the row to be mapped.
     * @return A fully populated {@link CustomerLog} instance.
     * @throws SQLException If an error occurs while accessing database column values.
     */
    private CustomerLog mapResultSetToCustomerLog(ResultSet result)
            throws SQLException {

        CustomerLog log = new CustomerLog();

        log.setLogId(result.getInt("log_id"));
        log.setRunId(result.getInt("run_id"));
        log.setCustomerId(result.getInt("customer_id"));
        log.setCustomerType(result.getString("customer_type"));
        log.setArrivalTime(result.getDouble("arrival_time"));
        log.setExitTime(result.getDouble("exit_time"));
        log.setTotalStayTime(result.getDouble("total_stay_time"));

        return log;
    }
}