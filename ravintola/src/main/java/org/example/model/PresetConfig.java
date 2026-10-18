package org.example.model;


/**
 * Model entity representing a simulation parameter configuration preset stored in the {@code preset_configs} database table.
 * <p>
 * Encapsulates setup parameters including customer arrival mean, station resource capacities (ovens, dining tables, couriers),
 * ordering probabilities, and average service durations across service points.
 * </p>
 *
 * @author Daniel
 * @version 1.0
 */
public class PresetConfig {

    private int configId;
    private String configName;
    private double arrivalMean;
    private int oven1Capacity;
    private int oven2Capacity;
    private int tableCapacity;
    private int courierCapacity;
    private double drinkProbability;
    private double oven1ServiceTime;
    private double oven2ServiceTime;
    private double tableServiceTime;
    private double courierServiceTime;

    /**
     * Default no-argument constructor for Object-Relational Mapping (ORM) and manual instantiation.
     */
    public PresetConfig() {
    }

    /**
     * Parameterized constructor for creating a new preset configuration entity prior to database insertion.
     *
     * @param configName         Unique descriptive label for the preset.
     * @param arrivalMean        Mean inter-arrival time for arriving customers.
     * @param oven1Capacity      Maximum parallel capacity for Oven 1.
     * @param oven2Capacity      Maximum parallel capacity for Oven 2.
     * @param tableCapacity      Maximum seating capacity for dining tables.
     * @param courierCapacity    Maximum active couriers for delivery.
     * @param drinkProbability   Probability of a customer purchasing a drink (0.0 to 1.0).
     * @param oven1ServiceTime   Mean service duration for Oven 1.
     * @param oven2ServiceTime   Mean service duration for Oven 2.
     * @param tableServiceTime   Mean service duration for dining tables.
     * @param courierServiceTime Mean service duration for delivery couriers.
     */
    public PresetConfig(String configName,
                        double arrivalMean,
                        int oven1Capacity,
                        int oven2Capacity,
                        int tableCapacity,
                        int courierCapacity,
                        double drinkProbability,
                        double oven1ServiceTime,
                        double oven2ServiceTime,
                        double tableServiceTime,
                        double courierServiceTime) {

        this.configName = configName;
        this.arrivalMean = arrivalMean;
        this.oven1Capacity = oven1Capacity;
        this.oven2Capacity = oven2Capacity;
        this.tableCapacity = tableCapacity;
        this.courierCapacity = courierCapacity;
        this.drinkProbability = drinkProbability;
        this.oven1ServiceTime = oven1ServiceTime;
        this.oven2ServiceTime = oven2ServiceTime;
        this.tableServiceTime = tableServiceTime;
        this.courierServiceTime = courierServiceTime;
    }

    /**
     * Pre-configured default parameter preset instance for quick initialization of the pizzeria simulation.
     */
    public static final PresetConfig DEFAULT = new PresetConfig(
            "Default",
            5.0,   // arrival mean (Normal 5.0s)
            4,     // oven 1 cap
            4,     // oven 2 cap
            10,    // table cap
            3,     // courier cap
            0.50,  // drink prob
            10.0,  // oven 1 time
            10.0,  // oven 2 time
            15.0,  // table time
            15.0   // courier time
    );

    public int getConfigId() {
        return configId;
    }

    public void setConfigId(int configId) {
        this.configId = configId;
    }

    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public double getArrivalMean() {
        return arrivalMean;
    }

    public void setArrivalMean(double arrivalMean) {
        this.arrivalMean = arrivalMean;
    }

    public int getOven1Capacity() {
        return oven1Capacity;
    }

    public void setOven1Capacity(int oven1Capacity) {
        this.oven1Capacity = oven1Capacity;
    }

    public int getOven2Capacity() {
        return oven2Capacity;
    }

    public void setOven2Capacity(int oven2Capacity) {
        this.oven2Capacity = oven2Capacity;
    }

    public int getTableCapacity() {
        return tableCapacity;
    }

    public void setTableCapacity(int tableCapacity) {
        this.tableCapacity = tableCapacity;
    }

    public int getCourierCapacity() {
        return courierCapacity;
    }

    public void setCourierCapacity(int courierCapacity) {
        this.courierCapacity = courierCapacity;
    }

    public double getDrinkProbability() {
        return drinkProbability;
    }

    public void setDrinkProbability(double drinkProbability) {
        this.drinkProbability = drinkProbability;
    }

    public double getOven1ServiceTime() { return oven1ServiceTime; }

    public double getOven2ServiceTime() { return oven2ServiceTime; }

    public double getTableServiceTime() { return tableServiceTime; }

    public double getCourierServiceTime() { return courierServiceTime; }

    public void setOven1ServiceTime(double oven1ServiceTime) {
        this.oven1ServiceTime = oven1ServiceTime;
    }

    public void setOven2ServiceTime(double oven2ServiceTime) {
        this.oven2ServiceTime = oven2ServiceTime;
    }

    public void setTableServiceTime(double tableServiceTime) {
        this.tableServiceTime = tableServiceTime;
    }

    public void setCourierServiceTime(double courierServiceTime) {
        this.courierServiceTime = courierServiceTime;
    }
}