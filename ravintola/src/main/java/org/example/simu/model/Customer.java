package org.example.simu.model;

import org.example.simu.framework.Clock;
import org.example.simu.framework.Trace;
import java.util.Random;


// TODO:
// Customer to be implemented according to the requirements of the simulation model (data!)
public class Customer {
	private double arrivalTime;
	private double removalTime;
	private int id;
	private CustomerType type;

	private boolean wantsDrink;
	private static volatile double drinkProbability = 0.5;
	private static volatile double dineInWeight = 40.0;
	private static volatile double takeawayWeight = 40.0;
	private static volatile double onlineWeight = 20.0;

	private static int i = 1;
	private static double totalStayTime = 0;
	private static int completedCount = 0;
	private static Random random = new Random(42);

	public static void reset() { // Call this when starting a new simulation run
		i = 1;
		completedCount = 0;
		totalStayTime = 0;
		resetSeed();
	}

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

	public Customer() {
		this(generateRandomType());
	}
	
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

	public CustomerType getType() {
		return type;
	}

	public void setType(CustomerType type) {
		this.type = type;
	}

	public boolean wantsDrink() {
		return wantsDrink;
	}

	public void setWantsDrink(boolean wantsDrink) {
		this.wantsDrink = wantsDrink;
	}

	public int getId() {
		return id;
	}

	public double getRemovalTime() {
		return removalTime;
	}

	public void setRemovalTime(double removalTime) {
		this.removalTime = removalTime;
	}

	public double getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(double arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public static void setDrinkProbability(double probability) {
		drinkProbability = probability;
	}

	public double getDrinkProbability() {
		if (this.type == CustomerType.DINEIN) {
			return drinkProbability;
		}
		return 0.0;
	}

	public static void setCustomerWeights(double dineIn, double takeaway, double online) {
		dineInWeight = dineIn;
		takeawayWeight = takeaway;
		onlineWeight = online;
	}

	public static void resetSeed() {
		random = new Random(42);
	}
	
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
