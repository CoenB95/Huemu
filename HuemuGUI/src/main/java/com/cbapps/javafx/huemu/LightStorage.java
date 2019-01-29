package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import javax.json.bind.config.PropertyVisibilityStrategy;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Coen Boelhouwers
 */
public class LightStorage {
	private static final long VERSION = 1;
	@JsonbProperty
	private List<HueBulbInfo> lights;
	private long version = VERSION;

	public List<HueBulbInfo> getLights() {
		return lights;
	}

	public void restoreLights() {
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
		Jsonb jsonb = JsonbBuilder.newBuilder().withConfig(config).build();
		String json = Preferences.userRoot().get("storedLights", "");
		LightStorage storage = jsonb.fromJson(json, LightStorage.class);
		if (storage == null || storage.version != VERSION) {
			System.out.println("Old storage version, can't restore bulb positions.");
			lights = new ArrayList<>();
			for (int i = 0; i < 9; i++) {
				HueLight hl = new HueLight(HueLightState.off());
				lights.add(new HueBulbInfo(hl,i * RoomPane.GRID_SIZE, RoomPane.GRID_SIZE));
			}
			return;
		}
		lights = storage.lights;
	}

	public void storeLights() {
		Jsonb jsonb = JsonbBuilder.create();
		String json = jsonb.toJson(this);
		Preferences.userRoot().put("storedLights", json);
	}

	public static class HueBulbInfo {
		@JsonbProperty
		private HueLight light;
		@JsonbProperty
		private double x;
		@JsonbProperty
		private double y;

		@JsonbCreator
		public HueBulbInfo(@JsonbProperty("light") HueLight light,
						   @JsonbProperty("x") double x,
						   @JsonbProperty("y") double y) {
			this.light = light;
			this.x = x;
			this.y = y;
		}

		public HueLight getLight() {
			return light;
		}

		public double getX() {
			return x;
		}

		public double getY() {
			return y;
		}
	}
}
