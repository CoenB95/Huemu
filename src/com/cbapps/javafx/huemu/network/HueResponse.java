package com.cbapps.javafx.huemu.network;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;
import com.google.gson.Gson;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Coen Boelhouwers
 */
public class HueResponse extends ResponseBehavior {

	@Override
	public void sendLights(Collection<HueLight> lights) {
		Gson gson = new Gson();
		Map<String, HueLight> mapping = new HashMap<>();
		lights.forEach(l -> mapping.put(l.getId(), l));
		send(gson.toJsonTree(mapping));
	}

	@Override
	public void success(String hueLightId, HueLightState oldState, HueLightState newState) {
		JsonObjectBuilder builder = Json.createObjectBuilder();
		if (oldState.isOn() != newState.isOn())
			builder.add("/lights/" + hueLightId + "/state/on", newState.isOn());
		send(Json.createObjectBuilder().add("success", builder).build());
	}

	@Override
	public void wrongMethod(String address, String gotMethod, String expectedMethod) {
		JsonObjectBuilder builder = Json.createObjectBuilder()
				.add("type", 3)
				.add("address", address)
				.add("description", "resource, " + address + ", not available");
		send(Json.createArrayBuilder()
				.add(Json.createObjectBuilder()
						.add("error", builder)).build());
	}
}
