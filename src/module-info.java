module Huemu {
	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires gson;
	requires jdk.httpserver;
	requires HueLightLib;
	requires java.net.http;
	requires java.prefs;
	requires java.sql;

	opens com.cbapps.javafx.huemu to gson;

	exports com.cbapps.javafx.huemu;
}