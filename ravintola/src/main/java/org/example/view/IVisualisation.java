package org.example.view;

import org.example.simu.model.CustomerType;

public interface IVisualisation {
	public void clearDisplay();
	public void resetVisuals();
	public void startVisuals();
	public void visualiseCustomer(int id, CustomerType type, String action);

	void adjustAnimationSpeed(double speedMultiplier);
	void setPaused(boolean paused);

	void setStationCapacities(int oven1Capacity, int oven2Capacity, int tableCapacity, int courierCapacity);
}

