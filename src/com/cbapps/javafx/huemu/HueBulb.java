package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;
import javafx.geometry.Point2D;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import javax.json.*;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coen Boelhouwers
 */
public class HueBulb extends StackPane {

	public HueLight light;
	private TargetedAccelerator x;
	private TargetedAccelerator y;
	private double mouseX;
	private double mouseY;
	private Color newColor;

	private Circle circle;
	private Text text;

	public HueBulb(RoomPane parent) {
		circle = new Circle(40, Color.BLUE);
		circle.setStrokeWidth(8);
		circle.setStroke(Color.TRANSPARENT);

		text = new Text("-");
		text.setFont(Font.font("Segoe UI", 30));
		text.setFill(Color.WHITE);

		x = new TargetedAccelerator(0, 2000, 1000);
		y = new TargetedAccelerator(0, 2000, 1000);

		layoutXProperty().addListener((a, b, c) -> {
			//x.resetValue(getTranslateX() + b.doubleValue() - c.doubleValue());
			update(0);
		});
		layoutYProperty().addListener((a, b, c) -> {
			//y.resetValue(getTranslateY() + b.doubleValue() - c.doubleValue());
			update(0);
		});

		circle.hoverProperty().addListener((v1, v2, v3) -> {
			if (!v3)
				circle.setStroke(Color.TRANSPARENT);
		});

		setOnMousePressed(event -> {
			mouseX = event.getSceneX();
			mouseY = event.getSceneY();
		});

		setOnMouseDragged(event -> {
			parent.showGrid(true);

			double diffX = event.getSceneX() - mouseX;
			double diffY = event.getSceneY() - mouseY;
			while (diffX > RoomPane.GRID_SIZE) {
				setLayoutX(getLayoutX() + RoomPane.GRID_SIZE);
				mouseX += RoomPane.GRID_SIZE;
				diffX -= RoomPane.GRID_SIZE;
			}
			while (diffX < -RoomPane.GRID_SIZE) {
				setLayoutX(getLayoutX() - RoomPane.GRID_SIZE);
				mouseX -= RoomPane.GRID_SIZE;
				diffX += RoomPane.GRID_SIZE;
			}
			while (diffY > RoomPane.GRID_SIZE) {
				setLayoutY(getLayoutY() + RoomPane.GRID_SIZE);
				mouseY += RoomPane.GRID_SIZE;
				diffY -= RoomPane.GRID_SIZE;
			}
			while (diffY < -RoomPane.GRID_SIZE) {
				setLayoutY(getLayoutY() - RoomPane.GRID_SIZE);
				mouseY -= RoomPane.GRID_SIZE;
				diffY += RoomPane.GRID_SIZE;
			}
		});

		setOnMouseMoved(event -> {
			double x = event.getX();
			double y = event.getY();
			double angle = Math.atan2(y - getHeight() / 2, x - getWidth() / 2) / (Math.PI * 2) * 360;
			double distance = new Point2D(getWidth() / 2, getHeight() / 2).distance(x, y) / circle.getRadius();
			newColor = Color.hsb(angle, 1, distance < 1.0 ? distance : 1.0);
			if (circle.isHover() || text.isHover()) circle.setStroke(newColor);
		});

		setOnMouseReleased(event -> {
			if (!event.isStillSincePress())
				parent.showGrid(false);
		});

		setOnMouseClicked(event -> CompletableFuture.runAsync(() -> {
			if (!event.isStillSincePress())
				return;

			try {
				URL url = new URL("http://145.48.205.33/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights/" +
						light.getId() + "/state");
				HttpURLConnection con = (HttpURLConnection) url.openConnection();
				con.setRequestMethod("PUT");
				con.setDoOutput(true);
				try (OutputStream out = con.getOutputStream()) {
					JsonWriter writer = Json.createWriter(out);
					if (event.getButton() == MouseButton.SECONDARY) {
						writer.writeObject(Json.createObjectBuilder()
								.add("on", !light.getState().isOn())
								.build());
					} else if (event.getButton() == MouseButton.PRIMARY) {
						HueLightState newColorState = HueLightUtil.withColor(light.getState(), newColor);
						writer.writeObject(Json.createObjectBuilder()
								.add("hue", newColorState.getHue())
								.add("sat", newColorState.getSaturation())
								.add("bri", newColorState.getBrightness())
								.build());
					}
					System.out.println("Send!");
				}
				try (InputStream input = con.getInputStream()) {
					JsonReader reader = Json.createReader(input);
					JsonArray array = reader.readArray();
					for (JsonObject o : array.getValuesAs(JsonObject.class)) {
						JsonObject ack;
						if ((ack = o.getJsonObject("success")) != null) {
							System.out.println("Success!");
							String base = "/lights/" + light.getId() + "/state/";
							HueLightState state = light.getState();
							if (ack.containsKey(base + "on"))
								state.toggled(ack.getBoolean( base + "on"));
							else if (ack.containsKey(base + "bri"))
								state.withHue(ack.getJsonNumber(base + "bri").intValue());
							else if (ack.containsKey(base + "hue"))
								state.withHue(ack.getJsonNumber(base + "hue").intValue());
							else if (ack.containsKey(base + "sat"))
								state.withHue(ack.getJsonNumber(base + "sat").intValue());
							light.setState(state);
						}
					}
					System.out.println("response: " + array);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		getChildren().addAll(circle, text);
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
			}
			text.setText(light.getId());
		}
	}
}
