package com.cbapps.javafx.huemu.data;

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
