package org.smellrefactored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.repodriller.persistence.PersistenceMechanism;

public class ConfusionMatrixTechniques {
	
	private String title = "";
	private ArrayList<String> subtitles = new ArrayList<String>();

	private float commonFalseNegative = 0;
	private float commonTrueNegative = 0;
	private Float realPositiveValidation = null;
	private Float preditiveValidation = null;
	
	private LinkedHashMap<String, ConfusionMatrix> confusionMatrices;
	private HashSet<String> techniquesInRound;
	
	public ConfusionMatrixTechniques(String titleToResult, String[] techniques) {
		title = titleToResult;
		subtitles.clear();
		confusionMatrices = new LinkedHashMap<String, ConfusionMatrix>(); 
		for (String technique: techniques) {
			confusionMatrices.put(technique, new ConfusionMatrix());
		}
		techniquesInRound = new HashSet<String>();
	}

	public void addSubtitle(String subtitle) {
		this.subtitles.add(subtitle);
	}
	
	
	
	
	public void resetRound() {
		techniquesInRound.clear();
	}

	public void incTruePositiveIfOutOfRound(HashSet<String> techniques) {
		for (String technique: techniques) {
			if (!techniquesInRound.contains(technique)) {
				techniquesInRound.add(technique);
				confusionMatrices.get(technique).incTruePositive();
			}
		}
	}
	
	public void incFalsePositiveIfOutOfRound(HashSet<String> techniques) {
		for (String technique: techniques) {
			if (!techniquesInRound.contains(technique)) {
				techniquesInRound.add(technique);
				confusionMatrices.get(technique).incFalsePositive();
			}
		}
	}
	
	public void incFalsePositiveForAllTechniqueOutOfRound() {
		for (String technique: confusionMatrices.keySet()) {
			if (!techniquesInRound.contains(technique)) {
				techniquesInRound.add(technique);
				confusionMatrices.get(technique).incFalsePositive();
			}
		}
	}
	
	public void incTrueNegativeForAllTechniquesOutOfRoundExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!techniquesInRound.contains(technique)) {
				if (!exceptTechniques.contains(technique)) {
					techniquesInRound.add(technique);
					confusionMatrices.get(technique).incTrueNegative();
				}
			}
		}
	}
	
	
	public void incTrueNegativeForAllTechniquesOutOfRound() {
		for (String technique: confusionMatrices.keySet()) {
			if (!techniquesInRound.contains(technique)) {
				techniquesInRound.add(technique);
				confusionMatrices.get(technique).incTrueNegative();
			}
		}
	}

	
	public void incFalseNegativeForAllTechniquesOutOfRoundExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!techniquesInRound.contains(technique)) {
				if (!exceptTechniques.contains(technique)) {
					techniquesInRound.add(technique);
					confusionMatrices.get(technique).incFalseNegative();
				}
			}
		}
	}
	

	public void incFalseNegativeForAllTechniquesOutOfRound() {
		for (String technique: confusionMatrices.keySet()) {
			if (!techniquesInRound.contains(technique)) {
				techniquesInRound.add(technique);
				confusionMatrices.get(technique).incFalseNegative();
			}
		}
	}

	
	public boolean hasTechniqueOutOfRound() {
		return (confusionMatrices.size() > 0) && (techniquesInRound.size() < confusionMatrices.size());
	}

	public boolean hasTechniqueInRound() {
		return (techniquesInRound.size() > 0);
	}

	
	public void putInRoundWithoutInc(HashSet<String> techniques) {
		for (String technique: techniques) {
			if (!techniquesInRound.contains(technique)) {
				techniquesInRound.add(technique);
			}
		}
	}
	
	public void putAllTechniquesInRoundWithoutIncExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!techniquesInRound.contains(technique)) {
				if (!exceptTechniques.contains(technique)) {
					techniquesInRound.add(technique);
				}
			}
		}
	}
	
	
	public void incFalseNegativeForAllTechniques() {
		commonFalseNegative++;
		for (ConfusionMatrix confusionMatrix : confusionMatrices.values()) {
			confusionMatrix.incFalseNegative();
		}
	}

	public void incTrueNegativeForAllTechniques() {
		commonTrueNegative++;
		for (ConfusionMatrix confusionMatrix : confusionMatrices.values()) {
			confusionMatrix.incTrueNegative();
		}
	}
	
	public void incTruePositiveForTechniques(HashSet<String> techniques) {
		for (String technique: techniques) {
			confusionMatrices.get(technique).incTruePositive();
		}
	}

	public void incFalseNegativeForAllTechniquesExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!exceptTechniques.contains(technique)) {
				confusionMatrices.get(technique).incFalseNegative();
			}
		}
	}
	
	public void incFalsePositiveForTechniques(HashSet<String> techniques) {
		for (String technique: techniques) {
			confusionMatrices.get(technique).incFalsePositive();
		}
	}

		
	public void incTrueNegativeForAllTechniquesExcept(HashSet<String> exceptTechniques) {
		for (String technique: confusionMatrices.keySet()) {
			if (!exceptTechniques.contains(technique)) {
				confusionMatrices.get(technique).incTrueNegative();
			}
		}
	}
	
	public void setRealPositiveValidation(float value) {
		this.realPositiveValidation = value;
	}
	
	public void setPredictionCountValidation(float value) {
		this.preditiveValidation = value;
	}
	
	public void writeToCsvFile(PersistenceMechanism persistenceMechanism) {
		
		persistenceMechanism.write(title.toUpperCase());

		for (String subtitleLine: this.subtitles) {
			persistenceMechanism.write(subtitleLine);
		}

		for (String technique: confusionMatrices.keySet()) {
			if ( (this.realPositiveValidation!=null) && (confusionMatrices.get(technique).getRealPositive() != this.realPositiveValidation) ) {
				persistenceMechanism.write("Warning (" + technique + ") = ", "Real positive(" + confusionMatrices.get(technique).getRealPositive() + ") differ from expected(" + this.realPositiveValidation + ").");
			}
		}
		// Conferir resultado da contagem de preditivos negativos
		for (String technique: confusionMatrices.keySet()) {
			if ( (this.preditiveValidation!=null) && ( (confusionMatrices.get(technique).getPredictedPositive() + confusionMatrices.get(technique).getPredictedNegative()) != this.preditiveValidation) ) {
				float predictionCount = confusionMatrices.get(technique).getPredictedPositive() + confusionMatrices.get(technique).getPredictedNegative();
				persistenceMechanism.write("Warning (" + technique + ") = ", "Prediction count (" + predictionCount + ") differ from expected(" + this.preditiveValidation + ").");
			}
		}

		
		if (commonTrueNegative > 0) {
			persistenceMechanism.write("Common True Negative = ", commonTrueNegative);
		}
		if (commonFalseNegative > 0) {
			persistenceMechanism.write("Common False Negative = ", commonFalseNegative);
		}

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

		/*
		float truePositiveReward  = 10; 
		float trueNegativeReward  = 1; 
		float falsePositiveReward = -3;
		float falseNegativeReward = -10;
		for (String technique: confusionMatrices.keySet()) {
			persistenceMechanism.write("Net Reward (Weight: TP=" + truePositiveReward + " TN=" + trueNegativeReward + " FP=" + falsePositiveReward + " FN=" + falseNegativeReward + ") (" + technique + ") = ", confusionMatrices.get(technique).getNetReward(truePositiveReward, trueNegativeReward, falsePositiveReward, falseNegativeReward));
		}
		*/
		
		}

}
