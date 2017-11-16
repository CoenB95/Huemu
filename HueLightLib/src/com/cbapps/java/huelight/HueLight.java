package com.cbapps.java.huelight;

/**
 * @author CoenB95
 */

public class HueLight {
	private String id;
	private HueLightState state;

	protected HueLight() {

	}

	public HueLight(HueLightState state) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public HueLightState getState() {
		return state;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setState(HueLightState newState) {
		state = newState;
	}
}
