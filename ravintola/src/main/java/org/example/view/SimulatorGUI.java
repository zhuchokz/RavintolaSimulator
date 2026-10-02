package org.example.view;

import java.text.DecimalFormat;

import javafx.beans.value.ChangeListener;
import org.example.controller.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import org.example.simu.framework.Trace;
import org.example.simu.framework.Trace.Level;
import org.example.simu.model.Customer;

public class SimulatorGUI extends Application implements ISimulatorUI {

	private IControllerVtoM controller;

	private TextField time;
	private TextField delay;

	private TextArea statsTextArea;

	private Button startButton, pauseButton;
	private Button slowButton;
	private Button speedUpButton;

	private IVisualisation display;

	private int totalCustomersCount = 0;
	private Label customerCountLabel;
	private Label totalTimeLabel;

	private Slider drinkProbabilitySlider;
	private Slider oven1Slider, oven2Slider, tableSlider, courierSlider;
	private ChoiceBox<String> paceChoiceBox;
	private Slider dineInMixSlider, takeawayMixSlider, onlineMixSlider;

	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller(this);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.setTitle("Pizzeria Simulator");
			primaryStage.setOnCloseRequest(t -> {
				Platform.exit();
				System.exit(0);
			});

			// 1. Build components
			VBox parameterSidebar = createParameterSidebar();
			VBox statsSidebar = createStatsSidebar();
			display = new PizzaVisualisation(1242, 700);

			// 2. Root Layout (Left: Sidebar, Center: Canvas, Right: Stats)
			HBox mainLayout = new HBox(15);
			mainLayout.setPadding(new Insets(10));
			mainLayout.getChildren().addAll(parameterSidebar, (Canvas) display, statsSidebar);

