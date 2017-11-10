package com.cbapps.javafx.huemu;

import com.cbapps.javafx.huemu.data.HueLight;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import javax.json.Json;
import javax.json.JsonReader;
import javax.json.bind.JsonbConfig;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coen Boelhouwers
 */
public class Main extends Application {

	private long lastNanos;
	private boolean stopped;
	private double lastHue;
	private double lastSec;
	private double longtermSec;

	private Pane bulbGrid;

	public static void main(String[] args) {
		Application.launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		bulbGrid = new RoomPane();//new GridPane();
		BorderPane pane = new BorderPane(bulbGrid);

		List<HueLightPane> bulbs = new ArrayList<>();

		CompletableFuture.runAsync(() -> {
			for (int i = 0; i < 10; i++) {
			//while (!stopped) {
				HueLightPane bulb = new HueLightPane();
				bulb.setLayoutX(i * 50);
				Platform.runLater(() -> {
					bulbGrid.getChildren().add(bulb);//.add(bulb, bulbs.size(), 0);
					bulbs.add(bulb);
				});

//				try {
//					Thread.sleep(2000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
			}
		});
		pane.setTop(new Label("Hallo wereld!"));
		pane.setBackground(new Background(new BackgroundFill(Color.PINK, null, null)));

		Scene scene = new Scene(pane, 300, 300);
		primaryStage.setScene(scene);
		primaryStage.show();

		drawLines();

		new AnimationTimer() {
			@Override
			public void handle(long now) {
				double elapsedSeconds = 0.013;//(System.nanoTime() - lastNanos) / 1_000_000.0 / 1_000.0;
				lastNanos = now;

				for (HueLightPane bulb : bulbs) {
					//bulb.getTargetX().setTarget(pane.getWidth() / 2);
					//bulb.getTargetY().setTarget(pane.getHeight() / 2);
					bulb.update(elapsedSeconds);
				}
			}
		}.start();

		CompletableFuture.runAsync(() -> {
			try {
				URL url = new URL("http://145.48.205.33/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights");
				while (!stopped) {
					try (JsonReader reader = Json.createReader(url.openStream())) {
						List<HueLight> lights = new ArrayList<>();
						JsonbConfig config = new JsonbConfig().withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
							@Override
							public boolean isVisible(Field field) {
								return true;
							}

							@Override
							public boolean isVisible(Method method) {
								return true;
							}
						});
						Gson gson = new Gson();
						reader.readObject().forEach((k, v) -> {
							HueLight hueLight = gson.fromJson(v.toString(), HueLight.class);
							hueLight.setId(k);
							lights.add(hueLight);
						});
						Platform.runLater(() -> {
							double newHue = lights.get(0).getState().getHue();
							double newSec = (double) System.currentTimeMillis() / 1000;
							//System.out.printf("Hue diff: %.1f over %.1f seconds (%.1f seconds)%n", newHue-lastHue, newSec-lastSec, newSec - longtermSec);
							if (newHue != lastHue)
								longtermSec = newSec;
							lastHue = newHue;
							lastSec = newSec;
							for (int i = 0; i < lights.size() && i < bulbs.size(); i++) {
								//com.cbapps.javafx.huemu.data.HueLightState state = lights.get(i).getState();
								//bulbs.get(i).setFill(state.getColor());
								if (bulbs.get(i).light == null || bulbs.get(i).light.getState().getHue() !=
										lights.get(i).getState().getHue()) {
									System.out.println("Bulb " + i + " needs update.");
									bulbs.get(i).light = lights.get(i);
								}
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
					Thread.sleep(2000);
				}
			} catch (MalformedURLException | InterruptedException e) {
				e.printStackTrace();
			}
		});

		try {
			HttpServer server = HttpServer.create(new InetSocketAddress("localhost",7300), 0);
			server.createContext("/", exchange -> {
				System.out.println("Http request: " + exchange.getRequestMethod());
			});
			server.start();
			System.out.println("Server at " + server.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}

		pane.widthProperty().addListener((v1, v2, v3) -> drawLines());
		pane.heightProperty().addListener((v1, v2, v3) -> drawLines());
	}

	private List<Line> lines = new ArrayList<>();
	private void drawLines() {
		Platform.runLater(() -> {
			bulbGrid.getChildren().removeAll(lines);
			lines.clear();

			int x = 0;
			int y = 0;
			while (x < bulbGrid.getWidth()) {
				Line line = new Line(x, 0, x, bulbGrid.getHeight());
				lines.add(line);
				x += 50;
			}
			while (y < bulbGrid.getHeight()) {
				Line line = new Line(0, y, bulbGrid.getWidth(), y);
				lines.add(line);
				y += 50;
			}
			bulbGrid.getChildren().addAll(0, lines);
		});
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		stopped = true;
	}
}
