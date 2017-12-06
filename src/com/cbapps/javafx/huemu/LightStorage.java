package com.cbapps.javafx.huemu;
import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * @author Coen Boelhouwers
 */
public class LightStorage {
	private static final long VERSION = 1;
	private List<HueBulbInfo> lights;
	private long version = VERSION;

	public List<HueBulbInfo> getLights() {
		return lights;
	}

	public void restoreLights() {
		Gson gson = new Gson();
		String json = Preferences.userRoot().get("storedLights", "");
		LightStorage storage = gson.fromJson(json, LightStorage.class);
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
		Gson gson = new Gson();
		String json = gson.toJson(this);
		Preferences.userRoot().put("storedLights", json);
	}

	public static class HueBulbInfo {
		private HueLight light;
		private double x;
		private double y;

		public HueBulbInfo(HueLight light, double x, double y) {
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
