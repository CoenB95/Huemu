package com.cbapps.java.huelight;

import java.util.Collection;
import java.util.HashMap;
import java.util.stream.Collectors;

public class RawHueLightCollection extends HashMap<String, RawHueLightState> {

	public Collection<HueLight> toCollection() {
		return entrySet()
				.stream()
				.map(entry -> new HueLight(entry.getKey(), entry.getValue().state))
				.collect(Collectors.toList());
	}

}
