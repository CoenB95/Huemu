import javax.json.bind.annotation.JsonbProperty;

/**
 * @author CoenB95
 */

public class HueLight {
	private String id;
	private HueLightState state;

	public String getId() {
		return id;
	}

	public HueLightState getState() {
		return state;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void update(double elapsedSeconds) {
		state.update(elapsedSeconds);
	}
}
