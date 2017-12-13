package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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
import jdk.incubator.http.HttpClient;
import jdk.incubator.http.HttpRequest;
import jdk.incubator.http.HttpResponse;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

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
			newPut(light.getId(), light.getState().withSaturation(v3.intValue()));
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
			HueLightState newColorState = HueLightUtil.withColor(light.getState(), newColor.get());
			if (event.getButton() == MouseButton.SECONDARY) newColorState = newColorState.toggled();
			newPut(light.getId(), newColorState);
		};
		circle.setOnMouseClicked(onCircleClick);
		text.setOnMouseClicked(onCircleClick);

		add(new StackPane(circle, text), 0, 0);
		add(saturationSlider, 1, 0);
		//getChildren().addAll(circle, text, saturationSlider);
	}

	private static void newPut(String lightId, HueLightState newState) {
		CompletableFuture.runAsync(() -> {
			try {
				System.out.println("Startup client...");
				HttpClient client = HttpClient.newHttpClient();
				System.out.println("Client = " + client);
				try {
					JsonObject object = new JsonObject();
					object.addProperty("on", newState.isOn());
					object.addProperty("hue", newState.getHue());
					object.addProperty("sat", newState.getSaturation());
					object.addProperty("bri", newState.getBrightness());
					System.out.println("Let's send:\n" + object.toString());
					HttpRequest request = HttpRequest.newBuilder(new URL(
							"http://145.48.205.33/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights/" +
									lightId + "/state").toURI())
							.PUT(HttpRequest.BodyProcessor.fromString(object.toString()))
							.build();
					CompletableFuture<HttpResponse<String>> future = client.sendAsync(request, HttpResponse.BodyHandler.asString());
					future.thenAccept(stringHttpResponse -> {
						System.out.println("response: " + stringHttpResponse.body());
						JsonArray array = new Gson().fromJson(stringHttpResponse.body(), JsonArray.class);
						for (JsonElement anArray : array) {
							JsonObject o = anArray.getAsJsonObject();
							JsonObject ack;
							if ((ack = o.getAsJsonObject("success")) != null) {
								System.out.println("Success!");
								String base = "/lights/" + lightId + "/state/";
								HueLightState respondedState = newState;
								if (ack.has(base + "on"))
									respondedState.toggled(ack.get(base + "on").getAsBoolean());
								else if (ack.has(base + "bri"))
									respondedState.withHue(ack.get(base + "bri").getAsInt());
								else if (ack.has(base + "hue"))
									respondedState.withHue(ack.get(base + "hue").getAsInt());
								else if (ack.has(base + "sat"))
									respondedState.withHue(ack.get(base + "sat").getAsInt());
								//light.setState(state);
							}
						}
					});
				} catch (URISyntaxException | MalformedURLException e) {
					e.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	private void oldPut(MouseEvent event) {
//		if (!event.isStillSincePress())
//			return;
//
//		try {
//			URL url = new URL("http://145.48.205.33/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights/" +
//					light.getId() + "/state");
//			HttpURLConnection con = (HttpURLConnection) url.openConnection();
//			con.setRequestMethod("PUT");
//			con.setDoOutput(true);
//			try (OutputStream out = con.getOutputStream()) {
//				JsonWriter writer = Json.createWriter(out);
//				if (event.getButton() == MouseButton.SECONDARY) {
//					writer.writeObject(Json.createObjectBuilder()
//							.add("on", !light.getState().isOn())
//							.build());
//				} else if (event.getButton() == MouseButton.PRIMARY) {
//					HueLightState newColorState = HueLightUtil.withColor(light.getState(), newColor.get());
//					writer.writeObject(Json.createObjectBuilder()
//							.add("hue", newColorState.getHue())
//							.add("sat", newColorState.getSaturation())
//							.add("bri", newColorState.getBrightness())
//							.build());
//				}
//				System.out.println("Send!");
//			}
//			try (InputStream input = con.getInputStream()) {
//				JsonReader reader = Json.createReader(input);
//				JsonArray array = reader.readArray();
//				for (JsonObject o : array.getValuesAs(JsonObject.class)) {
//					JsonObject ack;
//					if ((ack = o.getJsonObject("success")) != null) {
//						System.out.println("Success!");
//						String base = "/lights/" + light.getId() + "/state/";
//						HueLightState state = light.getState();
//						if (ack.containsKey(base + "on"))
//							state.toggled(ack.getBoolean(base + "on"));
//						else if (ack.containsKey(base + "bri"))
//							state.withHue(ack.getJsonNumber(base + "bri").intValue());
//						else if (ack.containsKey(base + "hue"))
//							state.withHue(ack.getJsonNumber(base + "hue").intValue());
//						else if (ack.containsKey(base + "sat"))
//							state.withHue(ack.getJsonNumber(base + "sat").intValue());
//						light.setState(state);
//					}
//				}
//				System.out.println("response: " + array);
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
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
