import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import javax.json.*;
import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * @author Coen Boelhouwers
 */
public class HueLightPane extends Circle {

	public HueLight light;
	private TargetedAccelerator x;
	private TargetedAccelerator y;
	private double centerX;
	private double centerY;
	private double mouseX;
	private double mouseY;

	public HueLightPane() {
		super(50, Color.BLUE);
		x = new TargetedAccelerator(0, 2000, 1000);
		y = new TargetedAccelerator(0, 2000, 1000);

		layoutXProperty().addListener((a,b,c) -> {
			//x.resetValue(getTranslateX() + b.doubleValue() - c.doubleValue());
			update(0);
		});
		layoutYProperty().addListener((a,b,c) -> {
			//y.resetValue(getTranslateY() + b.doubleValue() - c.doubleValue());
			update(0);
		});


		setOnMousePressed(event -> {
			mouseX = event.getSceneX();
			mouseY = event.getSceneY();
		});

		setOnMouseDragged(event -> {
			double diffX = event.getSceneX() - mouseX;
			double diffY = event.getSceneY() - mouseY;
			if (diffX >= 50) {
				setLayoutX(getLayoutX() + 50);
				mouseX = event.getSceneX();
			} else if (diffX <= -50) {
				setLayoutX(getLayoutX() - 50);
				mouseX = event.getSceneX();
			}
			if (diffY >= 50) {
				setLayoutY(getLayoutY() + 50);
				mouseY = event.getSceneY();
			} else if (diffY <= -50) {
				setLayoutY(getLayoutY() - 50);
				mouseY = event.getSceneY();
			}

//			if (Math.abs(event.getSceneY() - mouseY) > 50) {
//				setLayoutY(getLayoutY() + Math.signum());
//				centerY = getLayoutY();
//				mouseY = event.getSceneY();
//			}
		});

		setOnMouseClicked(event -> {
			CompletableFuture.runAsync(() -> {
				try {
					URL url = new URL("http://145.48.205.33/api/ewZRvcXwh9rAw20Ee1oWxeqiY-VqkAJuUiHUuet9/lights/" +
							light.getId() + "/state");
					HttpURLConnection con = (HttpURLConnection) url.openConnection();
					con.setRequestMethod("PUT");
					con.setDoOutput(true);
					try (OutputStream out = con.getOutputStream()) {
						JsonWriter writer = Json.createWriter(out);
						writer.writeObject(Json.createObjectBuilder()
								.add("on", !light.getState().isOn())
								.build());
						System.out.println("Send!");
					}
					try (InputStream input = con.getInputStream()) {
						JsonReader reader = Json.createReader(input);
						JsonArray array = reader.readArray();
						for (JsonObject o : array.getValuesAs(JsonObject.class)) {
							JsonObject ack;
							if ((ack = o.getJsonObject("success")) != null) {
								System.out.println("Success!");
								light.getState().setOn(ack.getBoolean("/lights/" + light.getId() + "/state/on"));
							}
						}
						System.out.println("response: " + array);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		});
	}

	public TargetedAccelerator getTargetX() {
		return x;
	}

	public TargetedAccelerator getTargetY() {
		return y;
	}

	void update(double elapsedSeconds) {
		x.update(elapsedSeconds);
		y.update(elapsedSeconds);
		setTranslateX(x.getValue());
		setTranslateY(y.getValue());
		if (light != null) {
			light.update(elapsedSeconds);
			setFill(light.getState().getColor());
		}
		//System.out.println("x=" + x.getValue() + ", y=" + y.getValue());
	}
}
