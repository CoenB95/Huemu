module huemu {
	requires HueLightLib;

	requires javafx.controls;

	requires gson;

	requires java.net.http;
	requires java.prefs;
	requires java.sql;

	opens com.cbapps.javafx.huemu to gson;

	exports com.cbapps.javafx.huemu;
}