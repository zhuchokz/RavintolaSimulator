package org.example.model;

public class CustomerLog {

    private int logId;
    private int runId;
    private int customerId;
    private String customerType;
    private double arrivalTime;
    private double exitTime;
    private double totalStayTime;

    public CustomerLog() {
    }

    public CustomerLog(int runId,
                       int customerId,
                       String customerType,
                       double arrivalTime,
                       double exitTime,
                       double totalStayTime) {

        this.runId = runId;
        this.customerId = customerId;
        this.customerType = customerType;
        this.arrivalTime = arrivalTime;
        this.exitTime = exitTime;
        this.totalStayTime = totalStayTime;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public int getRunId() {
        return runId;
    }

    public void setRunId(int runId) {
        this.runId = runId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getExitTime() {
        return exitTime;
    }

    public void setExitTime(double exitTime) {
        this.exitTime = exitTime;
    }

    public double getTotalStayTime() {
        return totalStayTime;
    }

    public void setTotalStayTime(double totalStayTime) {
        this.totalStayTime = totalStayTime;
    }
}