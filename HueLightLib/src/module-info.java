module HueLightLib {
	requires gson;
	opens com.cbapps.java.huelight to gson;

	exports com.cbapps.java.huelight;
}