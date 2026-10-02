package org.example.controller;

import javafx.application.Platform;
import org.example.simu.framework.IEngine;
import org.example.simu.model.Customer;
import org.example.simu.model.CustomerType;
import org.example.simu.model.MyEngine;
import org.example.view.ISimulatorUI;
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
		Customer.setDrinkProbability(ui.getDrinkProbability());
		Customer.setCustomerWeights(
				ui.getDineInWeight(),
				ui.getTakeawayWeight(),
				ui.getOnlineWeight()
		);

		MyEngine myEngine = new MyEngine(this); // new Engine thread is created for every simulation
		engine = myEngine;

		engine.setSimulationTime(ui.getTime());
		engine.setDelay(ui.getDelay());

		myEngine.setStationCapacities(
				ui.getOven1Capacity(),
				ui.getOven2Capacity(),
				ui.getTableCapacity(),
				ui.getCourierCapacity()
		);
		myEngine.setArrivalPace(ui.getArrivalPace());

		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
		//((Thread)engine).run(); // Never like this, why?
	}

	public void togglePause() {
		if (engine instanceof MyEngine) {
			((MyEngine) engine).togglePause();
		}
	}

	/* Simulation results passing to the UI
	 * Because FX-UI updates come from engine thread, they need to be directed to the JavaFX thread
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));
	}

	@Override
	public void visualiseCustomer(int id, CustomerType type, String action) {
		Platform.runLater(() -> {
			// Pass the data to the canvas
			ui.getVisualisation().visualiseCustomer(id, type, action);

			// Update the numbers on the UI text fields
			if (ui instanceof SimulatorGUI) {
				if ("ARRIVED".equals(action)) {
					((SimulatorGUI) ui).incrementCustomerCount();
					((SimulatorGUI) ui).logEvent(String.format("[+] Customer #%d (%s) arrived.", id, type));
				} else if ("FINISHED".equals(action)) {
					((SimulatorGUI) ui).decrementCustomerCount();
					((SimulatorGUI) ui).logEvent(String.format("[-] Customer #%d (%s) left.", id, type));
				}
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

	@Override
	public void stopSimulation() {
		if (engine instanceof MyEngine) {
			((MyEngine) engine).stopSimulation();
		}
	}

	@Override
	public void showResults(String s) {
		Platform.runLater(() -> {
			if (ui instanceof SimulatorGUI) {
				((SimulatorGUI) ui).displayResults(s);
			}
		});
	}
}
