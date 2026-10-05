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


public class MyEngine extends Engine {
    private ArrivalProcess arrivalProcess;

    private ServicePoint orderPoint;
    private ServicePoint payPoint;
    private ServicePoint tablePoint;
    private ServicePoint drinkPoint;
    private ServicePoint oven1;
    private ServicePoint oven2;
    private ServicePoint pickupPoint;
    private ServicePoint courierPoint;

    private volatile boolean paused = false;
    private volatile boolean stopped = false;

    private List<CustomerLog> finishedCustomerLogs = new ArrayList<>();

    public MyEngine(IControllerMtoV controller) {
        super(controller);

        orderPoint = new ServicePoint("Order Register", new Normal(2, 0.1), eventList, EventType.DEP_ORDER);
        payPoint = new ServicePoint("Payment Counter", new Normal(1, 0.1), eventList, EventType.DEP_PAY);
        tablePoint = new ServicePoint("Dining Tables", new Normal(15, 1.5), eventList, EventType.DEP_TABLE, 10);
        drinkPoint = new ServicePoint("Drink Kiosk", new Normal(1, 0.1), eventList, EventType.DEP_DRINK);

        oven1 = new ServicePoint("Oven 1", new Normal(10, 1), eventList, EventType.DEP_OVEN1, 4);
        oven2 = new ServicePoint("Oven 2", new Normal(10, 1), eventList, EventType.DEP_OVEN2, 4);

        pickupPoint = new ServicePoint("Pickup Counter", new Normal(1, 0.1), eventList, EventType.DEP_PICKUP);
        courierPoint = new ServicePoint("Couriers", new Normal(15, 0.1), eventList, EventType.DEP_COURIER, 3);

        servicePoints = new ServicePoint[]{
                orderPoint, payPoint, tablePoint, drinkPoint, oven1, oven2, pickupPoint, courierPoint
        };

        arrivalProcess = new ArrivalProcess(new Negexp(5, 2), eventList, EventType.ARR);
        this.setDelay(500);
    }

    @Override
    protected void initialization() {
        Customer.reset(); // resets after each restart of simulation
        Clock.getInstance().setTime(0); // resets clock after restart
        arrivalProcess.generateNext();     // First arrival in the system
    }

    public void setStationCapacities(int o1Cap, int o2Cap, int tableCap, int courierCap) {
        oven1.setCapacity(o1Cap);
        oven2.setCapacity(o2Cap);
        tablePoint.setCapacity(tableCap);
        courierPoint.setCapacity(courierCap);
    }

    public void setArrivalPace(String pace) {
        double meanInterval = 5.0; // Default Normal
        if ("Fast (2.5s)".equals(pace)) {
            meanInterval = 2.5;
        } else if ("Slow (10s)".equals(pace)) {
            meanInterval = 10.0;
        }
        arrivalProcess = new ArrivalProcess(new Negexp(meanInterval, 2), eventList, EventType.ARR);
    }

    public void togglePause() {
        this.paused = !this.paused;
    }

    public boolean isPaused() {
        return paused;
    }

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

    public void stopSimulation() {
        this.stopped = true;
        this.paused = false;
        setSimulationTime(0);
    }

    public void logCompletedCustomer(int customerId, String customerType, double arrivalTime, double exitTime) {
        double totalStayTime = exitTime - arrivalTime;
        CustomerLog log = new CustomerLog(0, customerId, customerType, arrivalTime, exitTime, totalStayTime);
        finishedCustomerLogs.add(log);
    }

    public List<CustomerLog> getFinishedCustomerLogs() {
        return finishedCustomerLogs;
    }

    public double getOven1Utilization() {
        return oven1.getUtilization();
    }

    public double getOven2Utilization() {
        return oven2.getUtilization();
    }

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
        sb.append("======================\n");
        sb.append("SIMULATION RESULTS REPORT\n");
        sb.append("======================\n");
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
