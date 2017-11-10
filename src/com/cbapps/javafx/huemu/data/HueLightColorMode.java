package com.cbapps.javafx.huemu.data;

import com.google.gson.annotations.SerializedName;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author Coen Boelhouwers
 */
public enum HueLightColorMode {
	NONE,

	@SerializedName("hs")
	HSB
}
