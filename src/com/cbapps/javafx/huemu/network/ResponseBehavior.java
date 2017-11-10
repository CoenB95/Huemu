package com.cbapps.javafx.huemu.network;

import com.cbapps.javafx.huemu.data.HueLight;
import com.cbapps.javafx.huemu.data.HueLightState;
import com.sun.net.httpserver.HttpExchange;

import javax.json.Json;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Collection;

/**
 * @author Coen Boelhouwers
 */
public abstract class ResponseBehavior {
	private HttpExchange exchange;

	protected void send(String json) {
		System.out.println("Sending response:\n" + json + '\n');
		try {
			exchange.sendResponseHeaders(200, 0);
			try (OutputStreamWriter writer = new OutputStreamWriter(exchange.getResponseBody())) {
				writer.write(json);
				writer.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected void send(JsonValue json) {
		System.out.println("Sending response:\n" + json.toString() + '\n');
		try {
			exchange.sendResponseHeaders(200, 0);
			OutputStream out = exchange.getResponseBody();
			Json.createWriter(out).write(json);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setExchange(HttpExchange exchange) {
		this.exchange = exchange;
	}

	public abstract void sendLights(Collection<HueLight> lights);
	public abstract void success(String hueLightId, HueLightState oldState, HueLightState newState);
	public abstract void wrongMethod(String address, String gotMethod, String expectedMethod);
}
