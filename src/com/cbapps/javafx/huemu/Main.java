package com.cbapps.javafx.huemu;

import com.cbapps.javafx.huemu.data.HueLight;
import com.cbapps.javafx.huemu.network.HueHttpHandler;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.IOException;
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

	private boolean stopped;
	private RoomPane bulbGrid;
	private HttpServer server;
	private LightStorage storage;

	public static void main(String[] args) {
		Application.launch();
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		storage = new LightStorage();
		storage.restoreLights();
		bulbGrid = new RoomPane(storage.getLights());
		BorderPane pane = new BorderPane(bulbGrid);

		pane.setTop(new Label("Hallo wereld!"));
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

		CompletableFuture.runAsync(() -> {
			try {
				URL url = new URL("http://145.48.205.33/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights");
				//while (!stopped) {
					try (JsonReader reader = Json.createReader(url.openStream())) {
						List<HueLight> lights = new ArrayList<>();
						Gson gson = new Gson();
						reader.readObject().forEach((k, v) -> {
							HueLight hueLight = gson.fromJson(v.toString(), HueLight.class);
							hueLight.setId(k);
							lights.add(hueLight);
						});

						if (server == null)
							startServer(lights);

						Platform.runLater(() -> {
							for (int i = 0; i < lights.size() && i < bulbGrid.getBulbs().size(); i++) {
								HueLight bulbLight = bulbGrid.getBulbs().get(i).light;
								if (bulbLight == null || bulbLight.getState().getHue() !=
										lights.get(i).getState().getHue()) {
									System.out.println("Bulb " + i + " needs update.");
									bulbGrid.getBulbs().get(i).light = lights.get(i);
								}
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
					Thread.sleep(2000);
				//}
			} catch (MalformedURLException | InterruptedException e) {
				e.printStackTrace();
			}
		});
	}

	private void startServer(List<HueLight> lights) {
		try {
			server = HttpServer.create(new InetSocketAddress("localhost",7300), 0);
			HueHttpHandler handler = new HueHttpHandler();
			for (HueLight light : lights) {
				server.createContext("/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights",
						exchange -> handler.handleLights(lights, exchange));
				String address = "/lights/" + light.getId() + "/state";
				server.createContext("/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9" + address,
						exchange -> handler.handleState(address, light, exchange));
			}
			server.start();
			System.out.println("Server at " + server.getAddress());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		stopped = true;
		storage.getLights().clear();
		bulbGrid.getBulbs().forEach(b -> storage.getLights().add(
				new LightStorage.HueBulbInfo(b.light, b.getLayoutX(), b.getLayoutY())
		));
		storage.storeLights();
		if (server != null)
			server.stop(0);
	}
}
