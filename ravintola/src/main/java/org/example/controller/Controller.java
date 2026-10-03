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


		long initialDelay = ui.getDelay();
		if (initialDelay < 100) {
			initialDelay = 5000;
		}
		engine.setDelay(initialDelay);

		myEngine.setStationCapacities(
				ui.getOven1Capacity(),
				ui.getOven2Capacity(),
				ui.getTableCapacity(),
				ui.getCourierCapacity()
		);
		myEngine.setArrivalPace(ui.getArrivalPace());

		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			((PizzaVisualisation) ui.getVisualisation()).adjustAnimationSpeed(1.0);
		}

		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
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
	public void decreaseSpeed() {
		engine.setDelay((long)(engine.getDelay() * 1.10));

		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			((PizzaVisualisation) ui.getVisualisation()).adjustAnimationSpeed(0.85);
		}
	}

	@Override
	public void increaseSpeed() {
		long newDelay = (long)(engine.getDelay() * 0.90);
		if (newDelay < 10) newDelay = 10;
		engine.setDelay(newDelay);

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
