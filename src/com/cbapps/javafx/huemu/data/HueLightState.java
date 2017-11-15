package com.cbapps.javafx.huemu.data;

import com.google.gson.annotations.SerializedName;
import javafx.scene.paint.Color;

/**
 * @author CoenB95
 */

public class HueLightState {
	private boolean on;
	@SerializedName("bri")
	private int brightness;
	@SerializedName("hue")
	private int hue;
	@SerializedName("sat")
	private int saturation;

	private HueLightEffect effect = HueLightEffect.NONE;

	private double x;

	private double y;

	@SerializedName("ct")
	private int colorTemperature;

	private HueLightAlert alert;
	@SerializedName("colormode")
	private HueLightColorMode colorMode = HueLightColorMode.NONE;

	private transient double animSec;

	public Color getColor() {
		if (colorMode == null)
			return Color.BLACK;
		if (!on)
			return Color.BLACK;

		switch (colorMode) {
			case XY:
			case HSB:
				double h = effect == HueLightEffect.COLOR_LOOP ? (hue + animSec * 3435) % 65_535 : hue;
				return Color.hsb(h / 65_535 * 360, (double) saturation / 255, (double) brightness / 255);
			default:
				return Color.BLACK;
		}
	}

	public boolean isOn() {
		return on;
	}

	public void setOn(boolean on) {
		this.on = on;
	}

	public void update(double elapsedSeconds) {
		animSec += elapsedSeconds;
		if (effect == HueLightEffect.COLOR_LOOP) {
			//hue = (hue + elapsedSeconds * 3435) % 65_535;
		}
	}

	public double getHue() {
		return hue;
	}
}
