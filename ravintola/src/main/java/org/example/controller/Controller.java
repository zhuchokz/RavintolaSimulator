package org.example.controller;

import javafx.application.Platform;
import  org.example.simu.framework.IEngine;
import  org.example.simu.model.MyEngine;
import  org.example.view.ISimulatorUI;
import org.example.view.PizzaVisualisation;
import org.example.view.SimulatorGUI;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private ISimulatorUI ui;
	
	public Controller(ISimulatorUI ui) {
		this.ui = ui;
	}

	/* Engine control: */
	@Override
	public void startSimulation() {
		engine = new MyEngine(this); // new Engine thread is created for every simulation
		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());
		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
		//((Thread)engine).run(); // Never like this, why?
	}

	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));
	}

	@Override
	public void visualiseCustomer() {
		Platform.runLater(() -> {
			// start the ball animation on the canvas.
			ui.getVisualisation().newCustomer();

			// Increasing the number of clients in the statistics sidebar
			if (ui instanceof SimulatorGUI) {
				((SimulatorGUI) ui).incrementCustomerCount();
			}
		});
	}
	@Override
	public void decreaseSpeed() { // Slowdown
		engine.setDelay((long)(engine.getDelay() * 1.10)); //Slowing down the engine logic

		// slow down the movement of the balls on the canvas.
		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			((PizzaVisualisation) ui.getVisualisation()).adjustAnimationSpeed(0.85);
		}
	}

	@Override
	public void increaseSpeed() { // speed up
		engine.setDelay((long)(engine.getDelay() * 0.90)); //speed up the engine logic

		// speeding up the movement of the balls on the canvas
		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			((PizzaVisualisation) ui.getVisualisation()).adjustAnimationSpeed(1.20);
		}
	}


}
