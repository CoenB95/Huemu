package com.cbapps.java.huelight;

import com.google.gson.annotations.SerializedName;

/**
 * @author CoenB95
 */

public class HueLightState {
	private boolean on;
	@SerializedName("bri")
	private int brightness;
	private int hue;
	@SerializedName("sat")
	private int saturation;
	private HueLightEffect effect;
	private double[] xy;
	@SerializedName("ct")
	private int colorTemperature;
	private HueLightAlert alert;
	@SerializedName("colormode")
	private HueLightColorMode colorMode = HueLightColorMode.NONE;

	public HueLightEffect getEffect() {
		return effect;
	}

	public int getBrightness() {
		return brightness;
	}

	public int getHue() {
		return hue;
	}

	public int getSaturation() {
		return saturation;
	}

	public double[] getXy() {
		return xy;
	}

	public int getColorTemperature() {
		return colorTemperature;
	}

	public HueLightAlert getAlert() {
		return alert;
	}

	public HueLightColorMode getColorMode() {
		return colorMode;
	}

	public boolean isOn() {
		return on;
	}
}
