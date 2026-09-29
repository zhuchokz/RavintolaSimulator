package org.example.controller;

import org.example.simu.model.CustomerType;

/* interface for the engine */
public interface IControllerMtoV {
	public void showEndTime(double time);
	public void visualiseCustomer(int id, CustomerType type, String action);
	public void showResults(String resultsText);
}
