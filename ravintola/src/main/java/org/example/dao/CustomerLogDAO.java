package org.example.dao;

import org.example.datasource.MariaDbConnection;
import org.example.model.CustomerLog;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerLogDAO {

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