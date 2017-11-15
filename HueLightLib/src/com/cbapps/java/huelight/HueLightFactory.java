package com.cbapps.java.huelight;

/**
 * @author Coen Boelhouwers
 */
public abstract class HueLightFactory {

	private static HueLightFactory localFactory;

	protected abstract HueLight createLocalHueLight();

	public static HueLight newHueLightInstance() {
		return localFactory.createLocalHueLight();
	}

	public static void registerFactory(HueLightFactory factory) {
		localFactory = factory;
	}
}
