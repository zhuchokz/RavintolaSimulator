package org.example.controller;

import org.example.model.PresetConfig;
import org.example.model.SimulationRun;

import java.util.List;

/* interface for the UI */
public interface IControllerVtoM {
		public void startSimulation();
		public void stopSimulation();
		public void increaseSpeed();
		public void decreaseSpeed();
		public void togglePause();

		List<PresetConfig> getAllPresets();
		boolean saveCurrentPreset(String presetName);
		void applyPresetToUI(PresetConfig config);
		void deletePreset(int configId);

		List<SimulationRun> getAllSimulationRuns();
}
