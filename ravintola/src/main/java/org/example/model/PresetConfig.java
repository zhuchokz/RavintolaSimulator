package org.example.model;

public class PresetConfig {

    private int configId;
    private String configName;
    private double arrivalMean;
    private int oven1Capacity;
    private int oven2Capacity;
    private int tableCapacity;
    private int courierCapacity;
    private double drinkProbability;

    public PresetConfig() {
    }

    public PresetConfig(String configName,
                        double arrivalMean,
                        int oven1Capacity,
                        int oven2Capacity,
                        int tableCapacity,
                        int courierCapacity,
                        double drinkProbability) {

        this.configName = configName;
        this.arrivalMean = arrivalMean;
        this.oven1Capacity = oven1Capacity;
        this.oven2Capacity = oven2Capacity;
        this.tableCapacity = tableCapacity;
        this.courierCapacity = courierCapacity;
        this.drinkProbability = drinkProbability;
    }

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
}