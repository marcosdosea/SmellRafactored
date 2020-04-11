package org.smellrefactored;

import java.util.LinkedHashMap;

public class Technique {
	
	public static LinkedHashMap<String, String> getTechniques() {
		LinkedHashMap<String, String> techniques = new LinkedHashMap<String, String>();
		techniques.put("A", "Alves (2010)");
		techniques.put("X", "Aniche (2016)");
		techniques.put("R", "Dosea (2016)");
		techniques.put("D", "Dosea (2018)");
		techniques.put("V", "Vale (2015)");
		return techniques;
	}

	public static String getTechnique(String key) {
		LinkedHashMap<String, String> techniques = getTechniques();
		return techniques.get(key);
	}

}
