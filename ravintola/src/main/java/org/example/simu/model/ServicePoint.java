package org.example.simu.model;

import org.example.eduni.distributions.ContinuousGenerator;
import org.example.simu.framework.Clock;
import org.example.simu.framework.Event;
import org.example.simu.framework.EventList;

import java.util.LinkedList;


/**
 * Model entity representing a service point workstation within the pizzeria simulation.
 * <p>
 * Manages customer queueing, schedules completion events using stochastic generators,
 * supports both single-channel and multi-capacity workstations (e.g., ovens, dining tables, couriers),
 * and computes continuous time-weighted performance metrics (utilization and average queue length).
 * </p>
 *
 * @author Illia, Niklas
 * @version 3.0
 */
public class ServicePoint {
	/** Descriptive label name identifying the service point workstation. */
	private String name;
	/** Queue data structure holding waiting and in-service customer entities. */
	private LinkedList<Customer> jono = new LinkedList<Customer>();
	/** Continuous random distribution generator producing service duration samples. */
	private ContinuousGenerator generator;
	/** Reference to the shared event list for scheduling service completion events. */
	private EventList eventList;
	/** Discrete event type to schedule upon service completion. */
	private EventType eventTypeScheduled;

	/** Maximum parallel processing capacity threshold of the workstation. */
	private int capacity;
	/** Current number of customers actively undergoing service at the workstation. */
	private int activeServices = 0;

	/** Cumulative total count of customers serviced by this station. */
	private int totalServed = 0;
	/** Peak customer queue size recorded at this station during the run. */
	private int maxQueueSize = 0;
	/** Time-weighted area integral accumulated under the queue length curve. */
	private double cumulativeQueueTime = 0.0;
	/** Time-weighted cumulative duration units during which service capacity was active. */
	private double cumulativeBusyTime = 0.0;
	/** Simulation clock timestamp when time-weighted metrics were last updated. */
	private double lastUpdateTime = 0.0;

	/**
	 * Constructs a single-capacity service point (capacity = 1) for standard queues.
	 *
	 * @param name           Descriptive name for the workstation.
	 * @param generator      Stochastic random generator producing service times.
	 * @param tapahtumalista Shared event list reference.
	 * @param tyyppi         Discrete event type scheduled upon service completion.
	 */
	public ServicePoint(String name, ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi){
		this(name, generator, tapahtumalista, tyyppi, 1);
	}

	/**
	 * Constructs a multi-capacity service point with a specified parallel capacity limit.
	 *
	 * @param name           Descriptive name for the workstation.
	 * @param generator      Stochastic random generator producing service times.
	 * @param tapahtumalista Shared event list reference.
	 * @param tyyppi         Discrete event type scheduled upon service completion.
	 * @param capacity       Maximum parallel service capacity.
	 */
	public ServicePoint(String name, ContinuousGenerator generator, EventList tapahtumalista, EventType tyyppi, int capacity){
		this.name = name;
		this.eventList = tapahtumalista;
		this.generator = generator;
		this.eventTypeScheduled = tyyppi;
		this.capacity = capacity;
	}

	/**
	 * Updates time-weighted queue length and busy time statistics prior to state changes.
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

	/**
	 * Adds a customer to the station queue and updates maximum queue size tracking.
	 *
	 * @param a The {@link Customer} entering the queue.
	 */
	public void addQueue(Customer a){   // First customer at the queue is always on the service
		updateStats();
		jono.add(a);

		int waiting = getQueueSize();
		if (waiting > maxQueueSize) {
			maxQueueSize = waiting;
		}
	}

	/**
	 * Removes and returns the serviced customer from the station queue.
	 *
	 * @return The completed {@link Customer} entity.
	 */
	public Customer removeQueue(){
		updateStats();
		activeServices--;
		totalServed++;
		return jono.poll();
	}

	/**
	 * Initiates service for a customer, increments active busy capacity, and schedules a completion event.
	 */
	public void beginService() {
		updateStats();
		activeServices++;
		double serviceTime = generator.sample();
		eventList.add(new Event(eventTypeScheduled, Clock.getInstance().getTime()+serviceTime));
	}

	/**
	 * Checks if all service capacity units are currently active.
	 *
	 * @return {@code true} if active services equal or exceed capacity; {@code false} otherwise.
	 */
	public boolean isReserved(){
		return activeServices >= capacity;
	}

	/**
	 * Checks if any customer is waiting in line outside of active service.
	 *
	 * @return {@code true} if waiting queue length is greater than zero; {@code false} otherwise.
	 */
	public boolean isOnQueue(){
		return (jono.size() - activeServices) > 0;
	}

	/**
	 * Calculates the number of customers currently waiting in line (excluding active services).
	 *
	 * @return Current waiting customer queue count.
	 */
	public int getQueueSize() {
		return Math.max(0, jono.size() - activeServices);
	}

	/**
	 * Gets the descriptive workstation name.
	 *
	 * @return Station name string.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the total number of customers serviced by this station.
	 *
	 * @return Total serviced customer count.
	 */
	public int getTotalServed() { return totalServed; }

	/**
	 * Sets the maximum parallel processing capacity threshold.
	 *
	 * @param capacity Station capacity limit to set.
	 */
	public void setCapacity(int capacity) { this.capacity = capacity; }

	/**
	 * Gets the maximum parallel processing capacity threshold.
	 *
	 * @return Station capacity limit.
	 */
	public int getCapacity() { return capacity; }

	/**
	 * Gets the peak queue length recorded during the run.
	 *
	 * @return Maximum waiting customer count.
	 */
	public int getMaxQueueSize() { return maxQueueSize; }


	/**
	 * Computes the station resource utilization percentage rate.
	 *
	 * @return Utilization percentage (0.0% to 100.0%).
	 */
	public double getUtilization() {
		updateStats();
		double totalSimTime = Clock.getInstance().getTime();
		if (totalSimTime == 0) return 0.0;
		return (cumulativeBusyTime / (capacity * totalSimTime)) * 100.0;
	}

	/**
	 * Computes the time-weighted average customer queue length over total simulation duration.
	 *
	 * @return Average customer queue length.
	 */
	public double getAverageQueueLength() {
		updateStats();
		double totalSimTime = Clock.getInstance().getTime();
		if (totalSimTime == 0) return 0.0;
		return cumulativeQueueTime / totalSimTime;
	}

	/**
	 * Outputs station performance statistics directly to standard console output.
	 */
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
