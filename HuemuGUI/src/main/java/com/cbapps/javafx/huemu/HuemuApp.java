package com.cbapps.javafx.huemu;

import com.cbapps.javafx.huemu.connection.HueConnection;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.transform.Scale;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Coen Boelhouwers
 */
public class HuemuApp extends Application {

	private ScheduledExecutorService executorService;
	private ScheduledFuture scheduledUpdates;

	private RoomPane bulbGrid;
	private LightStorage storage;
	private int updateFrequencyMillis = 2000;

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.getIcons().add(new Image("/Huemu logo.png"));
		primaryStage.setTitle("Huemu");

		executorService = Executors.newSingleThreadScheduledExecutor();

		storage = new LightStorage();
		storage.restoreLights();

		bulbGrid = new RoomPane(storage.getLights());
		BorderPane pane = new BorderPane(bulbGrid);

		Slider slider = new Slider(50, 400, 100);
		Scale scale = new Scale(1, 1, 0, 0);
		bulbGrid.scaleProperty().bind(slider.valueProperty().divide(100));
		scale.xProperty().bind(slider.valueProperty().divide(100));
		scale.yProperty().bind(slider.valueProperty().divide(100));
		bulbGrid.getTransforms().add(scale);

		bulbGrid.setOnZoom(event -> {
			slider.setValue(bulbGrid.scaleProperty().get() * event.getZoomFactor() * 100);
		});

		TextField updateFrequencyField = new TextField(String.valueOf(updateFrequencyMillis));
		updateFrequencyField.setPromptText("Update freq (ms)");
		updateFrequencyField.setOnAction(event -> {
			int value = 2000;
			try {
				if (!updateFrequencyField.getText().isEmpty())
					value = Integer.parseInt(updateFrequencyField.getText());
			} catch (NumberFormatException e) {
				System.err.println("Not a valid value.");
			}
			updateFrequencyMillis = value;
			scheduleUpdates();
		});

		VBox settingsBox = new VBox(
				new Label("Scale"),
				slider,
				new Label("Update frequency"),
				updateFrequencyField);
		settingsBox.setBackground(new Background(new BackgroundFill(Color.WHITE, new CornerRadii(5), null)));
		pane.setRight(settingsBox);

		pane.setBackground(new Background(new BackgroundFill(Color.DARKGRAY, null, null)));

		Scene scene = new Scene(pane, 400, 400);
		primaryStage.setScene(scene);
		primaryStage.show();

		new AnimationTimer() {
			@Override
			public void handle(long now) {
				double elapsedSeconds = 0.013;
				//(System.nanoTime() - lastNanos) / 1_000_000.0 / 1_000.0;
				//lastNanos = now;
				bulbGrid.update(elapsedSeconds);
			}
		}.start();

		scheduleUpdates();
	}

	private void scheduleUpdates() {
		if (scheduledUpdates != null)
			scheduledUpdates.cancel(false);

		HueConnection connection = new HueConnection();
		scheduledUpdates = executorService.scheduleAtFixedRate(() -> {
			bulbGrid.getBulbs().forEach(bulb -> {
				connection.fetchState(bulb.light).thenAccept(state -> bulb.light.setState(state));
			});
		}, 0, updateFrequencyMillis, TimeUnit.MILLISECONDS);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		storage.getLights().clear();
		bulbGrid.getBulbs().forEach(b -> storage.getLights().add(
				new LightStorage.HueBulbInfo(b.light, b.getLayoutX(), b.getLayoutY())
		));
		storage.storeLights();

		if (scheduledUpdates != null)
			scheduledUpdates.cancel(true);
	}
}
