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
	private HueLightEffect effect = HueLightEffect.COLOR_LOOP;
	private double[] xy;
	@SerializedName("ct")
	private int colorTemperature;
	private HueLightAlert alert = HueLightAlert.NONE;
	@SerializedName("colormode")
	private HueLightColorMode colorMode = HueLightColorMode.NONE;

	protected HueLightState() {

	}

	private HueLightState copy() {
		HueLightState cc = new HueLightState();
		cc.on = on;
		cc.brightness = brightness;
		cc.hue = hue;
		cc.saturation = saturation;
		cc.effect = effect;
		cc.xy = xy;
		cc.colorTemperature = colorTemperature;
		cc.alert = alert;
		cc.colorMode = colorMode;
		return cc;
	}

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

	public static HueLightState off() {
		HueLightState state = new HueLightState();
		state.on = false;
		return state;
	}

	public static HueLightState on() {
		HueLightState state = new HueLightState();
		state.on = true;
		return state;
	}

	public HueLightState toggled() {
		HueLightState s = copy();
		s.on = !s.on;
		return s;
	}

	public HueLightState toggled(boolean value) {
		HueLightState s = copy();
		s.on = value;
		return s;
	}
}
