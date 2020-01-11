package org.smellrefactored;

import java.util.HashSet;
import java.util.LinkedHashMap;

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
	
	public void incTrueNegativeForInsensibleTechniquesExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!sensibleTechniques.contains(technique)) {
				if (!exceptTechniques.contains(technique)) {
					confusionMatrices.get(technique).incTrueNegative();
				}
			}
		}
	}
	
	public void incFalseNegativeForInsensibleTechniquesExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!sensibleTechniques.contains(technique)) {
				if (!exceptTechniques.contains(technique)) {
					sensibleTechniques.add(technique);
					confusionMatrices.get(technique).incFalseNegative();
				}
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
		
		persistenceMechanism.write("Common True Negative = ", trueNegative);
		persistenceMechanism.write("Common False Negative = ", falseNegative);

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Sample Size (" + technique + ") = ", confusionMatrices.get(technique).getSampleSize());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("True Negative (" + technique + ") = ", confusionMatrices.get(technique).getTrueNegative());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("False Negative (" + technique + ") = ", confusionMatrices.get(technique).getFalseNegative());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("True Positive (" + technique + ") = ", confusionMatrices.get(technique).getTruePositive());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("False Positive (" + technique + ") = ", confusionMatrices.get(technique).getFalsePositive());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Accuracy (" + technique + ") = ", confusionMatrices.get(technique).getAccuracy());
		}
		
		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Precision (" + technique + ") = ", confusionMatrices.get(technique).getPrecision());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Recall (" + technique + ") = ", confusionMatrices.get(technique).getRecall());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("F-measure (" + technique + ") = ", confusionMatrices.get(technique).getF1Measure());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Matthews Correlation Coefficient (" + technique + ") = ", confusionMatrices.get(technique).getMatthewsCorrelationCoefficient());
		}

		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Kappa (" + technique + ") = ", confusionMatrices.get(technique).getKappa());
		}

		float truePositiveReward  = 10; 
		float trueNegativeReward  = 1; 
		float falsePositiveReward = -3;
		float falseNegativeReward = -10;
		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Net Reward (Weight: TP=" + truePositiveReward + " TN=" + trueNegativeReward + " FP=" + falsePositiveReward + " FN=" + falseNegativeReward + ") (" + technique + ") = ", confusionMatrices.get(technique).getNetReward(truePositiveReward, trueNegativeReward, falsePositiveReward, falseNegativeReward));
		}
		
		}

}
