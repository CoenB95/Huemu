package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;
import com.cbapps.javafx.huemu.connection.HueConnection;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * @author Coen Boelhouwers
 */
public class HueBulb extends GridPane {

	public static final double DEFAULT_RADIUS = 40;

	public HueLight light;
	private TargetedAccelerator x;
	private TargetedAccelerator y;
	private double mouseX;
	private double mouseY;
	private ObjectProperty<Color> newColor;

	private Circle circle;
	private Text text;
	private Slider saturationSlider;

	public HueBulb(RoomPane parent) {
		circle = new Circle(DEFAULT_RADIUS, Color.BLUE);
		circle.setStrokeWidth(8);
		circle.setStroke(Color.TRANSPARENT);

		saturationSlider = new Slider(0, 255, 255);
		saturationSlider.setPrefHeight(30);
		saturationSlider.setOrientation(Orientation.VERTICAL);
		saturationSlider.valueProperty().addListener((v1, v2, v3) -> {
			new HueConnection().pushState(light.getId(), light.getState().withSaturation(v3.intValue()))
					.thenAccept(state -> {
				light.setState(state);
			});
		});

		newColor = new SimpleObjectProperty<>(Color.BLACK);
		text = new Text("-");
		text.setFont(Font.font("Segoe UI", 30));
		text.setFill(Color.WHITE);

		x = new TargetedAccelerator(0, 2000, 1000);
		y = new TargetedAccelerator(0, 2000, 1000);

		layoutXProperty().addListener((a, b, c) -> {
			x.resetValue(getTranslateX() + b.doubleValue() - c.doubleValue());
			update(0);
		});
		layoutYProperty().addListener((a, b, c) -> {
			y.resetValue(getTranslateY() + b.doubleValue() - c.doubleValue());
			update(0);
		});

		circle.strokeProperty().bind(Bindings.when(circle.hoverProperty().or(text.hoverProperty()))
				.then(newColor).otherwise(new Color(1, 1, 1, 0.2)));

		setOnMousePressed(event -> {
			mouseX = event.getSceneX();
			mouseY = event.getSceneY();
		});

		EventHandler<MouseEvent> onCircleDrag = event -> {
			parent.showGrid(true);

			double scale = parent.scaleProperty().get();
			double diffX = event.getSceneX() - mouseX;
			double diffY = event.getSceneY() - mouseY;

			while (diffX > RoomPane.GRID_SIZE * scale && (getLayoutX() + getWidth()) * scale + RoomPane.GRID_SIZE < parent.getWidth()) {
				setLayoutX(getLayoutX() + RoomPane.GRID_SIZE);
				mouseX += RoomPane.GRID_SIZE * scale;
				diffX -= RoomPane.GRID_SIZE * scale;
			}
			while (diffX < -RoomPane.GRID_SIZE * scale && getLayoutX() * scale - RoomPane.GRID_SIZE > 0) {
				setLayoutX(getLayoutX() - RoomPane.GRID_SIZE);
				mouseX -= RoomPane.GRID_SIZE * scale;
				diffX += RoomPane.GRID_SIZE * scale;
			}
			while (diffY > RoomPane.GRID_SIZE * scale && (getLayoutY() + getHeight()) * scale + RoomPane.GRID_SIZE < parent.getHeight()) {
				setLayoutY(getLayoutY() + RoomPane.GRID_SIZE);
				mouseY += RoomPane.GRID_SIZE * scale;
				diffY -= RoomPane.GRID_SIZE * scale;
			}
			while (diffY < -RoomPane.GRID_SIZE * scale && getLayoutY() * scale - RoomPane.GRID_SIZE > 0) {
				setLayoutY(getLayoutY() - RoomPane.GRID_SIZE);
				mouseY -= RoomPane.GRID_SIZE * scale;
				diffY += RoomPane.GRID_SIZE * scale;
			}
		};
		circle.setOnMouseDragged(onCircleDrag);
		text.setOnMouseDragged(onCircleDrag);

		setOnMouseMoved(event -> {
			double x = event.getX();
			double y = event.getY();
			double angle = Math.atan2(y - circle.getRadius(), x - circle.getRadius()) / (Math.PI * 2) * 360;
			double distance = new Point2D(circle.getRadius(), circle.getRadius()).distance(x, y) / circle.getRadius();
			newColor.set(Color.hsb(angle, 1, distance < 1.0 ? distance : 1.0));
		});

		setOnMouseReleased(event -> {
			parent.showGrid(false);
		});

		EventHandler<MouseEvent> onCircleClick = event -> {
			if (!event.isStillSincePress())
				return;

			HueLightState parsedColor = HueLightUtil.withColor(light.getState(), newColor.get());
			HueLightState newColorState = light.getState().withHue(parsedColor.getHue()).withBrightness(parsedColor.getBrightness());
			if (event.getButton() == MouseButton.SECONDARY) newColorState = newColorState.toggled();
			new HueConnection().pushState(light.getId(), newColorState).thenAccept(state -> {
				light.setState(state);
			});
		};
		circle.setOnMouseClicked(onCircleClick);
		text.setOnMouseClicked(onCircleClick);

		add(new StackPane(circle, text), 0, 0);
		add(saturationSlider, 1, 0);
	}



	public double getTargetX() {
		return x.getTarget();
	}

	public double getTargetY() {
		return y.getTarget();
	}

	void update(double elapsedSeconds) {
		x.update(elapsedSeconds);
		y.update(elapsedSeconds);
		setTranslateX(x.getValue());
		setTranslateY(y.getValue());
		if (light != null) {
			switch (light.getState().getEffect()) {
				case COLOR_LOOP:
					circle.setFill(new LinearGradient(0.2, 0.8,
							0.8, 0.2, true, CycleMethod.NO_CYCLE,
							new Stop(0, Color.web("#f8bd55")),
							new Stop(0.14, Color.web("#c0fe56")),
							new Stop(0.28, Color.web("#5dfbc1")),
							new Stop(0.43, Color.web("#64c2f8")),
							new Stop(0.57, Color.web("#be4af7")),
							new Stop(0.71, Color.web("#ed5fc2")),
							new Stop(0.85, Color.web("#ef504c")),
							new Stop(1, Color.web("#f2660f"))
					));
					break;
				case NONE:
				default:
					circle.setFill(HueLightUtil.getColor(light));
					break;
			}
			text.setText(light.getId());
		}
	}
}
