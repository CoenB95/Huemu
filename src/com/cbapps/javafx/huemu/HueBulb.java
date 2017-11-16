package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
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

	private Circle circle;
	private Text text;

	public HueBulb(RoomPane parent) {
		circle = new Circle(40, Color.BLUE);

		text = new Text("-");
		text.setFont(Font.font("Segoe UI", 50));
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
					writer.writeObject(Json.createObjectBuilder()
							.add("on", !light.getState().isOn())
							.build());
					System.out.println("Send!");
				}
				try (InputStream input = con.getInputStream()) {
					JsonReader reader = Json.createReader(input);
					JsonArray array = reader.readArray();
					for (JsonObject o : array.getValuesAs(JsonObject.class)) {
						JsonObject ack;
						if ((ack = o.getJsonObject("success")) != null) {
							System.out.println("Success!");
							light.setState(light.getState().toggled(ack.getBoolean("/lights/" + light.getId() + "/state/on")));
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
					circle.setFill(new RadialGradient(0, 0,
							0, 0.5, 1, true, CycleMethod.NO_CYCLE,
							new Stop(0.125, Color.RED),
							new Stop(0.250, Color.ORANGE),
							new Stop(0.375, Color.YELLOW),
							new Stop(0.500, Color.GREEN),
							new Stop(0.625, Color.BLUE),
							new Stop(0.750, Color.INDIGO),
							new Stop(0.875, Color.VIOLET),
							new Stop(1.000, Color.RED)
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
