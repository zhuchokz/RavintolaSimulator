package org.example.view;

import org.example.simu.model.CustomerType;

public interface IVisualisation {
	public void clearDisplay();
	public void resetVisuals();
	public void visualiseCustomer(int id, CustomerType type, String action);

	void adjustAnimationSpeed(double speedMultiplier);
}

