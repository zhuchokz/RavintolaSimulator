package org.example.dao;

import org.example.datasource.MariaDbConnection;
import org.example.model.PresetConfig;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PresetConfigDAO {

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