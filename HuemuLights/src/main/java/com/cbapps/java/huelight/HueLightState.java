package com.cbapps.java.huelight;

import javax.json.bind.Jsonb;
import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;
import java.io.Serializable;

/**
 * @author CoenB95
 */

public class HueLightState implements Serializable {
	@JsonbProperty
	private boolean on;
	@JsonbProperty("bri")
	private int brightness;
	@JsonbProperty
	private int hue;
	@JsonbProperty("sat")
	private int saturation;
	@JsonbProperty("effect")
	private HueLightEffect effect = HueLightEffect.colorloop;
	@JsonbProperty
	private double[] xy;
	@JsonbProperty("ct")
	private int colorTemperature;
	@JsonbProperty
	private HueLightAlert alert = HueLightAlert.none;
	@JsonbProperty("colormode")
	private HueLightColorMode colorMode = HueLightColorMode.none;

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

	public HueLightState withBrightness(int value) {
		HueLightState s = copy();
		s.brightness = value;
		s.colorMode = HueLightColorMode.hs;
		return s;
	}

	public HueLightState withEffect(HueLightEffect value) {
		HueLightState s = copy();
		s.effect = value;
		return s;
	}

	public HueLightState withHue(int value) {
		HueLightState s = copy();
		s.hue = value;
		s.colorMode = HueLightColorMode.hs;
		return s;
	}

	public HueLightState withSaturation(int value) {
		HueLightState s = copy();
		s.saturation = value;
		s.colorMode = HueLightColorMode.hs;
		return s;
	}
}
