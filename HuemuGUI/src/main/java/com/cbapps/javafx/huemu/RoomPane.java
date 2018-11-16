package com.cbapps.javafx.huemu;

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Slider;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Coen Boelhouwers
 */
public class RoomPane extends Pane {

	public static final int GRID_SIZE = 25;

	private List<HueBulb> bulbs;
	private boolean gridVisible;
	private List<Line> lines;
	private Slider slider;
	private DoubleProperty scaleProperty = new SimpleDoubleProperty(1);

	public RoomPane(List<LightStorage.HueBulbInfo> infos) {
		bulbs = new ArrayList<>();
		lines = new ArrayList<>();

		for (int i = 0; i < infos.size(); i++) {
			HueBulb bulb = new HueBulb(this);
			bulb.setLayoutX(infos.get(i).getX());
			bulb.setLayoutY(infos.get(i).getY());
			bulb.light = infos.get(i).getLight();
			addBulb(bulb);
		}

		drawLines();
		widthProperty().addListener((v1, v2, v3) -> drawLines());
		heightProperty().addListener((v1, v2, v3) -> drawLines());
	}

	private void addBulb(HueBulb bulb) {
		Platform.runLater(() -> {
			getChildren().add(bulb);
			bulbs.add(bulb);
		});
	}

	private void drawLines() {
		Platform.runLater(() -> {
			getChildren().removeAll(lines);
			lines.clear();

			if (!gridVisible)
				return;

			int x = 0;
			int y = 0;
			while (x < getWidth()) {
				Line line = new Line(x, 0, x, getHeight());
				line.setStroke(Color.WHITE);
				lines.add(line);
				x += GRID_SIZE;
			}
			while (y < getHeight()) {
				Line line = new Line(0, y, getWidth(), y);
				line.setStroke(Color.WHITE);
				lines.add(line);
				y += GRID_SIZE;
			}
			getChildren().addAll(0, lines);
		});
	}

	public List<HueBulb> getBulbs() {
		return bulbs;
	}

	public DoubleProperty scaleProperty() {
		return scaleProperty;
	}

	public boolean isGridVisible() {
		return gridVisible;
	}

	public void showGrid(boolean value) {
		if (gridVisible == value)
			return;
		gridVisible = value;
		drawLines();
	}

	public void toggleGrid() {
		showGrid(!gridVisible);
	}

	public void update(double elapsedSeconds) {
		for (HueBulb bulb : bulbs)
			bulb.update(elapsedSeconds);
	}
}
