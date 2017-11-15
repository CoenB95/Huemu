package com.cbapps.javafx.huemu;

import com.cbapps.java.huelight.HueLight;
import com.cbapps.java.huelight.HueLightFactory;

/**
 * @author Coen Boelhouwers
 */
public class JavaFXHueLightFactory extends HueLightFactory {

	static {
		HueLightFactory.registerFactory(new JavaFXHueLightFactory());
	}

	@Override
	protected HueLight createLocalHueLight() {
		HueLight hl = new HueLight();
		hl.setState(new JavaFXHueLightState());
		return hl;
	}

}
