package org.example.view;

import org.example.simu.model.CustomerType;

/**
 * Interface defining the visual rendering component contract for the JavaFX pizzeria simulation UI.
 * <p>
 * Provides abstract methods for rendering active customer movements across workstations, clearing display states,
 * adjusting animation playback speeds, setting workstation capacity limits, and controlling dynamic animation playback.
 * </p>
 *
 * @author Anja
 * @version 3.0
 */
public interface IVisualisation {
	public void clearDisplay();
	public void resetVisuals();
	public void startVisuals();
	public void visualiseCustomer(int id, CustomerType type, String action);

	void adjustAnimationSpeed(double speedMultiplier);
	void setPaused(boolean paused);

	void setStationCapacities(int oven1Capacity, int oven2Capacity, int tableCapacity, int courierCapacity);
}

