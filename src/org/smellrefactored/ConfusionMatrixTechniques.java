package org.smellrefactored;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;

import org.repodriller.persistence.PersistenceMechanism;

public class ConfusionMatrixTechniques {
	
	private String title = "";

	private float falseNegative = 0;
	private float trueNegative = 0;
	
	private LinkedHashMap<String, ConfusionMatrix> confusionMatrices;
	private HashSet<String> sensibleTechniques;
	
	public ConfusionMatrixTechniques(String titleToResult, String[] techniques) {
		title = titleToResult;
		confusionMatrices = new LinkedHashMap<String, ConfusionMatrix>(); 
		for (String technique: techniques) {
			confusionMatrices.put(technique, new ConfusionMatrix());
		}
		sensibleTechniques = new HashSet<String>();
	}
	
	public void resetSensibleTechniques() {
		sensibleTechniques.clear();
	}

	public void incTruePositiveForSensibleTechniques(HashSet<String> techniques) {
		for (String technique: techniques) {
			if (!sensibleTechniques.contains(technique)) {
				sensibleTechniques.add(technique);
				confusionMatrices.get(technique).incTruePositive();
			}
		}
	}
	
	public void incFalsePositiveForInsensibleTechniques(HashSet<String> techniques) {
		for (String technique: techniques) {
			if (!sensibleTechniques.contains(technique)) {
				confusionMatrices.get(technique).incFalsePositive();
			}
		}
	}
	
	public void incFalseNegativeForAllTechniques() {
		falseNegative++;
		for (ConfusionMatrix confusionMatrix : confusionMatrices.values()) {
			confusionMatrix.incFalseNegative();
		}
	}

	public void incTrueNegativeForAllTechniques() {
		trueNegative++;
		for (ConfusionMatrix confusionMatrix : confusionMatrices.values()) {
			confusionMatrix.incTrueNegative();
		}
	}
	
	public boolean hasInsensibleTechniques() {
		return (confusionMatrices.size() > 0) && (sensibleTechniques.size() < confusionMatrices.size());
	}
	
	public void writeToCsvFile(PersistenceMechanism persistenceMechanism) {
		persistenceMechanism.write(title.toUpperCase());
		
		persistenceMechanism.write("True Negative = ", trueNegative);
		persistenceMechanism.write("False Negative = ", falseNegative);

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("False Positive (" + technique + ") = ", confusionMatrices.get(technique).getFalsePositive());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("True Positive (" + technique + ") = ", confusionMatrices.get(technique).getTruePositive());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Precision (" + technique + ") = ", confusionMatrices.get(technique).getPrecision());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Recall (" + technique + ") = ", confusionMatrices.get(technique).getRecall());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("F-measure (" + technique + ") = ", confusionMatrices.get(technique).getFMeasure());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Accuracy (" + technique + ") = ", confusionMatrices.get(technique).getAccuracy());
		}
		
		persistenceMechanism.write("");
		}

}
