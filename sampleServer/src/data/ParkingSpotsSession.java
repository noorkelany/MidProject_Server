package data;

import java.util.Map;

public class ParkingSpotsSession {
	private static Map<Integer, Boolean> spotMap;

	public static void setMap(Map<Integer, Boolean> map) {
		spotMap = map;
	}

	public static Map<Integer, Boolean> getMap() {
		return spotMap;
	}
}