			Scene scene = new Scene(mainLayout);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// -------------------------------------------------------------------------
	// LEFT PARAMETER SIDEBAR LAYOUT
	// -------------------------------------------------------------------------
	private VBox createParameterSidebar() {
		VBox panel = new VBox(12);
		panel.setPadding(new Insets(10));
		panel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dcdcdc; -fx-border-width: 1;");
		panel.setPrefWidth(260);

		// --- SECTION 1: STATIONS PARAMETERS ---
		Label stationHeader = createSectionHeader("Station Capacities");
		oven1Slider = createSnapSlider(2, 12, 4);
		oven2Slider = createSnapSlider(2, 12, 4);
		tableSlider = createSnapSlider(5, 15, 10);
		courierSlider = createSnapSlider(1, 5, 3);

		VBox stationBox = new VBox(6,
				stationHeader,
				createLabeledControl("Oven 1 Capacity", oven1Slider),
				createLabeledControl("Oven 2 Capacity", oven2Slider),
				createLabeledControl("Tables Capacity", tableSlider),
				createLabeledControl("Couriers Capacity", courierSlider)
		);

		// --- SECTION 2: CUSTOMERS & DEMOGRAPHICS ---
		Label customerHeader = createSectionHeader("Customer Parameters");

		paceChoiceBox = new ChoiceBox<>();
		paceChoiceBox.getItems().addAll("Fast (2.5s)", "Normal (5.0s)", "Slow (10s)");
		paceChoiceBox.setValue("Normal (5.0s)");
		paceChoiceBox.setMaxWidth(Double.MAX_VALUE);

		drinkProbabilitySlider = new Slider(0, 100, 50);
		drinkProbabilitySlider.setMajorTickUnit(50);
		drinkProbabilitySlider.setMinorTickCount(0);
		drinkProbabilitySlider.setSnapToTicks(true);
		drinkProbabilitySlider.setShowTickMarks(true);
		drinkProbabilitySlider.setShowTickLabels(true);

		// Attach live listener for drink probability
		drinkProbabilitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			Customer.setDrinkProbability(newVal.doubleValue() / 100.0);
		});

		// 3 Independent Type Mix Sliders
		dineInMixSlider = createMixSlider(40);
		takeawayMixSlider = createMixSlider(40);
		onlineMixSlider = createMixSlider(20);

		// Attach live listener for customer weight mix
		ChangeListener<Number> mixListener = (obs, oldVal, newVal) -> {
			Customer.setCustomerWeights(
					dineInMixSlider.getValue(),
					takeawayMixSlider.getValue(),
					onlineMixSlider.getValue()
			);
		};
		dineInMixSlider.valueProperty().addListener(mixListener);
		takeawayMixSlider.valueProperty().addListener(mixListener);
		onlineMixSlider.valueProperty().addListener(mixListener);

		VBox customerBox = new VBox(6,
				customerHeader,
				createLabeledControl("Arrival Pace", paceChoiceBox),
				createLabeledControl("Dine-In Drink Chance", drinkProbabilitySlider),
				createLabeledControl("Dine-In Weight", dineInMixSlider),
				createLabeledControl("Takeaway Weight", takeawayMixSlider),
				createLabeledControl("Online Weight", onlineMixSlider)
		);

		// --- SECTION 3: TIME & EXECUTION CONTROLS ---
		Label execHeader = createSectionHeader("Time & Execution");

		time = new TextField("1000");
		delay = new TextField("100");

		startButton = new Button("Start Simulation");
		startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
		startButton.setMaxWidth(Double.MAX_VALUE);
		startButton.setOnAction(event -> {
			if ("Start Simulation".equals(startButton.getText())) {
				totalCustomersCount = 0;
				updateStatisticsUI();
				if (statsTextArea != null) {
					statsTextArea.clear();
				}

				controller.startSimulation();

				startButton.setText("Stop Simulation");
				startButton.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-weight: bold;");

				pauseButton.setDisable(false);
			} else {
				// --- STOP SIMULATION ---
				controller.stopSimulation();
				resetControlButtons();
			}
		});

		pauseButton = new Button("Pause");
		pauseButton.setDisable(true);
		pauseButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
		pauseButton.setMaxWidth(Double.MAX_VALUE);
		pauseButton.setOnAction(e -> {
			controller.togglePause();
			if ("Pause".equals(pauseButton.getText())) {
				pauseButton.setText("Resume");
				pauseButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
			} else {
				pauseButton.setText("Pause");
				pauseButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
			}
		});

		slowButton = new Button("Slow Down");
		slowButton.setOnAction(e -> controller.decreaseSpeed());

		speedUpButton = new Button("Speed Up");
		speedUpButton.setOnAction(e -> controller.increaseSpeed());

		HBox speedBox = new HBox(5, slowButton, speedUpButton);
		HBox.setHgrow(slowButton, Priority.ALWAYS);
		HBox.setHgrow(speedUpButton, Priority.ALWAYS);
		slowButton.setMaxWidth(Double.MAX_VALUE);
		speedUpButton.setMaxWidth(Double.MAX_VALUE);

		HBox runBox = new HBox(5, startButton, pauseButton);
		HBox.setHgrow(startButton, Priority.ALWAYS);
		HBox.setHgrow(pauseButton, Priority.ALWAYS);

		VBox execBox = new VBox(6,
				execHeader,
				createLabeledControl("Simulation Time:", time),
				createLabeledControl("Delay (ms):", delay),
				runBox,
				speedBox
		);

		panel.getChildren().addAll(stationBox, new Separator(), customerBox, new Separator(), execBox);

		ScrollPane scrollPane = new ScrollPane(panel);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		return new VBox(scrollPane);
	}

	// -------------------------------------------------------------------------
	// RIGHT STATISTICS SIDEBAR LAYOUT
	// -------------------------------------------------------------------------
	private VBox createStatsSidebar() {
		VBox statsBox = new VBox(10);
		statsBox.setPrefWidth(220);
		statsBox.setPadding(new Insets(5));

		Label statsHeader = createSectionHeader("--- STATISTICS ---");
		customerCountLabel = new Label("Customers inside: 0");
		customerCountLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
		customerCountLabel.setStyle("-fx-text-fill: #0000ff;");

		totalTimeLabel = new Label("Total execution time: -");
		totalTimeLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 14));
		totalTimeLabel.setStyle("-fx-text-fill: #008000;");

		statsTextArea = new TextArea();
		statsTextArea.setEditable(false);
		statsTextArea.setWrapText(true);
		statsTextArea.setPrefHeight(450);

		statsBox.getChildren().addAll(statsHeader, customerCountLabel, totalTimeLabel, statsTextArea);
		return statsBox;
	}

	// -------------------------------------------------------------------------
	// HELPER CREATORS
	// -------------------------------------------------------------------------
	private Label createSectionHeader(String title) {
		Label label = new Label(title);
		label.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
		return label;
	}

	private VBox createLabeledControl(String labelText, Control control) {
		Label label = new Label(labelText);
		label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 11));
		return new VBox(2, label, control);
	}

	private Slider createSnapSlider(double min, double max, double value) {
		Slider slider = new Slider(min, max, value);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setSnapToTicks(true);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		return slider;
	}

	private Slider createMixSlider(double defaultValue) {
		Slider slider = new Slider(0, 100, defaultValue);
		slider.setMajorTickUnit(50);
		slider.setShowTickMarks(true);
		return slider;
	}

	// -------------------------------------------------------------------------
	// ISimulatorUI INTERFACE IMPLEMENTATION
	// -------------------------------------------------------------------------
	@Override
	public double getTime() {
		return Double.parseDouble(time.getText());
	}

	@Override
	public long getDelay() {
		return Long.parseLong(delay.getText());
	}

	@Override
	public double getDrinkProbability() {
		return drinkProbabilitySlider.getValue() / 100.0;
	}

	@Override
	public int getOven1Capacity() {
		return (int) oven1Slider.getValue();
	}

	@Override
	public int getOven2Capacity() {
		return (int) oven2Slider.getValue();
	}

	@Override
	public int getTableCapacity() {
		return (int) tableSlider.getValue();
	}

	@Override
	public int getCourierCapacity() {
		return (int) courierSlider.getValue();
	}

	@Override
	public String getArrivalPace() {
		return paceChoiceBox.getValue();
	}

	@Override
	public double getDineInWeight() {
		return dineInMixSlider.getValue();
	}

	@Override
	public double getTakeawayWeight() {
		return takeawayMixSlider.getValue();
	}

	@Override
	public double getOnlineWeight() {
		return onlineMixSlider.getValue();
	}

	@Override
	public IVisualisation getVisualisation() {
		return display;
	}

	private void resetControlButtons() {
		startButton.setText("Start Simulation");
		startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

		pauseButton.setDisable(true);
		pauseButton.setText("Pause");
		pauseButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
	}

	@Override
	public void setEndingTime(double time) {
		DecimalFormat formatter = new DecimalFormat("#0.00");
		Platform.runLater(() -> {
			this.totalTimeLabel.setText("Total execution time: " + formatter.format(time));
			resetControlButtons();
		});
	}

	public void incrementCustomerCount() {
		totalCustomersCount++;
		updateStatisticsUI();
	}

	public void decrementCustomerCount() {
		totalCustomersCount--;
		updateStatisticsUI();
	}

	private void updateStatisticsUI() {
		customerCountLabel.setText("Customers inside: " + totalCustomersCount);
	}

	public void logEvent(String text) {
		if (statsTextArea != null) {
			statsTextArea.appendText(text + "\n");
		}
	}

	public void displayResults(String resultsText) {
		if (statsTextArea != null) {
			statsTextArea.setText(resultsText);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
