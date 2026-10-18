package org.example.simu.model;

import org.example.controller.IControllerMtoV;
import org.example.eduni.distributions.Negexp;
import org.example.eduni.distributions.Normal;
import org.example.simu.framework.ArrivalProcess;
import org.example.simu.framework.Clock;
import org.example.simu.framework.Engine;
import org.example.simu.framework.Event;
import org.example.model.CustomerLog;
import java.util.ArrayList;
import java.util.List;


/**
 * Main simulation engine implementation extending the discrete-event {@link Engine} framework.
 * <p>
 * Coordinates workstation service points, manages customer routing paths based on ordering channel types
 * (dine-in, takeaway, online), processes discrete state transitions, controls execution flow (pause, resume, stop),
 * and compiles final performance statistics for GUI visualization and database storage.
 * </p>
 *
 * @author Illia, Niklas
 * @version 1.0
 */
public class MyEngine extends Engine {
    /** Stochastic arrival process responsible for generating customer arrivals into the pizzeria. */
    private ArrivalProcess arrivalProcess;

    /** Service point workstation handling order placement. */
    private ServicePoint orderPoint;
    /** Service point workstation handling customer payment. */
    private ServicePoint payPoint;
    /** Service point workstation representing in-house dining tables. */
    private ServicePoint tablePoint;
    /** Service point workstation handling drink retrieval. */
    private ServicePoint drinkPoint;
    /** Primary pizza baking oven service point. */
    private ServicePoint oven1;
    /** Secondary pizza baking oven service point. */
    private ServicePoint oven2;
    /** Service point workstation handling order pickup. */
    private ServicePoint pickupPoint;
    /** Service point workstation handling courier dispatch for delivery. */
    private ServicePoint courierPoint;

    /** Flag indicating whether simulation thread execution is currently paused. */
    private volatile boolean paused = false;
    /** Flag indicating whether the simulation execution thread has been terminated. */
    private volatile boolean stopped = false;

    /** In-memory log collection recording completed customer stay durations for post-run persistence. */
    private List<CustomerLog> finishedCustomerLogs = new ArrayList<>();


    /**
     * Constructs and initializes the simulation engine with default service stations and arrival schedules.
     *
     * @param controller Link to the controller interface {@link IControllerMtoV} for visual updates and data transmission.
     */
    public MyEngine(IControllerMtoV controller) {
        super(controller);

        orderPoint = new ServicePoint("Order Register", new Normal(3, 0.4), eventList, EventType.DEP_ORDER);
        payPoint = new ServicePoint("Payment Counter", new Normal(2, 0.2), eventList, EventType.DEP_PAY);
        drinkPoint = new ServicePoint("Drink Kiosk", new Normal(3, 0.4), eventList, EventType.DEP_DRINK);
        pickupPoint = new ServicePoint("Pickup Counter", new Normal(2, 0.2), eventList, EventType.DEP_PICKUP);

        oven1 = new ServicePoint("Oven 1", new Normal(10, 1.0), eventList, EventType.DEP_OVEN1, 4);
        oven2 = new ServicePoint("Oven 2", new Normal(10, 1.0), eventList, EventType.DEP_OVEN2, 4);

        tablePoint = new ServicePoint("Dining Tables", new Normal(15, 9.0), eventList, EventType.DEP_TABLE, 10);
        courierPoint = new ServicePoint("Couriers", new Normal(15, 9.0), eventList, EventType.DEP_COURIER, 3);

        servicePoints = new ServicePoint[]{
                orderPoint, payPoint, tablePoint, drinkPoint, oven1, oven2, pickupPoint, courierPoint
        };

        arrivalProcess = new ArrivalProcess(new Negexp(5, 2), eventList, EventType.ARR);
        this.setDelay(500);
    }

    /**
     * Resets simulation clock time, customer sequence counters, and schedules the initial arrival event.
     */
    @Override
    protected void initialization() {
        Customer.reset(); // resets after each restart of simulation
        Clock.getInstance().setTime(0); // resets clock after restart
        arrivalProcess.generateNext();     // First arrival in the system
    }

    /**
     * Configures workstation parallel capacities for ovens, dining tables, and couriers.
     *
     * @param o1Cap      Capacity threshold for Oven 1.
     * @param o2Cap      Capacity threshold for Oven 2.
     * @param tableCap   Seating capacity for dining tables.
     * @param courierCap Active courier dispatch capacity.
     */
    public void setStationCapacities(int o1Cap, int o2Cap, int tableCap, int courierCap) {
        oven1.setCapacity(o1Cap);
        oven2.setCapacity(o2Cap);
        tablePoint.setCapacity(tableCap);
        courierPoint.setCapacity(courierCap);
    }

