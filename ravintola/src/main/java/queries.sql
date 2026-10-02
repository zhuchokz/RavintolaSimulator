CREATE DATABASE IF NOT EXISTS pizzeria_db;
USE pizzeria_db;

-- Table 1: Overall Run Summaries
CREATE TABLE IF NOT EXISTS simulation_runs (
                                               run_id INT AUTO_INCREMENT PRIMARY KEY,
                                               simulation_time DOUBLE NOT NULL,
                                               total_customers INT NOT NULL,
                                               avg_stay_time DOUBLE NOT NULL,
                                               takeaway_count INT DEFAULT 0,
                                               dinein_count INT DEFAULT 0,
                                               online_count INT DEFAULT 0,
                                               oven1_utilization DOUBLE,
                                               oven2_utilization DOUBLE
);

-- Table 2: Saved Input Configurations
CREATE TABLE IF NOT EXISTS preset_configs (
                                              config_id INT AUTO_INCREMENT PRIMARY KEY,
                                              config_name VARCHAR(100) NOT NULL UNIQUE,
    arrival_mean DOUBLE NOT NULL,
    oven1_capacity INT NOT NULL DEFAULT 4,
    oven2_capacity INT NOT NULL DEFAULT 4,
    table_capacity INT NOT NULL DEFAULT 10,
    courier_capacity INT NOT NULL DEFAULT 3,
    drink_probability DOUBLE NOT NULL DEFAULT 0.50
    );

-- Table 3: Detailed Per-Customer Records
CREATE TABLE IF NOT EXISTS customer_logs (
                                             log_id INT AUTO_INCREMENT PRIMARY KEY,
                                             run_id INT NOT NULL,
                                             customer_id INT NOT NULL,
                                             customer_type VARCHAR(20) NOT NULL,
    arrival_time DOUBLE NOT NULL,
    exit_time DOUBLE NOT NULL,
    total_stay_time DOUBLE NOT NULL,
    FOREIGN KEY (run_id) REFERENCES simulation_runs(run_id) ON DELETE CASCADE
    );