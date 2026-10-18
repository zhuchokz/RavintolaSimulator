package org.example.controller;

import org.example.simu.model.CustomerType;

/**
 * Controller interface exposed to the simulation engine (Model-to-View).
 * <p>
 * Defines the contract through which the core simulation engine pushes execution timestamps,
 * dynamic customer state transitions for visualization, and final performance reports to the presentation layer.
 * </p>
 *
 * @author Illia, Daniel
 * @version 3.0
 */
public interface IControllerMtoV {

	/**
	 * Transmits the final simulation completion timestamp to the UI for display.
	 *
	 * @param time Total elapsed simulation execution duration.
	 */
	public void showEndTime(double time);

	/**
	 * Relays customer state transitions and location movements to the UI visualization canvas.
	 *
	 * @param id     Unique customer entity identifier.
	 * @param type   Customer category type (DINEIN, TAKEAWAY, ONLINE).
	 * @param action Target workstation action or state label string.
	 */
	public void visualiseCustomer(int id, CustomerType type, String action);

	/**
	 * Delivers final formatted simulation statistics and workstation performance metrics to the UI text area.
	 *
	 * @param resultsText Complete formatted performance report string.
	 */
	public void showResults(String resultsText);
}
