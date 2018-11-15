package com.cbapps.java.huelight;

import com.google.gson.annotations.SerializedName;

/**
 * @author Coen Boelhouwers
 */
public enum HueLightColorMode {
	NONE,

	@SerializedName("hs")
	HSB,

	@SerializedName("xy")
	XY
}
