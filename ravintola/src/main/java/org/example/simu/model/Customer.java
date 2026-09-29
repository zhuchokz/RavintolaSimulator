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

	private static int i = 1;
	private static double totalStayTime = 0;
	private static int completedCount = 0;
	private static final Random random = new Random();

	public static void reset() { // Call this when starting a new simulation run
		i = 1;
		completedCount = 0;
		totalStayTime = 0;
	}

	public Customer() {
		this(CustomerType.values()[random.nextInt(CustomerType.values().length)]);
	}
	
	public Customer(CustomerType type) {
	    this.id = i++;
		this.type = type;
		this.arrivalTime = Clock.getInstance().getTime();

		if (this.type == CustomerType.DINEIN) {
			this.wantsDrink = random.nextBoolean();
		} else {
			this.wantsDrink = false;
		}

		Trace.out(Trace.Level.INFO, "New customer #" + id + " arrived at  " + arrivalTime);
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
	
	public void reportResults() {
		Trace.out(Trace.Level.INFO, "\nCustomer " + id + " ready! ");
		Trace.out(Trace.Level.INFO, "Customer "   + id + " arrived: " + arrivalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " removed: " + removalTime);
		Trace.out(Trace.Level.INFO,"Customer "    + id + " stayed: "  + (removalTime - arrivalTime));

		completedCount++;
		totalStayTime += (removalTime - arrivalTime);
		double mean = totalStayTime/completedCount;
		System.out.println("Current mean of the customer service times " + mean);
	}

}
