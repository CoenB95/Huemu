package com.cbapps.java.huelight;

import javax.json.bind.annotation.JsonbProperty;

public class RawHueLightState {
	@JsonbProperty("state")
	public HueLightState state;
}
