package org.example.model;

/**
 * Model entity representing an individual customer log record stored in the {@code customer_logs} database table.
 * <p>
 * Encapsulates fine-grained timing metrics for a single customer within a simulation run, including arrival time,
 * exit time, total duration spent in the system, and customer category type.
 * </p>
 *
 * @author Daniel
 * @version 1.0
 */
public class CustomerLog {

    private int logId;
    private int runId;
    private int customerId;
    private String customerType;
    private double arrivalTime;
    private double exitTime;
    private double totalStayTime;


    /**
     * Default no-argument constructor for Object-Relational Mapping (ORM) and manual instantiation.
     */
    public CustomerLog() {
    }


    /**
     * Parameterized constructor for instantiating a new customer log record prior to database insertion.
     *
     * @param runId         Foreign key linking to the parent simulation run.
     * @param customerId    Unique customer identifier.
     * @param customerType  Category string representing customer type.
     * @param arrivalTime   Timestamp when customer entered the system.
     * @param exitTime      Timestamp when customer departed.
     * @param totalStayTime Total duration spent in the pizzeria.
     */
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