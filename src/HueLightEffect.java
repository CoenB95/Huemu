import com.google.gson.annotations.SerializedName;

import javax.json.bind.annotation.JsonbProperty;

/**
 * @author CoenB95
 */

public enum HueLightEffect {
	@SerializedName("none")
	NONE,

	@SerializedName("colorloop")
	COLOR_LOOP
}
