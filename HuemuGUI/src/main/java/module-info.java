module huemu.gui {
	requires huemu.lights;

	requires javafx.controls;

	requires java.json;
	requires java.json.bind;

	requires java.net.http;
	requires java.prefs;
	requires java.sql;

	exports com.cbapps.javafx.huemu;
}