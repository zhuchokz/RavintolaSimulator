package org.example.simu.model;

import org.example.simu.framework.Clock;
import org.example.simu.framework.Trace;
import java.util.Random;


/**
 * Model entity representing an individual customer moving through the pizzeria simulation.
 * <p>
 * Tracks customer arrival and exit timestamps, assigns customer category types (dine-in, takeaway, online)
 * based on configurable relative distribution weights, models ordering preferences (e.g., drink probability),
 * and computes system-wide throughput metrics.
 * </p>
 *
 * @author Illia, Niklas, Daniel, Anja
 * @version 3.0
 */
public class Customer {
	/** Simulation timestamp when the customer entered the system. */
	private double arrivalTime;
	/** Simulation timestamp when the customer completed service and departed from the system. */
	private double removalTime;
	/** Unique incremental identifier assigned to the customer. */
	private int id;
	/** Category type defining customer behavior and routing path. */
	private CustomerType type;

	/** Indicates whether the customer desires a drink with their order. */
	private boolean wantsDrink;
	/** Global probability threshold (0.0 to 1.0) determining whether a dine-in customer orders a drink. */
	private static volatile double drinkProbability = 0.5;
	/** Relative distribution weight for generating dine-in customers. */
	private static volatile double dineInWeight = 40.0;
	/** Relative distribution weight for generating takeaway customers. */
	private static volatile double takeawayWeight = 40.0;
	/** Relative distribution weight for generating online delivery customers. */
	private static volatile double onlineWeight = 20.0;

	/** Static counter used to assign unique sequential IDs to newly instantiated customers. */
	private static int i = 1;
	/** Cumulative stay duration sum across all completed customers for average calculation. */
	private static double totalStayTime = 0;
	/** Total number of customers who have successfully departed the simulation. */
	private static int completedCount = 0;
	/** Seeded random number generator for reproducible customer type and preference generation. */
	private static Random random = new Random(42);


	/**
	 * Resets static sequence counters, total metrics, and random generation seeds.
	 * <p>
	 * Must be called prior to starting a new simulation run to ensure consistent results.
	 * </p>
	 */
	public static void reset() { // Call this when starting a new simulation run
		i = 1;
		completedCount = 0;
		totalStayTime = 0;
		resetSeed();
	}

	/**
	 * Generates a random customer category type based on the currently configured relative weight ratios.
	 *
	 * @return The selected {@link CustomerType} (DINEIN, TAKEAWAY, or ONLINE).
	 */
	private static CustomerType generateRandomType() {
		double totalWeight = dineInWeight + takeawayWeight + onlineWeight;

		// Fallback if all sliders are at 0
		if (totalWeight <= 0) return CustomerType.DINEIN;

		double roll = random.nextDouble() * totalWeight;

		if (roll < dineInWeight) {
			return CustomerType.DINEIN;
		} else if (roll < dineInWeight + takeawayWeight) {
			return CustomerType.TAKEAWAY;
		} else {
			return CustomerType.ONLINE;
		}
	}

	/**
	 * Constructs a new customer instance with a randomly assigned customer type.
	 */
	public Customer() {
		this(generateRandomType());
	}


	/**
	 * Constructs a new customer instance with the specified customer type.
	 * <p>
	 * Automatically assigns a sequential ID, records the current clock time as arrival time,
	 * and evaluates drink preference logic for dine-in customers.
	 * </p>
	 *
	 * @param type The {@link CustomerType} to assign to this customer.
	 */
	public Customer(CustomerType type) {
	    this.id = i++;
		this.type = type;
		this.arrivalTime = Clock.getInstance().getTime();

		if (this.type == CustomerType.DINEIN) {
			this.wantsDrink = random.nextDouble() < drinkProbability;
		} else {
			this.wantsDrink = false;
		}

		Trace.out(Trace.Level.INFO, "New customer #" + id + " with type " + getType().toString() + " and drink% " + getDrinkProbability() + " arrived at  " + arrivalTime);
	}

	/**
	 * Gets the customer category type.
	 *
	 * @return The assigned {@link CustomerType}.
	 */
	public CustomerType getType() {
		return type;
	}

	/**
	 * Sets the customer category type.
	 *
	 * @param type The {@link CustomerType} to set.
	 */
	public void setType(CustomerType type) {
		this.type = type;
	}

	/**
	 * Checks if the customer wants a drink.
	 *
	 * @return {@code true} if the customer ordered a drink; {@code false} otherwise.
	 */
	public boolean wantsDrink() {
		return wantsDrink;
	}

	/**
	 * Sets whether the customer wants a drink.
	 *
	 * @param wantsDrink The drink preference state to set.
	 */
	public void setWantsDrink(boolean wantsDrink) {
		this.wantsDrink = wantsDrink;
	}

	/**
	 * Gets the unique sequential customer ID.
	 *
	 * @return The customer ID number.
	 */
	public int getId() {
		return id;
	}

	/**
	 * Gets the simulation timestamp when the customer departed.
	 *
	 * @return The removal timestamp.
	 */
	public double getRemovalTime() {
		return removalTime;
	}

	/**
	 * Sets the simulation timestamp when the customer departs.
	 *
	 * @param removalTime The departure timestamp to set.
	 */
	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	/**
	 * Gets the simulation timestamp when the customer arrived.
	 *
	 * @return The arrival timestamp.
	 */
	public double getArrivalTime() {
		return arrivalTime;
	}

	/**
	 * Sets the simulation timestamp when the customer arrived.
	 *
	 * @param arrivalTime The arrival timestamp to set.
	 */
	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	/**
	 * Sets the global drink probability for dine-in customers.
	 *
	 * @param probability The probability threshold between 0.0 and 1.0.
	 */
	public static void setDrinkProbability(double probability) {
		drinkProbability = probability;
	}

	/**
	 * Gets the drink probability applicable to this specific customer instance.
	 *
	 * @return The global drink probability if dine-in; {@code 0.0} otherwise.
	 */
	public double getDrinkProbability() {
		if (this.type == CustomerType.DINEIN) {
			return drinkProbability;
		}
		return 0.0;
	}


	/**
	 * Configures the relative weighting ratios for customer type generation.
	 *
	 * @param dineIn   Weight ratio for dine-in customers.
	 * @param takeaway Weight ratio for takeaway customers.
	 * @param online   Weight ratio for online delivery customers.
	 */
	public static void setCustomerWeights(double dineIn, double takeaway, double online) {
		dineInWeight = dineIn;
		takeawayWeight = takeaway;
		onlineWeight = online;
	}

	/**
	 * Resets the seeded random generator to ensure deterministic behavior across runs.
	 */
	public static void resetSeed() {
		random = new Random(42);
	}

	/**
	 * Logs departure trace statements and updates global customer metrics upon simulation exit.
	 */
	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + "with type " + getType().toString() + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " stayed: "  + (removalTime - arrivalTime));

		completedCount++;
		totalStayTime += (removalTime - arrivalTime);
		double mean = totalStayTime/completedCount;
		System.out.println("Current mean of the customer service times " + mean);
	}

}
