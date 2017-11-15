package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLightState;
import javafx.scene.paint.Color;

/**
 * @author Coen Boelhouwers
 */
public class JavaFXHueLightState extends HueLightState {

	public Color getColor() {
		if (!isOn())
			return Color.BLACK;
		if (getColorMode() == null)
			return Color.BLACK;
		switch (getColorMode()) {
			case XY:
			case HSB:
				return Color.hsb((double)getHue() / 65_535f * 360f, (double)getSaturation() / 255, (double)getBrightness() / 255);
			case NONE:
			default:
				return Color.BLACK;
		}
	}
}
