package org.example.dao;

import org.example.datasource.MariaDbConnection;
import org.example.model.SimulationRun;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) responsible for performing CRUD operations on the
 * {@code simulation_runs} database table in MariaDB.
 * <p>
 * Handles the persistence and retrieval of overall simulation run performance summary metrics,
 * including total customer counts, service durations, customer breakdown by type, and station utilization rates.
 * </p>
 *
 * @author Daniel
 * @version 2.0
 */
public class SimulationRunDAO {

    /**
     * Inserts a completed simulation run record into the database and binds the auto-generated primary key ID back to the object.
     *
     * @param run The {@link SimulationRun} entity containing summary statistics for a completed run.
     * @return The auto-generated {@code run_id} primary key if successful; {@code -1} if an error occurs.
     */
    public int createRun(SimulationRun run) {

        String sql = """
                INSERT INTO simulation_runs
                (simulation_time, total_customers, avg_stay_time,
                 takeaway_count, dinein_count, online_count,
                 oven1_utilization, oven2_utilization)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setDouble(1, run.getSimulationTime());
            statement.setInt(2, run.getTotalCustomers());
            statement.setDouble(3, run.getAverageStayTime());
            statement.setInt(4, run.getTakeawayCount());
            statement.setInt(5, run.getDineinCount());
            statement.setInt(6, run.getOnlineCount());
            statement.setDouble(7, run.getOven1Utilization());
            statement.setDouble(8, run.getOven2Utilization());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    int runId = keys.getInt(1);
                    run.setRunId(runId);
                    return runId;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    /**
     * Updates all summary metrics of an existing simulation run record in the database.
     *
     * @param run The {@link SimulationRun} instance containing updated values and a valid {@code runId}.
     */
    public void updateRun(SimulationRun run) {

        String sql = """
                UPDATE simulation_runs
                SET simulation_time = ?,
                    total_customers = ?,
                    avg_stay_time = ?,
                    takeaway_count = ?,
                    dinein_count = ?,
                    online_count = ?,
                    oven1_utilization = ?,
                    oven2_utilization = ?
                WHERE run_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDouble(1, run.getSimulationTime());
            statement.setInt(2, run.getTotalCustomers());
            statement.setDouble(3, run.getAverageStayTime());
            statement.setInt(4, run.getTakeawayCount());
            statement.setInt(5, run.getDineinCount());
            statement.setInt(6, run.getOnlineCount());
            statement.setDouble(7, run.getOven1Utilization());
            statement.setDouble(8, run.getOven2Utilization());
            statement.setInt(9, run.getRunId());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Retrieves a single simulation run record by its primary key ID.
     *
     * @param runId The unique primary key identifier of the simulation run.
     * @return The matching {@link SimulationRun} object if found; {@code null} otherwise.
     */
    public SimulationRun getRunById(int runId) {

        String sql = """
                SELECT *
                FROM simulation_runs
                WHERE run_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, runId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapResultSetToSimulationRun(result);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Retrieves all recorded simulation runs from the database, ordered in descending order by run ID (newest first).
     *
     * @return A {@link List} of all {@link SimulationRun} instances stored in MariaDB; empty list if none found.
     */
    public List<SimulationRun> getAllRuns() {

        List<SimulationRun> runs = new ArrayList<>();

        String sql = """
                SELECT *
                FROM simulation_runs
                ORDER BY run_id DESC
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                runs.add(mapResultSetToSimulationRun(result));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return runs;
    }


    /**
     * Permanently deletes a simulation run record from the database by its primary key ID.
     *
     * @param runId The unique primary key identifier of the simulation run to delete.
     */
    public void deleteRun(int runId) {

        String sql = """
                DELETE FROM simulation_runs
                WHERE run_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, runId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Maps the current row of an active SQL {@link ResultSet} to a new {@link SimulationRun} domain entity.
     *
     * @param result The active {@link ResultSet} positioned at the target row.
     * @return A fully populated {@link SimulationRun} instance.
     * @throws SQLException If an error occurs while reading database column values.
     */
    private SimulationRun mapResultSetToSimulationRun(ResultSet result)
            throws SQLException {

        SimulationRun run = new SimulationRun();

        run.setRunId(result.getInt("run_id"));
        run.setSimulationTime(result.getDouble("simulation_time"));
        run.setTotalCustomers(result.getInt("total_customers"));
        run.setAverageStayTime(result.getDouble("avg_stay_time"));
        run.setTakeawayCount(result.getInt("takeaway_count"));
        run.setDineinCount(result.getInt("dinein_count"));
        run.setOnlineCount(result.getInt("online_count"));
        run.setOven1Utilization(result.getDouble("oven1_utilization"));
        run.setOven2Utilization(result.getDouble("oven2_utilization"));

        return run;
    }
}