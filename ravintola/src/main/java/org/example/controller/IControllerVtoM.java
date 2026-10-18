package org.example.controller;

import org.example.model.PresetConfig;
import org.example.model.SimulationRun;

import java.util.List;

/**
 * Controller interface exposed to the user interface (View-to-Model).
 * <p>
 * Defines the contract through which the JavaFX UI layer triggers simulation execution controls
 * (start, stop, pause, speed adjustment), manages preset parameter configurations, and retrieves
 * historical run logs from database persistence.
 * </p>
 *
 * @author Illia, Niklas, Anja
 * @version 3.0
 */
public interface IControllerVtoM {

	/**
	 * Initializes engine parameters from UI controls and starts simulation thread execution.
	 */
	public void startSimulation();

	/**
	 * Terminates the active simulation execution thread and resets run duration parameters.
	 */
	public void stopSimulation();

	/**
	 * Increases simulation execution speed by reducing event loop delay times and accelerating visual animations.
	 */
	public void increaseSpeed();

	/**
	 * Decreases simulation execution speed by increasing event loop delay times and slowing visual animations.
	 */
	public void decreaseSpeed();

	/**
	 * Toggles execution thread pause and resume states.
	 */
	public void togglePause();

	/**
	 * Retrieves all saved preset configuration records from the database.
	 *
	 * @return A {@link List} of stored {@link PresetConfig} entities.
	 */
	List<PresetConfig> getAllPresets();

	/**
	 * Captures current UI control values and persists them as a named parameter preset in the database.
	 *
	 * @param presetName Unique descriptive name for the preset configuration.
	 * @return {@code true} if successfully saved to database; {@code false} otherwise.
	 */
	boolean saveCurrentPreset(String presetName);

	/**
	 * Applies the values of a specified preset configuration to the corresponding UI input controls.
	 *
	 * @param config The {@link PresetConfig} instance containing parameters to load into the UI.
	 */
	void applyPresetToUI(PresetConfig config);

	/**
	 * Deletes a preset configuration from the database by its primary key identifier.
	 *
	 * @param configId Unique primary key ID of the preset record to remove.
	 */
	void deletePreset(int configId);

	/**
	 * Retrieves all historical simulation run summaries from the database.
	 *
	 * @return A {@link List} of stored {@link SimulationRun} entities.
	 */
	List<SimulationRun> getAllSimulationRuns();
}
