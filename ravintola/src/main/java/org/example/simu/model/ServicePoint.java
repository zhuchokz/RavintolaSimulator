package org.example.simu.model;

import org.example.eduni.distributions.ContinuousGenerator;
import org.example.simu.framework.Clock;
import org.example.simu.framework.Event;
import org.example.simu.framework.EventList;

import java.util.LinkedList;

// TODO:
// Service Point functionalities & calculations (+ variables needed) and reporting to be implemented
public class ServicePoint {
	private String name;
	private LinkedList<Customer> jono = new LinkedList<Customer>(); // Data Structure used
	private ContinuousGenerator generator;
	private EventList eventList;
	private EventType eventTypeScheduled;

	//Queuestrategy strategy; // option: ordering of the customer
	private int capacity;
	private int activeServices = 0;

	// --- Performance Metrics Variables ---
	private int totalServed = 0;
	private int maxQueueSize = 0;
	private double cumulativeQueueTime = 0.0; // Area under queue length curve
	private double cumulativeBusyTime = 0.0;  // Total time slots were busy
	private double lastUpdateTime = 0.0;

	/**
	 * Default constructor (Single capacity = 1)
	 * Keeps backward compatibility for normal queues like Cashier/Payment.
	 */
	public ServicePoint(String name, ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
		this(name, generator, tapahtumalista, tyyppi, 1);
	}

	/**
	 * Overloaded constructor for Multi-Capacity (Ovens, Tables, Couriers)
	 */
	public ServicePoint(String name, ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi, int capacity){
		this.name = name;
		this.eventList = tapahtumalista;
		this.generator = generator;
		this.eventTypeScheduled = tyyppi;
		this.capacity = capacity;
	}

	/**
	 * Updates time-weighted statistics before any state change occurs.
	 */
	private void updateStats() {
		double currentTime = Clock.getInstance().getTime();
		double timeDiff = currentTime - lastUpdateTime;

		if (timeDiff > 0) {
			int waitingCount = getQueueSize();
			cumulativeQueueTime += waitingCount * timeDiff;
			cumulativeBusyTime += activeServices * timeDiff;
			lastUpdateTime = currentTime;
		}
	}

	public void addQueue(Customer a){   // First customer at the queue is always on the service
		updateStats();
		jono.add(a);

		int waiting = getQueueSize();
		if (waiting > maxQueueSize) {
			maxQueueSize = waiting;
		}
	}

	public Customer removeQueue(){		// Remove serviced customer
		updateStats();
		activeServices--;
		totalServed++;
		return jono.poll();
	}

	public void beginService() {  		// Begins a new service, customer is on the queue during the service
		updateStats();
		activeServices++;
		double serviceTime = generator.sample();
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));
	}

	public boolean isReserved(){
		return activeServices >= capacity;
	}

	public boolean isOnQueue(){
		return (jono.size() - activeServices) > 0;
	}

	public int getQueueSize() {
		return Math.max(0, jono.size() - activeServices);
	}

	public String getName() {
		return name;
	}

	// --- Results & Reporting Methods ---

	public double getUtilization() {
		updateStats();
		double totalSimTime = Clock.getInstance().getTime();
		if (totalSimTime == 0) return 0.0;
		return (cumulativeBusyTime / (capacity * totalSimTime)) * 100.0;
	}

	public double getAverageQueueLength() {
		updateStats();
		double totalSimTime = Clock.getInstance().getTime();
		if (totalSimTime == 0) return 0.0;
		return cumulativeQueueTime / totalSimTime;
	}

	public void reportResults() {
		System.out.printf("--- %s ---\n", name);
		System.out.println("  Capacity: " + capacity);
		System.out.println("  Total Serviced: " + totalServed);
		System.out.printf("  Utilization Rate: %.2f%%\n", getUtilization());
		System.out.printf("  Avg Queue Length: %.2f customers\n", getAverageQueueLength());
		System.out.println("  Max Queue Length: " + maxQueueSize + " customers");
		System.out.println();
	}
}
