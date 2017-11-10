package com.cbapps.javafx.huemu.network;

import com.cbapps.javafx.huemu.data.HueLight;
import com.cbapps.javafx.huemu.data.HueLightState;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.sun.net.httpserver.HttpExchange;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Coen Boelhouwers
 */
public class HueHttpHandler {

	private ResponseBehavior responser = new HueResponse();

	public void handleLights(Collection<HueLight> lights, HttpExchange exchange) {
		responser.setExchange(exchange);
		if (!exchange.getRequestMethod().equals("GET")) {
			responser.wrongMethod("/lights",
					exchange.getRequestMethod(), "GET");
			return;
		}
		responser.sendLights(lights);
	}

	public void handleState(String address, HueLight light, HttpExchange exchange) {
		responser.setExchange(exchange);
		if (!exchange.getRequestMethod().equals("PUT")) {
			responser.wrongMethod(address + light.getId() + "/state",
								exchange.getRequestMethod(), "PUT");
			return;
		}
		Gson gson = new Gson();
		try {
			HueLightState state = gson.fromJson(new InputStreamReader(exchange.getRequestBody()), HueLightState.class);
			HueLightState oldState = light.getState();
			light.setState(state);
			responser.success(light.getId(), oldState, state);
		} catch (JsonIOException e) {
			e.printStackTrace();
		}
//		for (JsonObject o : array.getValuesAs(JsonObject.class)) {
//			JsonObject ack;
//			if ((ack = o.getJsonObject("success")) != null) {
//				System.out.println("Success!");
//				light.getState().setOn(ack.getBoolean("/lights/" + light.getId() + "/state/on"));
//			}
//		}
	}
}
