package org.example.view;

import java.text.DecimalFormat;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.controller.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.*;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.*;
import org.example.model.SimulationRun;
import org.example.simu.framework.Trace;
import org.example.simu.framework.Trace.Level;
import org.example.simu.model.Customer;
import org.example.model.PresetConfig;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.Priority;
import javafx.scene.image.Image;


/**
 * Main JavaFX GUI application class for the pizzeria simulation, implementing {@link ISimulatorUI}.
 * <p>
 * Renders the primary user interface consisting of interactive parameter configuration sidebars,
 * dynamic animation canvas visualization, execution control buttons, live performance metrics displays,
 * and database historical run logs table dialogs.
 * </p>
 *
 * @author Anja, Illia
 * @version 1.0
 */
public class SimulatorGUI extends Application implements ISimulatorUI {

	/** Presenter/Controller reference interface {@link IControllerVtoM} connecting view interactions to model logic. */
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

	private Spinner<Double> spinOven1Time, spinOven2Time, spinTableTime, spinCourierTime;

	/**
	 * Initializes background framework configurations and instantiates the presenter controller.
	 */
	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller(this);
	}

	/**
	 * Constructs and displays the primary JavaFX application window, scene graphs, custom CSS styling, and layouts.
	 *
	 * @param primaryStage Primary stage window container provided by the JavaFX runtime.
	 */
	@Override
	public void start(Stage primaryStage) {
		try {
			primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.jfif")));
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
			mainLayout.setStyle("-fx-border-color: #aeaeae; -fx-border-width: 3; -fx-border-style: solid;");
			mainLayout.setPadding(new Insets(10));
			mainLayout.getChildren().addAll(parameterSidebar, (Canvas) display, statsSidebar);

			Scene scene = new Scene(mainLayout);
			String cssData =
					".label { -fx-font-family: 'Comic Sans MS'; -fx-text-fill: #4a3b52; }\n" +

							".slider .thumb { " +
							"    -fx-background-color: #8e44ad; " +
							"    -fx-background-radius: 10px; " +
							"}\n" +

							".slider .filled-track { " +
							"    -fx-background-color: #d1b3ff; " +
							"    -fx-background-insets: 0; " +
							"}\n" +

							".slider .track { " +
							"    -fx-background-color: #f0ebf7; " +
							"    -fx-background-insets: 0; " +
							"}\n" +

							".slider .axis .axis-tick-label { " +
							"    -fx-font-family: 'Comic Sans MS'; " +
							"    -fx-font-size: 9px; " +
							"    -fx-fill: #7d6e85; " +
							"}\n" ;

			String encodedCss = "data:text/css," + cssData.replace(" ", "%20").replace("\n", "%0A");
			scene.getStylesheets().add(encodedCss);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructs the left-hand parameters input sidebar container containing preset, station, customer, and execution controls.
	 *
	 * @return Formatted {@link VBox} container node.
	 */
	private VBox createParameterSidebar() {
		VBox panel = new VBox(12);
		panel.setPadding(new Insets(10));
		panel.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dcdcdc; -fx-border-width: 1;" + "-fx-font-family: 'Comic Sans MS'");
		panel.setPrefWidth(260);

		// --- SECTION 0: PRESET CONFIGURATIONS ---
		Label presetHeader = createSectionHeader("Preset Configurations");

		ComboBox<PresetConfig> presetComboBox = new ComboBox<>();
		presetComboBox.setPromptText("Select Preset...");
		presetComboBox.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(presetComboBox, Priority.ALWAYS);

		presetComboBox.setConverter(new javafx.util.StringConverter<PresetConfig>() {
			@Override
			public String toString(PresetConfig config) {
				return config != null ? config.getConfigName() : "";
			}

			@Override
			public PresetConfig fromString(String string) {
				return null;
			}
		});

		// Load saved presets from DB on UI creation
		presetComboBox.getItems().add(PresetConfig.DEFAULT);
		presetComboBox.getItems().addAll(controller.getAllPresets());

		// Apply selected preset values to UI controls
		presetComboBox.setOnAction(e -> {
			PresetConfig selected = presetComboBox.getValue();
			if (selected != null) {
				controller.applyPresetToUI(selected);
			}
		});

		// Save Current UI Settings as a Preset Button
		Button savePresetButton = new Button("Save");
		savePresetButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
		savePresetButton.setOnAction(e -> {
			TextInputDialog dialog = new TextInputDialog();
			dialog.setTitle("Save Preset");
			dialog.setHeaderText("Save current UI settings as a preset configuration");
			dialog.setContentText("Preset Name:");

			dialog.showAndWait().ifPresent(presetName -> {
				if (!presetName.trim().isEmpty()) {
					boolean success = controller.saveCurrentPreset(presetName.trim());
					if (success) {
						presetComboBox.getItems().setAll(controller.getAllPresets());

						presetComboBox.getItems().stream()
								.filter(p -> p.getConfigName().equalsIgnoreCase(presetName.trim()))
								.findFirst()
								.ifPresent(presetComboBox::setValue);

						Alert alert = new Alert(Alert.AlertType.INFORMATION, "Preset saved successfully!", ButtonType.OK);
						alert.showAndWait();
					} else {
						Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save preset to database.", ButtonType.OK);
						alert.showAndWait();
					}
				}
			});
		});

		HBox presetControlsBox = new HBox(6, presetComboBox, savePresetButton);

		// Manual layout without changing createLabeledControl
		Label presetLabel = new Label("Load / Save Presets");
		presetLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 11));

		VBox presetBox = new VBox(6,
				presetHeader,
				new VBox(2, presetLabel, presetControlsBox)
		);

		// --- SECTION 1: STATIONS PARAMETERS ---
		Label stationHeader = createSectionHeader("Capacity & Service Time");

		oven1Slider = createSnapSlider(2, 12, 4);
		oven2Slider = createSnapSlider(2, 12, 4);
		tableSlider = createSnapSlider(5, 15, 10);
		courierSlider = createSnapSlider(1, 5, 3);

		oven1Slider.setPrefWidth(160);
		oven2Slider.setPrefWidth(160);
		tableSlider.setPrefWidth(160);
		courierSlider.setPrefWidth(160);

		// Initialize the 4 spinners
		spinOven1Time = createServiceTimeSpinner(10.0);
		spinOven2Time = createServiceTimeSpinner(10.0);
		spinTableTime = createServiceTimeSpinner(15.0);
		spinCourierTime = createServiceTimeSpinner(15.0);

		spinOven1Time.setPrefWidth(70);
		spinOven2Time.setPrefWidth(70);
		spinTableTime.setPrefWidth(70);
		spinCourierTime.setPrefWidth(70);

		// Group Slider and Spinner vertically using VBox
		VBox stationBox = new VBox(6,
				stationHeader,
				createLabeledControl("Oven 1 Cap & Time", new HBox(5, oven1Slider, spinOven1Time)),
				createLabeledControl("Oven 2 Cap & Time", new HBox(5, oven2Slider, spinOven2Time)),
				createLabeledControl("Tables Cap & Time", new HBox(5, tableSlider, spinTableTime)),
				createLabeledControl("Couriers Cap & Time", new HBox(5, courierSlider, spinCourierTime))
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

		drinkProbabilitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
			Customer.setDrinkProbability(newVal.doubleValue() / 100.0);
		});

		dineInMixSlider = createMixSlider(40);
		takeawayMixSlider = createMixSlider(40);
		onlineMixSlider = createMixSlider(20);

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

		time = new TextField("100");
		delay = new TextField("1000");		// These are close to real-life timestamps

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

		// Assemble all sections into panel in correct order:
		panel.getChildren().addAll(
				presetBox,
				new Separator(),
				stationBox,
				new Separator(),
				customerBox,
				new Separator(),
				execBox
		);

		presetComboBox.setValue(PresetConfig.DEFAULT);
		controller.applyPresetToUI(PresetConfig.DEFAULT);

		ScrollPane scrollPane = new ScrollPane(panel);
		scrollPane.setFitToWidth(true);
		scrollPane.setFitToHeight(true);
		return new VBox(scrollPane);
	}

	/**
	 * Constructs the right-hand statistics sidebar container housing real-time metrics, log outputs, and history triggers.
	 *
	 * @return Formatted {@link VBox} container node.
	 */
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
		statsTextArea.setPrefHeight(650);
		statsTextArea.setStyle("-fx-background-color: #8f5de4; -fx-border-color: #151515; -fx-border-width: 1;");

		// History Button placed beneath the stats text area
		Button historyButton = new Button("View Run History");
		historyButton.setStyle("-fx-background-color: #673AB7; -fx-text-fill: white; -fx-font-weight: bold;");
		historyButton.setMaxWidth(Double.MAX_VALUE);
		historyButton.setOnAction(e -> openHistoryWindow());

		statsBox.getChildren().addAll(
				statsHeader,
				customerCountLabel,
				totalTimeLabel,
				statsTextArea,
				historyButton
		);
		return statsBox;
	}

	/**
	 * Opens a new modal window displaying historical simulation runs stored in the database within a {@link TableView}.
	 */
	public void openHistoryWindow() {
		Stage historyStage = new Stage();
		historyStage.setTitle("Simulation Run History");
		historyStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.jfif")));

		TableView<SimulationRun> table = new TableView<>();

		// Define Columns mapped to SimulationRun getter property names
		TableColumn<SimulationRun, Integer> colId = new TableColumn<>("Run ID");
		colId.setCellValueFactory(new PropertyValueFactory<>("runId"));

		TableColumn<SimulationRun, Double> colTime = new TableColumn<>("Total Time");
		colTime.setCellValueFactory(new PropertyValueFactory<>("simulationTime"));

		TableColumn<SimulationRun, Integer> colCustomers = new TableColumn<>("Total Customers");
		colCustomers.setCellValueFactory(new PropertyValueFactory<>("totalCustomers"));

		TableColumn<SimulationRun, Double> colAvgStay = new TableColumn<>("Avg Stay Time");
		colAvgStay.setCellValueFactory(new PropertyValueFactory<>("averageStayTime"));

		TableColumn<SimulationRun, Integer> colDineIn = new TableColumn<>("Dine-In");
		colDineIn.setCellValueFactory(new PropertyValueFactory<>("dineinCount"));

		TableColumn<SimulationRun, Integer> colTakeaway = new TableColumn<>("Takeaway");
		colTakeaway.setCellValueFactory(new PropertyValueFactory<>("takeawayCount"));

		TableColumn<SimulationRun, Integer> colOnline = new TableColumn<>("Online");
		colOnline.setCellValueFactory(new PropertyValueFactory<>("onlineCount"));

		TableColumn<SimulationRun, Double> colOven1 = new TableColumn<>("Oven 1 Util (%)");
		colOven1.setCellValueFactory(new PropertyValueFactory<>("oven1Utilization"));

		TableColumn<SimulationRun, Double> colOven2 = new TableColumn<>("Oven 2 Util (%)");
		colOven2.setCellValueFactory(new PropertyValueFactory<>("oven2Utilization"));

		formatDoubleColumn(colTime);
		formatDoubleColumn(colAvgStay);
		formatDoubleColumn(colOven1);
		formatDoubleColumn(colOven2);

		table.getColumns().addAll(
				colId, colTime, colCustomers, colAvgStay,
				colDineIn, colTakeaway, colOnline, colOven1, colOven2
		);

		// Refresh Data from Controller/DAO
		Button refreshBtn = new Button("Refresh History");
		refreshBtn.setOnAction(e -> {
			table.setItems(FXCollections.observableArrayList(controller.getAllSimulationRuns()));
		});

		// Initial Load
		table.setItems(FXCollections.observableArrayList(controller.getAllSimulationRuns()));

		VBox layout = new VBox(10, table, refreshBtn);
		layout.setPadding(new Insets(10));
		VBox.setVgrow(table, Priority.ALWAYS);

		Scene scene = new Scene(layout, 800, 450);
		historyStage.setScene(scene);
		historyStage.show();
	}


	/**
	 * Creates a standardized bold section header label.
	 *
	 * @param title Header text string to display.
	 * @return Formatted section header {@link Label}.
	 */
	private Label createSectionHeader(String title) {
		Label label = new Label(title);
		label.setFont(Font.font("Tahoma", FontWeight.BOLD, 13));
		return label;
	}

	/**
	 * Wraps a generic UI node with a descriptive label in a vertical box container.
	 *
	 * @param labelText Descriptive label text.
	 * @param control   Child control node.
	 * @return Grouped {@link VBox} container.
	 */
	private VBox createLabeledControl(String labelText, javafx.scene.Node control) {
		Label label = new Label(labelText);
		label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 11));
		return new VBox(2, label, control);
	}

	/**
	 * Wraps a UI control with a descriptive label in a vertical box container.
	 *
	 * @param labelText Descriptive label text.
	 * @param control   Child control component.
	 * @return Grouped {@link VBox} container.
	 */
	private VBox createLabeledControl(String labelText, Control control) {
		Label label = new Label(labelText);
		label.setFont(Font.font("Tahoma", FontWeight.NORMAL, 11));
		return new VBox(2, label, control);
	}

	/**
	 * Constructs a tick-snapping slider for integer capacity inputs.
	 *
	 * @param min   Minimum slider threshold value.
	 * @param max   Maximum slider threshold value.
	 * @param value Default initial value.
	 * @return Configured {@link Slider} instance.
	 */
	private Slider createSnapSlider(double min, double max, double value) {
		Slider slider = new Slider(min, max, value);
		slider.setMajorTickUnit(1);
		slider.setMinorTickCount(0);
		slider.setSnapToTicks(true);
		slider.setShowTickMarks(true);
		slider.setShowTickLabels(true);
		return slider;
	}

	/**
	 * Constructs a percentage mix slider for customer demographic weighting.
	 *
	 * @param defaultValue Default initial ratio percentage value.
	 * @return Configured {@link Slider} instance.
	 */
	private Slider createMixSlider(double defaultValue) {
		Slider slider = new Slider(0, 100, defaultValue);
		slider.setMajorTickUnit(50);
		slider.setShowTickMarks(true);
		return slider;
	}

	/**
	 * Constructs a numerical spinner control for workstation service duration inputs.
	 *
	 * @param defaultValue Default initial mean service duration value.
	 * @return Configured {@link Spinner} instance.
	 */
	private Spinner<Double> createServiceTimeSpinner(double defaultValue) {
		Spinner<Double> spinner = new Spinner<>(1.0, 60.0, defaultValue, 0.5);
		spinner.setEditable(true);
		spinner.setPrefWidth(70);
		return spinner;
	}

	/**
	 * Applies custom decimal formatting (2 decimal places) to floating-point table view columns.
	 *
	 * @param <T>    Generic table model entity type.
	 * @param column Target table column to format.
	 */
	private <T> void formatDoubleColumn(TableColumn<T, Double> column) {
		column.setCellFactory(tc -> new TableCell<>() {
			@Override
			protected void updateItem(Double item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) {
					setText(null);
				} else {
					setText(String.format(java.util.Locale.US, "%.2f", item));
				}
			}
		});
	}


	/**
	 * Gets target simulation duration from input text fields.
	 *
	 * @return Double simulation time value.
	 */
	@Override
	public double getTime() {
		return Double.parseDouble(time.getText());
	}

	/**
	 * Gets execution step delay in milliseconds from input text fields.
	 *
	 * @return Long millisecond delay value.
	 */
	@Override
	public long getDelay() {
		return Long.parseLong(delay.getText());
	}

	/**
	 * Gets drink selection probability ratio from the UI slider.
	 *
	 * @return Double probability threshold (0.0 to 1.0).
	 */
	@Override
	public double getDrinkProbability() {
		return drinkProbabilitySlider.getValue() / 100.0;
	}

	/**
	 * Gets Oven 1 parallel capacity setting.
	 *
	 * @return Oven 1 capacity limit.
	 */
	@Override
	public int getOven1Capacity() {
		return (int) oven1Slider.getValue();
	}

	/**
	 * Gets Oven 2 parallel capacity setting.
	 *
	 * @return Oven 2 capacity limit.
	 */
	@Override
	public int getOven2Capacity() {
		return (int) oven2Slider.getValue();
	}

	/**
	 * Gets dining tables seating capacity setting.
	 *
	 * @return Tables seating capacity limit.
	 */
	@Override
	public int getTableCapacity() {
		return (int) tableSlider.getValue();
	}

	/**
	 * Gets delivery courier active capacity setting.
	 *
	 * @return Courier capacity limit.
	 */
	@Override
	public int getCourierCapacity() {
		return (int) courierSlider.getValue();
	}

	/**
	 * Gets selected arrival pace configuration label string.
	 *
	 * @return Arrival pace selection string.
	 */
	@Override
	public String getArrivalPace() {
		return paceChoiceBox.getValue();
	}

	/**
	 * Gets relative dine-in customer distribution weight.
	 *
	 * @return Dine-in weight ratio.
	 */
	@Override
	public double getDineInWeight() {
		return dineInMixSlider.getValue();
	}

	/**
	 * Gets relative takeaway customer distribution weight.
	 *
	 * @return Takeaway weight ratio.
	 */
	@Override
	public double getTakeawayWeight() {
		return takeawayMixSlider.getValue();
	}

	/**
	 * Gets mean service duration value for Oven 1.
	 *
	 * @return Oven 1 mean service time.
	 */
	@Override
	public double getOven1ServiceTime() {
		return spinOven1Time.getValue();
	}

	/**
	 * Gets mean service duration value for Oven 2.
	 *
	 * @return Oven 2 mean service time.
	 */
	@Override
	public double getOven2ServiceTime() {
		return spinOven2Time.getValue();
	}

	/**
	 * Gets mean service duration value for dining tables.
	 *
	 * @return Tables mean service time.
	 */
	@Override
	public double getTableServiceTime() {
		return spinTableTime.getValue();
	}

	/**
	 * Gets mean service duration value for delivery couriers.
	 *
	 * @return Courier mean service time.
	 */
	@Override
	public double getCourierServiceTime() {
		return spinCourierTime.getValue();
	}

	/**
	 * Sets Oven 1 service duration spinner input value.
	 *
	 * @param time Service duration value to set.
	 */
	@Override
	public void setOven1ServiceTime(double time) {
		if (spinOven1Time != null) spinOven1Time.getValueFactory().setValue(time);
	}

	/**
	 * Sets Oven 2 service duration spinner input value.
	 *
	 * @param time Service duration value to set.
	 */
	@Override
	public void setOven2ServiceTime(double time) {
		if (spinOven2Time != null) spinOven2Time.getValueFactory().setValue(time);
	}

	/**
	 * Sets dining tables service duration spinner input value.
	 *
	 * @param time Service duration value to set.
	 */
	@Override
	public void setTableServiceTime(double time) {
		if (spinTableTime != null) spinTableTime.getValueFactory().setValue(time);
	}

	/**
	 * Sets delivery couriers service duration spinner input value.
	 *
	 * @param time Service duration value to set.
	 */
	@Override
	public void setCourierServiceTime(double time) {
		if (spinCourierTime != null) spinCourierTime.getValueFactory().setValue(time);
	}

	/**
	 * Gets relative online delivery customer distribution weight.
	 *
	 * @return Online delivery weight ratio.
	 */
	@Override
	public double getOnlineWeight() {
		return onlineMixSlider.getValue();
	}

	/**
	 * Retrieves the canvas visualization component reference.
	 *
	 * @return The active {@link IVisualisation} canvas component.
	 */
	@Override
	public IVisualisation getVisualisation() {
		return display;
	}

	/**
	 * Resets execution control buttons back to initial idle states upon simulation end or stop.
	 */
	private void resetControlButtons() {
		startButton.setText("Start Simulation");
		startButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

		pauseButton.setDisable(true);
		pauseButton.setText("Pause");
		pauseButton.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold;");
	}

	/**
	 * Formats and updates the total execution duration output label upon simulation finish.
	 *
	 * @param time Final elapsed simulation time.
	 */
	@Override
	public void setEndingTime(double time) {
		DecimalFormat formatter = new DecimalFormat("#0.00");
		Platform.runLater(() -> {
			this.totalTimeLabel.setText("Total execution time: " + formatter.format(time));
			resetControlButtons();
		});
	}

	/**
	 * Increments active customer count tracking inside the system and refreshes UI labels.
	 */
	public void incrementCustomerCount() {
		totalCustomersCount++;
		updateStatisticsUI();
	}

	/**
	 * Decrements active customer count tracking inside the system and refreshes UI labels.
	 */
	public void decrementCustomerCount() {
		totalCustomersCount--;
		updateStatisticsUI();
	}

	/**
	 * Refreshes active customer count display label text.
	 */
	private void updateStatisticsUI() {
		customerCountLabel.setText("Customers inside: " + totalCustomersCount);
	}

	/**
	 * Appends an event text message line to the right sidebar statistics text area.
	 *
	 * @param text Message line text to append.
	 */
	public void logEvent(String text) {
		if (statsTextArea != null) {
			statsTextArea.appendText(text + "\n");
		}
	}

	/**
	 * Sets full report text into the right sidebar statistics text area.
	 *
	 * @param resultsText Complete formatted performance report string.
	 */
	public void displayResults(String resultsText) {
		if (statsTextArea != null) {
			statsTextArea.setText(resultsText);
		}
	}

	/**
	 * Sets drink probability slider position converted to percentage scale.
	 *
	 * @param value Decimal probability threshold (0.0 to 1.0).
	 */
	@Override
	public void setDrinkProbability(double value) {
		// Converts decimal probability (e.g., 0.50) to slider scale (0 - 100)
		drinkProbabilitySlider.setValue(value <= 1.0 ? value * 100.0 : value);
	}

	/**
	 * Sets Oven 1 capacity slider position.
	 *
	 * @param capacity Oven 1 capacity limit to set.
	 */
	@Override
	public void setOven1Capacity(int capacity) {
		oven1Slider.setValue(capacity);
	}

	/**
	 * Sets Oven 2 capacity slider position.
	 *
	 * @param capacity Oven 2 capacity limit to set.
	 */
	@Override
	public void setOven2Capacity(int capacity) {
		oven2Slider.setValue(capacity);
	}

	/**
	 * Sets dining tables capacity slider position.
	 *
	 * @param capacity Tables seating capacity limit to set.
	 */
	@Override
	public void setTableCapacity(int capacity) {
		tableSlider.setValue(capacity);
	}

	/**
	 * Sets delivery courier capacity slider position.
	 *
	 * @param capacity Courier capacity limit to set.
	 */
	@Override
	public void setCourierCapacity(int capacity) {
		courierSlider.setValue(capacity);
	}

	/**
	 * Sets customer arrival pace choice box selection.
	 *
	 * @param pace Arrival pace descriptive string to set.
	 */
	@Override
	public void setArrivalPace(String pace) {
		paceChoiceBox.setValue(pace);
	}

	/**
	 * Application entry point launching the JavaFX runtime framework.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}
}
