package org.example.view;

import java.text.DecimalFormat;
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

public class SimulatorGUI extends Application implements ISimulatorUI {

	private IControllerVtoM controller;

	private TextField time;
	private TextField delay;

	private TextArea statsTextArea;

	private Button startButton;
	private Button slowButton;
	private Button speedUpButton;

	private IVisualisation display;

	private int totalCustomersCount = 0;
	private Label customerCountLabel;
	private Label totalTimeLabel;

	@Override
	public void init() {
		Trace.setTraceLevel(Level.INFO);
		controller = new Controller(this);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			statsTextArea = new TextArea();
			statsTextArea.setEditable(false);
			statsTextArea.setWrapText(true);
			statsTextArea.setPrefWidth(200);
			statsTextArea.setPrefHeight(180);
			statsTextArea.setPrefRowCount(15);

			primaryStage.setOnCloseRequest(t -> {
				Platform.exit();
				System.exit(0);
			});

			primaryStage.setTitle("Pizzeria Simulator");

			startButton = new Button("Start simulation");
			startButton.setOnAction(event -> {
				totalCustomersCount = 0;
				updateStatisticsUI();
				if (statsTextArea != null) {
					statsTextArea.clear(); // Clear old report on new run
				}
				controller.startSimulation();
				startButton.setDisable(true);
			});

			slowButton = new Button("Slow down");
			slowButton.setOnAction(e -> controller.decreaseSpeed());

			speedUpButton = new Button("Speed up");
			speedUpButton.setOnAction(e -> controller.increaseSpeed());

			Label labelTimeInput = new Label("Simulation time:");
			labelTimeInput.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
			time = new TextField("1000");
			time.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
			time.setPrefWidth(120);

			Label labelDelayInput = new Label("Delay (ms):");
			labelDelayInput.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
			delay = new TextField("100");
			delay.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
			delay.setPrefWidth(120);

			Label statsHeader = new Label("--- STATISTICS ---");
			statsHeader.setFont(Font.font("Tahoma", FontWeight.BOLD, 16));

			customerCountLabel = new Label("Customers inside: 0");
			customerCountLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
			customerCountLabel.setStyle("-fx-text-fill: #0000ff;"); // Синий цвет текста

			totalTimeLabel = new Label("Total execution time: -");
			totalTimeLabel.setFont(Font.font("Tahoma", FontWeight.NORMAL, 16));
			totalTimeLabel.setStyle("-fx-text-fill: #008000;"); // Зеленый цвет текста

			GridPane grid = new GridPane();
			grid.setAlignment(Pos.TOP_LEFT);
			grid.setVgap(12);
			grid.setHgap(8);
			grid.setPadding(new Insets(10));

			grid.add(labelTimeInput, 0, 0);
			grid.add(time, 1, 0);
			grid.add(labelDelayInput, 0, 1);
			grid.add(delay, 1, 1);

			grid.add(startButton, 0, 2, 2, 1);
			startButton.setMaxWidth(Double.MAX_VALUE);
			grid.add(speedUpButton, 0, 3);
			grid.add(slowButton, 1, 3);

			grid.add(new Separator(), 0, 4, 2, 1);
			grid.add(statsHeader, 0, 5, 2, 1);
			grid.add(customerCountLabel, 0, 6, 2, 1);
			grid.add(totalTimeLabel, 0, 7, 2, 1);

			grid.add(statsTextArea, 0, 8, 2, 1); //statsTextArea

			display = new PizzaVisualisation(800, 450);

			HBox hBox = new HBox();
			hBox.setPadding(new Insets(15));
			hBox.setSpacing(20);
			hBox.getChildren().addAll(grid, (Canvas) display);

			Scene scene = new Scene(hBox);
			primaryStage.setScene(scene);
			primaryStage.setResizable(false);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private void updateStatisticsUI() {
		customerCountLabel.setText("Customers inside: " + totalCustomersCount);
	}

	@Override
	public double getTime(){
		return Double.parseDouble(time.getText());
	}

	@Override
	public long getDelay(){
		return Long.parseLong(delay.getText());
	}

	@Override
	public void setEndingTime(double time) {
		DecimalFormat formatter = new DecimalFormat("#0.00");
		Platform.runLater(() -> {
			this.totalTimeLabel.setText("Total execution time: " + formatter.format(time));
			this.startButton.setDisable(false); // Включаем кнопку обратно по окончании
		});
	}

	@Override
	public IVisualisation getVisualisation() {
		return display;
	}

	public void incrementCustomerCount() {
		totalCustomersCount++;
		updateStatisticsUI();
	}

	public void decrementCustomerCount() {
		totalCustomersCount--;
		updateStatisticsUI();
	}

	// Used for outporting customer arrives and departures
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