    /**
     * Re-initializes service point mean durations for ovens, tables, and couriers based on custom parameters.
     *
     * @param o1Time      Mean service time for Oven 1.
     * @param o2Time      Mean service time for Oven 2.
     * @param tableTime   Mean service time for dining tables.
     * @param courierTime Mean service time for delivery couriers.
     */
    public void setServiceTimes(double o1Time, double o2Time, double tableTime, double courierTime) {
        int o1Cap = oven1.getCapacity();
        int o2Cap = oven2.getCapacity();
        int tCap  = tablePoint.getCapacity();
        int cCap  = courierPoint.getCapacity();

        // Re-initialize service points with new mean durations
        oven1 = new ServicePoint("Oven 1", new Normal(o1Time, 1.0), eventList, EventType.DEP_OVEN1, o1Cap);
        oven2 = new ServicePoint("Oven 2", new Normal(o2Time, 1.0), eventList, EventType.DEP_OVEN2, o2Cap);
        tablePoint = new ServicePoint("Dining Tables", new Normal(tableTime, 1.5), eventList, EventType.DEP_TABLE, tCap);
        courierPoint = new ServicePoint("Couriers", new Normal(courierTime, 0.1), eventList, EventType.DEP_COURIER, cCap);

        // Update service points array reference
        servicePoints = new ServicePoint[]{
                orderPoint, payPoint, tablePoint, drinkPoint, oven1, oven2, pickupPoint, courierPoint
        };
    }

    /**
     * Configures customer mean inter-arrival interval pace using exponential distributions.
     *
     * @param pace Descriptive pace string (e.g., "Fast (2.5s)", "Slow (10s)", or default).
     */
    public void setArrivalPace(String pace) {
        double meanInterval = 5.0; // Default Normal
        if ("Fast (2.5s)".equals(pace)) {
            meanInterval = 2.5;
        } else if ("Slow (10s)".equals(pace)) {
            meanInterval = 10.0;
        }
        arrivalProcess = new ArrivalProcess(new Negexp(meanInterval, 2), eventList, EventType.ARR);
    }

    /**
     * Toggles the simulation thread pause state.
     */
    public void togglePause() {
        this.paused = !this.paused;
    }

    /**
     * Checks if the simulation engine execution is currently paused.
     *
     * @return {@code true} if paused; {@code false} otherwise.
     */
    public boolean isPaused() {
        return paused;
    }

    /**
     * Determines optimal oven routing by choosing an available oven or selecting the shorter queue.
     *
     * @param c The {@link Customer} requiring oven allocation.
     * @return String label indicating destination oven ("OVEN1" or "OVEN2").
     */
    private String routeToOven(Customer c) {
        if (!oven1.isReserved()) {
            oven1.addQueue(c); // Put in Oven 1 if it has space
            return "OVEN1";
        } else if (!oven2.isReserved()) {
            oven2.addQueue(c); // Put in Oven 2 if Oven 1 is full
            return "OVEN2";
        } else {
            // If BOTH are full, put the customer in the queue that is shortest
            if (oven1.getQueueSize() <= oven2.getQueueSize()) {
                oven1.addQueue(c);
                return "OVEN1";
            } else {
                oven2.addQueue(c);
                return "OVEN2";
            }
        }
    }

