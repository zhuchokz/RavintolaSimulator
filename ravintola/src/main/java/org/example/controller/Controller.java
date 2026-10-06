package org.example.controller;

import javafx.application.Platform;
import org.example.simu.framework.Clock;
import org.example.simu.framework.IEngine;
import org.example.simu.model.Customer;
import org.example.simu.model.CustomerType;
import org.example.simu.model.MyEngine;

import org.example.view.ISimulatorUI;
import org.example.view.PizzaVisualisation;
import org.example.view.SimulatorGUI;

import org.example.dao.CustomerLogDAO;
import org.example.dao.SimulationRunDAO;
import org.example.dao.PresetConfigDAO;

import org.example.model.CustomerLog;
import org.example.model.SimulationRun;
import org.example.model.PresetConfig;

import java.util.List;

public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private ISimulatorUI ui;
	private PresetConfigDAO presetDAO = new PresetConfigDAO();
	
	public Controller(ISimulatorUI ui) {
		this.ui = ui;
	}

	/* Engine control: */
	@Override
	public void startSimulation() {
		Customer.reset();
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
		if (initialDelay < 10) {
			initialDelay = 50;
		}
		engine.setDelay(initialDelay);

		myEngine.setStationCapacities(
				ui.getOven1Capacity(),
				ui.getOven2Capacity(),
				ui.getTableCapacity(),
				ui.getCourierCapacity()
		);
		myEngine.setArrivalPace(ui.getArrivalPace());

		myEngine.setServiceTimes(
				ui.getOven1ServiceTime(),
				ui.getOven2ServiceTime(),
				ui.getTableServiceTime(),
				ui.getCourierServiceTime()
		);

		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			ui.getVisualisation().adjustAnimationSpeed(1.0);
			ui.getVisualisation().resetVisuals();
			ui.getVisualisation().startVisuals();
			ui.getVisualisation().setStationCapacities(ui.getOven1Capacity(), ui.getOven2Capacity(), ui.getTableCapacity(), ui.getCourierCapacity());
		}

		ui.getVisualisation().clearDisplay();
		((Thread) engine).start();
	}


	public void togglePause() {
		if (engine instanceof MyEngine) {
			MyEngine myEngine = (MyEngine) engine;
			myEngine.togglePause();

			// Sync UI visualization pause state with engine pause state
			if (ui.getVisualisation() != null) {
				ui.getVisualisation().setPaused(myEngine.isPaused());
			}
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
		if (ui.getVisualisation() != null) {
			ui.getVisualisation().resetVisuals();
		}
	}

	@Override
	public List<SimulationRun> getAllSimulationRuns() {
		SimulationRunDAO runDAO = new SimulationRunDAO();
		return runDAO.getAllRuns();
	}

	@Override
	public void showResults(String s) {
		Platform.runLater(() -> {
			if (ui instanceof SimulatorGUI) {
				((SimulatorGUI) ui).displayResults(s);
			}
		});
	}

	// Run in a background thread so database latency doesn't hang the application
	public void saveSimulationResultsToDatabase(MyEngine engine) {
		new Thread(() -> {
			System.out.println("Initiating database save...");

			List<CustomerLog> logs = engine.getFinishedCustomerLogs();

			// 1. Calculate run statistics dynamically from the collected logs
			int takeaway = 0, dinein = 0, online = 0;
			double totalStaySum = 0;

			for (CustomerLog log : logs) {
				totalStaySum += log.getTotalStayTime();
				switch (log.getCustomerType()) {
					case "TAKEAWAY": takeaway++; break;
					case "DINEIN": dinein++; break;
					case "ONLINE": online++; break;
				}
			}

			double averageStay = logs.isEmpty() ? 0 : (totalStaySum / logs.size());
			double totalTime = Clock.getInstance().getTime();

			// 2. Build the simulation run object
			SimulationRun run = new SimulationRun(
					totalTime,
					logs.size(),
					averageStay,
					takeaway,
					dinein,
					online,
					engine.getOven1Utilization(),
					engine.getOven2Utilization()
			);

			// 3. Save to database using DAOs
			SimulationRunDAO runDAO = new SimulationRunDAO();
			CustomerLogDAO logDAO = new CustomerLogDAO();

			int runId = runDAO.createRun(run);

			if (runId != -1) {
				System.out.println("✅ Saved SimulationRun with ID: " + runId);

				for (CustomerLog log : logs) {
					log.setRunId(runId); // Connect the log to the run
					logDAO.saveCustomerLog(log);
				}

				System.out.println("Saved " + logs.size() + " customer logs.");
			} else {
				System.err.println("Failed to save simulation run to database.");
			}
		}).start();
	}

	// Retrieves all saved preset configurations from the database.
	public List<PresetConfig> getAllPresets() {
		return presetDAO.getAllPresets();
	}

	//Saves the current UI configuration as a new Preset in MariaDB.
	public boolean saveCurrentPreset(String presetName) {
		if (presetName == null || presetName.trim().isEmpty()) {
			return false;
		}

		double arrivalMean = parseArrivalPaceToMean(ui.getArrivalPace());

		PresetConfig config = new PresetConfig(
				presetName.trim(),
				arrivalMean,
				ui.getOven1Capacity(),
				ui.getOven2Capacity(),
				ui.getTableCapacity(),
				ui.getCourierCapacity(),
				ui.getDrinkProbability(),
				ui.getOven1ServiceTime(),
				ui.getOven2ServiceTime(),
				ui.getTableServiceTime(),
				ui.getCourierServiceTime()
		);

		int generatedId = presetDAO.savePreset(config);
		return generatedId != -1;
	}

	// Applies a selected PresetConfig's parameters back onto the UI inputs.
	public void applyPresetToUI(PresetConfig config) {
		if (config == null) return;

		Platform.runLater(() -> {
			ui.setDrinkProbability(config.getDrinkProbability());
			ui.setOven1Capacity(config.getOven1Capacity());
			ui.setOven2Capacity(config.getOven2Capacity());
			ui.setTableCapacity(config.getTableCapacity());
			ui.setCourierCapacity(config.getCourierCapacity());
			ui.setArrivalPace(convertMeanToArrivalPace(config.getArrivalMean()));
			ui.setOven1ServiceTime(config.getOven1ServiceTime());
			ui.setOven2ServiceTime(config.getOven2ServiceTime());
			ui.setTableServiceTime(config.getTableServiceTime());
			ui.setCourierServiceTime(config.getCourierServiceTime());
		});
	}

	// Deletes a preset configuration by ID.
	public void deletePreset(int configId) {
		presetDAO.deletePreset(configId);
	}

	// Helper converters for arrival pace mapping
	private double parseArrivalPaceToMean(String pace) {
		if ("Fast (2.5s)".equals(pace)) return 2.5;
		if ("Slow (10s)".equals(pace)) return 10.0;
		return 5.0; // Default / Normal
	}

	private String convertMeanToArrivalPace(double mean) {
		if (mean <= 3.0) return "Fast (2.5s)";
		if (mean >= 8.0) return "Slow (10s)";
		return "Normal (5s)";
	}
}
