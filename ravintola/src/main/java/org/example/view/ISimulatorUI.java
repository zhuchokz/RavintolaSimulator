package org.example.view;

public interface ISimulatorUI {
	// The Controller needs input which is passed to the Engine
	public double getTime();
	public long getDelay();
	public double getDrinkProbability();
	
	// Controller gives Engine produced results to the UI
	public void setEndingTime(double time);
	
	// Controller requires
	public IVisualisation getVisualisation();

	// Getters
	public int getOven1Capacity();
	public int getOven2Capacity();
	public int getTableCapacity();
	public int getCourierCapacity();
	public String getArrivalPace();
	public double getDineInWeight();
	public double getTakeawayWeight();
	public double getOnlineWeight();
	public double getOven1ServiceTime();
	public double getOven2ServiceTime();
	public double getTableServiceTime();
	public double getCourierServiceTime();

	// Setters
	public void setDrinkProbability(double value);
	public void setOven1Capacity(int capacity);
	public void setOven2Capacity(int capacity);
	public void setTableCapacity(int capacity);
	public void setCourierCapacity(int capacity);
	public void setArrivalPace(String pace);
	public void setOven1ServiceTime(double time);
	public void setOven2ServiceTime(double time);
	public void setTableServiceTime(double time);
	public void setCourierServiceTime(double time);
}
