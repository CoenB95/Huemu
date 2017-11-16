package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightState;
import javafx.scene.paint.Color;

/**
 * @author Coen Boelhouwers
 */
public class HueLightUtil {

	public static Color getColor(HueLight light) {
		return getColor(light.getState());
	}

	public static Color getColor(HueLightState state) {
		if (!state.isOn())
			return Color.BLACK;
		if (state.getColorMode() == null)
			return Color.BLACK;
		switch (state.getColorMode()) {
			case XY:
			case HSB:
				return Color.hsb((double)state.getHue() / 65_535f * 360f,
						(double)state.getSaturation() / 255,
						(double)state.getBrightness() / 255);
			case NONE:
			default:
				return Color.BLACK;
		}
	}
}
