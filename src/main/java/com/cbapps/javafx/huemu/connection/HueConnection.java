package com.cbapps.javafx.huemu.connection;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightEffect;
import com.cbapps.java.huelight.HueLightState;
import com.google.gson.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class HueConnection {
	private String address = "145.48.205.33";
	private String user = "ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9";

	public HueConnection() {

	}

	public CompletionStage<List<HueLight>> fetchLights() {
		HttpClient client = HttpClient.newHttpClient();

		try {
			HttpRequest request = HttpRequest.newBuilder(
					new URI(getBaseUrl() + "/lights"))
					.GET()
					.build();

			return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApplyAsync(stringHttpResponse -> {
						//System.out.println("response: " + stringHttpResponse.body());
						List<HueLight> lights = new ArrayList<>();
						Gson gson = new Gson();
						JsonObject object = new JsonParser().parse(stringHttpResponse.body()).getAsJsonObject();
						object.entrySet().forEach(entry -> {
							HueLight hueLight = gson.fromJson(entry.getValue().toString(), HueLight.class);
							hueLight.setId(entry.getKey());
							lights.add(hueLight);
						});
						return lights;
					});
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return CompletableFuture.failedFuture(e);
		}
	}

	public CompletionStage<HueLightState> fetchState(HueLight light) {
		return fetchState(light.getId());
	}

	public CompletionStage<HueLightState> fetchState(String lightId) {
		HttpClient client = HttpClient.newHttpClient();

		try {
			HttpRequest request = HttpRequest.newBuilder(
					new URI(getBaseUrl() + "/lights/" + lightId))
					.GET()
					.build();

			return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApplyAsync(stringHttpResponse -> {
						//System.out.println("response: " + stringHttpResponse.body());
						Gson gson = new Gson();
						JsonObject wrappingObject = gson.fromJson(stringHttpResponse.body(), JsonObject.class);
						HueLightState s = gson.fromJson(wrappingObject.get("state"), HueLightState.class);
						return s;
					});
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return CompletableFuture.failedFuture(e);
		}
	}

	private String getBaseUrl() {
		return "http://" + address + "/api/" + user;
	}

	public CompletionStage<HueLightState> pushState(HueLight light) {
		return pushState(light.getId(), light.getState());
	}

	public CompletionStage<HueLightState> pushState(HueLight light, HueLightState state) {
		return pushState(light.getId(), state);
	}

	public CompletionStage<HueLightState> pushState(String lightId, HueLightState newState) {
		HttpClient client = HttpClient.newHttpClient();

		JsonObject object = new JsonObject();
		object.addProperty("on", newState.isOn());
		object.addProperty("hue", newState.getHue());
		object.addProperty("sat", newState.getSaturation());
		object.addProperty("bri", newState.getBrightness());
		object.addProperty("effect", "none");
		System.out.println("Let's send:\n" + object.toString());

		try {
			HttpRequest request = HttpRequest.newBuilder(
					new URI(getBaseUrl() + "/lights/" + lightId + "/state"))
					.PUT(HttpRequest.BodyPublishers.ofString(object.toString()))
					.build();

			return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
					.thenApplyAsync(stringHttpResponse -> {
						System.out.println("response: " + stringHttpResponse.body());
						JsonArray array = new Gson().fromJson(stringHttpResponse.body(), JsonArray.class);
						HueLightState respondedState = newState.withEffect(HueLightEffect.NONE);
						for (JsonElement anArray : array) {
							JsonObject o = anArray.getAsJsonObject();
							JsonObject ack;
							if ((ack = o.getAsJsonObject("success")) != null) {
								String base = "/lights/" + lightId + "/state/";
								Map.Entry<String, JsonElement> entry = ack.entrySet().stream().findFirst().orElse(null);
								if (entry == null || !entry.getKey().contains(base)) {
									throw new IllegalStateException("Unexpected key");
								}

								switch (entry.getKey().replace(base, "")) {
									case "on":
										respondedState = respondedState.toggled(entry.getValue().getAsBoolean());
										break;
									case "bri":
										respondedState = respondedState.withBrightness(entry.getValue().getAsInt());
										break;
									case "hue":
										respondedState = respondedState.withHue(entry.getValue().getAsInt());
										break;
									case "sat":
										respondedState = respondedState.withSaturation(entry.getValue().getAsInt());
										break;
								}
							}
						}
						return respondedState;
					});
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return CompletableFuture.failedFuture(e);
		}
	}
}
