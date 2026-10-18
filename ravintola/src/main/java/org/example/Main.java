package org.example;

import org.example.view.SimulatorGUI;

/**
 * Application entry point for launching the discrete-event pizzeria simulation.
 * <p>
 * Serves as the primary execution wrapper that initializes JavaFX platform settings
 * and delegates execution startup to {@link SimulatorGUI}.
 * </p>
 *
 * @author Illia, Anja, Daniel, Niklas
 * @version 4.0
 */
public class Main { // Simulator using Java FX
	public static void main(String args[]) {
		SimulatorGUI.main(args);
	}
}
