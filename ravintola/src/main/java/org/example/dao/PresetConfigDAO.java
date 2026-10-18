package org.example.dao;

import org.example.datasource.MariaDbConnection;
import org.example.model.PresetConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Data Access Object (DAO) responsible for performing CRUD operations on the
 * {@code preset_configs} database table in MariaDB.
 * <p>
 * Allows saving, retrieving, updating, and deleting pre-configured simulation parameters
 * (station capacities, mean arrival rates, and service durations).
 * </p>
 *
 * @author Daniel
 * @version 2.0
 */
public class PresetConfigDAO {

    /**
     * Inserts a new simulation parameter preset into the database and sets the generated primary key ID.
     *
     * @param config The {@link PresetConfig} object containing the input configuration parameters.
     * @return The auto-generated {@code config_id} primary key if successful; {@code -1} if saving fails.
     */
    public int savePreset(PresetConfig config) {

        String sql = "INSERT INTO preset_configs (config_name, arrival_mean, oven1_capacity, oven2_capacity, " +
                "table_capacity, courier_capacity, drink_probability, " +
                "oven1_service_time, oven2_service_time, table_service_time, courier_service_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, config.getConfigName());
            statement.setDouble(2, config.getArrivalMean());
            statement.setInt(3, config.getOven1Capacity());
            statement.setInt(4, config.getOven2Capacity());
            statement.setInt(5, config.getTableCapacity());
            statement.setInt(6, config.getCourierCapacity());
            statement.setDouble(7, config.getDrinkProbability());
            statement.setDouble(8, config.getOven1ServiceTime());
            statement.setDouble(9, config.getOven2ServiceTime());
            statement.setDouble(10, config.getTableServiceTime());
            statement.setDouble(11, config.getCourierServiceTime());

            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {

                if (keys.next()) {
                    int configId = keys.getInt(1);
                    config.setConfigId(configId);
                    return configId;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }


    /**
     * Retrieves all saved simulation parameter presets from the database, sorted alphabetically by configuration name.
     *
     * @return A {@link List} of all {@link PresetConfig} instances stored in MariaDB; empty list if none found.
     */
    public List<PresetConfig> getAllPresets() {

        List<PresetConfig> configs = new ArrayList<>();

        String sql = """
                SELECT *
                FROM preset_configs
                ORDER BY config_name
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                configs.add(mapResultSetToPresetConfig(result));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return configs;
    }


    /**
     * Retrieves a specific configuration preset by its primary key ID.
     *
     * @param configId The unique primary key identifier of the preset.
     * @return The matching {@link PresetConfig} object if found; {@code null} otherwise.
     */
    public PresetConfig getPresetById(int configId) {

        String sql = """
                SELECT *
                FROM preset_configs
                WHERE config_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, configId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapResultSetToPresetConfig(result);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }


    /**
     * Updates all parameter values of an existing configuration preset record in the database.
     *
     * @param config The {@link PresetConfig} entity containing updated values and a valid {@code configId}.
     */
    public void updatePreset(PresetConfig config) {

        String sql = """
                UPDATE preset_configs
                SET config_name = ?,
                    arrival_mean = ?,
                    oven1_capacity = ?,
                    oven2_capacity = ?,
                    table_capacity = ?,
                    courier_capacity = ?,
                    drink_probability = ?,
                    oven1_service_time = ?,
                    oven2_service_time = ?,
                    table_service_time = ?,
                    courier_service_time = ?
                WHERE config_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, config.getConfigName());
            statement.setDouble(2, config.getArrivalMean());
            statement.setInt(3, config.getOven1Capacity());
            statement.setInt(4, config.getOven2Capacity());
            statement.setInt(5, config.getTableCapacity());
            statement.setInt(6, config.getCourierCapacity());
            statement.setDouble(7, config.getDrinkProbability());
            statement.setDouble(8, config.getOven1ServiceTime());
            statement.setDouble(9, config.getOven2ServiceTime());
            statement.setDouble(10, config.getTableServiceTime());
            statement.setDouble(11, config.getCourierServiceTime());
            statement.setInt(12, config.getConfigId());

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Permanently removes a preset configuration record from the database by its primary key ID.
     *
     * @param configId The unique primary key identifier of the preset to delete.
     */
    public void deletePreset(int configId) {

        String sql = """
                DELETE FROM preset_configs
                WHERE config_id = ?
                """;

        try (Connection connection = MariaDbConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, configId);
            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    /**
     * Maps the current row of an active SQL {@link ResultSet} to a new {@link PresetConfig} domain entity.
     *
     * @param result The active {@link ResultSet} positioned at the row to be mapped.
     * @return A fully populated {@link PresetConfig} instance.
     * @throws SQLException If an error occurs while reading column data from the result set.
     */
    private PresetConfig mapResultSetToPresetConfig(ResultSet result)
            throws SQLException {

        PresetConfig config = new PresetConfig();

        config.setConfigId(result.getInt("config_id"));
        config.setConfigName(result.getString("config_name"));
        config.setArrivalMean(result.getDouble("arrival_mean"));
        config.setOven1Capacity(result.getInt("oven1_capacity"));
        config.setOven2Capacity(result.getInt("oven2_capacity"));
        config.setTableCapacity(result.getInt("table_capacity"));
        config.setCourierCapacity(result.getInt("courier_capacity"));
        config.setDrinkProbability(result.getDouble("drink_probability"));
        config.setOven1ServiceTime(result.getDouble("oven1_service_time"));
        config.setOven2ServiceTime(result.getDouble("oven2_service_time"));
        config.setTableServiceTime(result.getDouble("table_service_time"));
        config.setCourierServiceTime(result.getDouble("courier_service_time"));

        return config;
    }
}