    /**
     * Executes discrete-event state transitions for B-phase events according to simulation business rules.
     *
     * @param t The discrete {@link Event} scheduled for execution.
     */
    @Override
    protected void runEvent(Event t) {  // B phase events
        Customer customer;
        while (paused) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                return;
            }
        }
        if (stopped) return;
        switch ((EventType) t.getType()) {
            case ARR:
                customer = new Customer();
                controller.visualiseCustomer(customer.getId(), customer.getType(), "ARRIVED");
                if (customer.getType() == CustomerType.ONLINE) {
                    String chosenOven = routeToOven(customer); // get OVEN1 or OVEN2
                    controller.visualiseCustomer(customer.getId(), customer.getType(), chosenOven);
                } else {
                    orderPoint.addQueue(customer);
                    controller.visualiseCustomer(customer.getId(), customer.getType(), "ORDER");
                }
                arrivalProcess.generateNext();
                break;

            case DEP_ORDER:
                customer = orderPoint.removeQueue();
                payPoint.addQueue(customer);
                controller.visualiseCustomer(customer.getId(), customer.getType(), "PAY");
                break;

            case DEP_PAY:
                customer = payPoint.removeQueue();
                String chosenOven = routeToOven(customer);
                controller.visualiseCustomer(customer.getId(), customer.getType(), chosenOven);
                break;

            case DEP_OVEN1:
            case DEP_OVEN2:
                if (t.getType() == EventType.DEP_OVEN1) {
                    customer = oven1.removeQueue();
                } else {
                    customer = oven2.removeQueue();
                }

                if (customer.getType() == CustomerType.ONLINE) {
                    courierPoint.addQueue(customer);
                    controller.visualiseCustomer(customer.getId(), customer.getType(), "COURIER");
                } else {
                    pickupPoint.addQueue(customer);
                    controller.visualiseCustomer(customer.getId(), customer.getType(), "PICKUP");
                }
                break;

            case DEP_PICKUP:
                customer = pickupPoint.removeQueue();
                if (customer.getType() == CustomerType.TAKEAWAY) {
                    finishCustomer(customer);
                } else if (customer.getType() == CustomerType.DINEIN) {
                    if (customer.wantsDrink()) {
                        drinkPoint.addQueue(customer);
                        controller.visualiseCustomer(customer.getId(), customer.getType(), "DRINK");
                    } else {
                        tablePoint.addQueue(customer);
                        controller.visualiseCustomer(customer.getId(), customer.getType(), "TABLE");
                    }
                }
                break;

            case DEP_DRINK:
                customer = drinkPoint.removeQueue();
                tablePoint.addQueue(customer);
                controller.visualiseCustomer(customer.getId(), customer.getType(), "TABLE");
                break;

            case DEP_TABLE:
                customer = tablePoint.removeQueue();
                finishCustomer(customer);
                break;

            case DEP_COURIER:
                customer = courierPoint.removeQueue();
                finishCustomer(customer);
                break;
        }
    }

    /**
     * Finalizes customer processing upon departure, logs metrics, and triggers UI updates.
     *
     * @param c The departing {@link Customer} instance.
     */
    private void finishCustomer(Customer c) {
        c.setRemovalTime(Clock.getInstance().getTime());
        c.reportResults();
        logCompletedCustomer(
                c.getId(),
                c.getType().name(),
                c.getArrivalTime(),
                c.getRemovalTime()
        );
        controller.visualiseCustomer(c.getId(), c.getType(), "FINISHED");
    }

    /**
     * Halts simulation execution thread and resets run duration parameters.
     */
    public void stopSimulation() {
        this.stopped = true;
        this.paused = false;
        setSimulationTime(0);
    }

    /**
     * Logs completed customer metrics into an in-memory collection prior to database persistence.
     *
     * @param customerId  Unique customer identifier.
     * @param customerType Category type string.
     * @param arrivalTime Arrival timestamp.
     * @param exitTime    Departure timestamp.
     */
    public void logCompletedCustomer(int customerId, String customerType, double arrivalTime, double exitTime) {
        double totalStayTime = exitTime - arrivalTime;
        CustomerLog log = new CustomerLog(0, customerId, customerType, arrivalTime, exitTime, totalStayTime);
        finishedCustomerLogs.add(log);
    }

    /**
     * Retrieves the collection of customer log records recorded during the current run.
     *
     * @return A {@link List} of {@link CustomerLog} entities.
     */
    public List<CustomerLog> getFinishedCustomerLogs() {
        return finishedCustomerLogs;
    }

    /**
     * Calculates the station utilization rate for Oven 1.
     *
     * @return Oven 1 utilization percentage/rate.
     */
    public double getOven1Utilization() {
        return oven1.getUtilization();
    }

    /**
     * Calculates the station utilization rate for Oven 2.
     *
     * @return Oven 2 utilization percentage/rate.
     */
    public double getOven2Utilization() {
        return oven2.getUtilization();
    }

    /**
     * Compiles final workstation performance statistics, updates the JavaFX UI, and triggers database persistence.
     */
    @Override
    protected void results() {
        double totalTime = Clock.getInstance().getTime();

        // Console output (original logic)
        System.out.println("=========================================");
        System.out.println("       SIMULATION RESULTS REPORT        ");
        System.out.println("=========================================");
        System.out.printf("Total Simulation Time: %.2f minutes\n\n", totalTime);

        // Build string output for JavaFX sidebar
        StringBuilder sb = new StringBuilder();
        sb.append("=====================\n");
        sb.append("SIMULATION RESULTS REPORT\n");
        sb.append("=====================\n");
        sb.append(String.format("Total Duration: %.2f minutes\n\n", totalTime));

        for (ServicePoint sp : servicePoints) {
            sp.reportResults(); // Console reporting per station

            // Collect station statistics for GUI
            sb.append(String.format("--- %s ---\n", sp.getName()));
            sb.append(String.format("Capacity: %d\n", sp.getCapacity()));
            sb.append(String.format("Total Serviced: %d\n", sp.getTotalServed()));
            sb.append(String.format("Utilization Rate: %.2f%%\n", sp.getUtilization()));
            sb.append(String.format("Avg Queue Length: %.2f customers\n", sp.getAverageQueueLength()));
            sb.append(String.format("Max Queue Length: %d customers\n\n", sp.getMaxQueueSize()));
        }

        // Send data to Controller
        controller.showEndTime(totalTime);
        controller.showResults(sb.toString());

        if (controller instanceof org.example.controller.Controller) {
            ((org.example.controller.Controller) controller).saveSimulationResultsToDatabase(this);
        }
    }
}
