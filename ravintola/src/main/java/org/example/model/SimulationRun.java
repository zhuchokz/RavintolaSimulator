package org.example.model;

public class SimulationRun {

    private int runId;
    private double simulationTime;
    private int totalCustomers;
    private double averageStayTime;
    private int takeawayCount;
    private int dineinCount;
    private int onlineCount;
    private double oven1Utilization;
    private double oven2Utilization;

    public SimulationRun() {
    }

    public SimulationRun(double simulationTime,
                         int totalCustomers,
                         double averageStayTime,
                         int takeawayCount,
                         int dineinCount,
                         int onlineCount,
                         double oven1Utilization,
                         double oven2Utilization) {

        this.simulationTime = simulationTime;
        this.totalCustomers = totalCustomers;
        this.averageStayTime = averageStayTime;
        this.takeawayCount = takeawayCount;
        this.dineinCount = dineinCount;
        this.onlineCount = onlineCount;
        this.oven1Utilization = oven1Utilization;
        this.oven2Utilization = oven2Utilization;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(double simulationTime) {
        this.simulationTime = simulationTime;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public double getAverageStayTime() {
        return averageStayTime;
    }

    public void setAverageStayTime(double averageStayTime) {
        this.averageStayTime = averageStayTime;
    }

    public int getTakeawayCount() {
        return takeawayCount;
    }

    public void setTakeawayCount(int takeawayCount) {
        this.takeawayCount = takeawayCount;
    }

    public int getDineinCount() {
        return dineinCount;
    }

    public void setDineinCount(int dineinCount) {
        this.dineinCount = dineinCount;
    }

    public int getOnlineCount() {
        return onlineCount;
    }

    public void setOnlineCount(int onlineCount) {
        this.onlineCount = onlineCount;
    }

    public double getOven1Utilization() {
        return oven1Utilization;
    }

    public void setOven1Utilization(double oven1Utilization) {
        this.oven1Utilization = oven1Utilization;
    }

    public double getOven2Utilization() {
        return oven2Utilization;
    }

    public void setOven2Utilization(double oven2Utilization) {
        this.oven2Utilization = oven2Utilization;
    }
}