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


/**
 * Controller class serving as the mediator between the JavaFX User Interface and the
 * underlying discrete-event simulation engine in accordance with the Model-View-Controller (MVC) pattern.
 * <p>
 * Handles user interactions, updates simulation parameters, controls thread lifecycle,
 * synchronizes visualization updates with the JavaFX Application Thread, and manages database
 * persistence via DAO implementations.
 * </p>
 *
 * @author Illia
 * @version 3.0
 */
public class Controller implements IControllerVtoM, IControllerMtoV {   // NEW
	private IEngine engine;
	private ISimulatorUI ui;
	private PresetConfigDAO presetDAO = new PresetConfigDAO();

	/**
	 * Constructs a Controller instance bound to the specified simulator user interface.
	 *
	 * @param ui The {@link ISimulatorUI} instance providing input values and visual components.
	 */
	public Controller(ISimulatorUI ui) {
		this.ui = ui;
	}


	/**
	 * Initializes and starts a new simulation run in a dedicated background thread.
	 * <p>
	 * Resets customer metrics, reads configuration values from the UI (arrival paces, service times,
	 * capacities, and customer distribution weights), configures the simulation engine and visualization
	 * canvas, and launches the engine execution thread.
	 * </p>
	 */
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


	/**
	 * Toggles the paused state of the simulation engine and synchronizes the UI visualization canvas state.
	 */
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


	/**
	 * Updates the simulation end time display on the JavaFX Application Thread.
	 *
	 * @param time The total simulated time duration completed.
	 */
	@Override
	public void showEndTime(double time) {
		Platform.runLater(()->ui.setEndingTime(time));
	}


	/**
	 * Relays customer movement and state transitions to the visualization canvas and UI event logs.
	 * <p>
	 * Ensures all visual manipulations and log appends are safely executed on the JavaFX Application Thread
	 * via {@link Platform#runLater(Runnable)}.
	 * </p>
	 *
	 * @param id     The unique identifier of the customer.
	 * @param type   The {@link CustomerType} representing the customer category (e.g., DINEIN, TAKEAWAY, ONLINE).
	 * @param action The state transition action string (e.g., "ARRIVED", "FINISHED").
	 */
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


	/**
	 * Decreases the simulation execution speed by increasing the step delay and adjusting the UI animation speed scale.
	 */
	@Override
	public void decreaseSpeed() {
		engine.setDelay((long)(engine.getDelay() * 1.10));

		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			((PizzaVisualisation) ui.getVisualisation()).adjustAnimationSpeed(0.85);
		}
	}


	/**
	 * Increases the simulation execution speed by reducing the step delay (clamped to a minimum of 10ms)
	 * and adjusting the UI animation speed scale.
	 */
	@Override
	public void increaseSpeed() {
		long newDelay = (long)(engine.getDelay() * 0.90);
		if (newDelay < 10) newDelay = 10;
		engine.setDelay(newDelay);

		if (ui.getVisualisation() instanceof PizzaVisualisation) {
			((PizzaVisualisation) ui.getVisualisation()).adjustAnimationSpeed(1.20);
		}
	}


	/**
	 * Halts the active simulation thread and resets the UI visualization canvas to its initial state.
	 */
	@Override
	public void stopSimulation() {
		if (engine instanceof MyEngine) {
			((MyEngine) engine).stopSimulation();
		}
		if (ui.getVisualisation() != null) {
			ui.getVisualisation().resetVisuals();
		}
	}


	/**
	 * Fetches all previously recorded simulation runs from the MariaDB database.
	 *
	 * @return A {@link List} of {@link SimulationRun} objects representing historical execution data.
	 */
	@Override
	public List<SimulationRun> getAllSimulationRuns() {
		SimulationRunDAO runDAO = new SimulationRunDAO();
		return runDAO.getAllRuns();
	}


	/**
	 * Forwards formatted simulation summary results to the UI text display.
	 *
	 * @param s The formatted string containing completed run statistics.
	 */
	@Override
	public void showResults(String s) {
		Platform.runLater(() -> {
			if (ui instanceof SimulatorGUI) {
				((SimulatorGUI) ui).displayResults(s);
			}
		});
	}


	/**
	 * Asynchronously calculates summary metrics and persists the completed simulation run data
	 * along with detailed individual customer logs into the database.
	 * <p>
	 * Runs in a separate thread to prevent database network latency from freezing the UI.
	 * </p>
	 *
	 * @param engine The {@link MyEngine} instance containing finished customer records and station performance statistics.
	 */
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


	/**
	 * Retrieves all saved parameter configuration presets from the database.
	 *
	 * @return A {@link List} of {@link PresetConfig} entities stored in MariaDB.
	 */
	public List<PresetConfig> getAllPresets() {
		return presetDAO.getAllPresets();
	}


	/**
	 * Persists the current configuration parameters set in the UI as a named preset in the database.
	 *
	 * @param presetName The unique name label assigned to the saved configuration preset.
	 * @return {@code true} if the preset was successfully saved; {@code false} if the name was empty or saving failed.
	 */
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


	/**
	 * Applies a selected preset configuration's values to the UI input controls on the JavaFX Application Thread.
	 *
	 * @param config The {@link PresetConfig} instance containing target parameter values.
	 */
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


	/**
	 * Deletes a specified preset configuration record from the database by its primary key ID.
	 *
	 * @param configId The unique identifier of the preset record to delete.
	 */
	public void deletePreset(int configId) {
		presetDAO.deletePreset(configId);
	}


	/**
	 * Converts a human-readable arrival pace dropdown choice string into its corresponding numeric mean value.
	 *
	 * @param pace The UI choice string (e.g., "Fast (2.5s)", "Slow (10s)").
	 * @return The double value representing arrival mean time.
	 */
	private double parseArrivalPaceToMean(String pace) {
		if ("Fast (2.5s)".equals(pace)) return 2.5;
		if ("Slow (10s)".equals(pace)) return 10.0;
		return 5.0; // Default / Normal
	}


	/**
	 * Converts a numeric arrival mean time into its corresponding UI choice string representation.
	 *
	 * @param mean The numeric arrival mean value in seconds.
	 * @return The formatted string representation for the UI dropdown menu.
	 */
	private String convertMeanToArrivalPace(double mean) {
		if (mean <= 3.0) return "Fast (2.5s)";
		if (mean >= 8.0) return "Slow (10s)";
		return "Normal (5s)";
	}
}
