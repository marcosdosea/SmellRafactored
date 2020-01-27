package org.smellrefactored.statistics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.repodriller.persistence.PersistenceMechanism;

public class ConfusionMatrixPredictors {
	
	private String title = "";
	private ArrayList<String> subtitles = new ArrayList<String>();
	private LinkedHashMap<String, String> otherFields = new LinkedHashMap<String, String>();

	private int commonFalseNegative = 0;
	private int commonTrueNegative = 0;
	
	private LinkedHashMap<String, ConfusionMatrix> confusionMatrices;
	private PredictionRound predictionRound = null;

	private boolean enableValidations = false;
	private String validationMessagePrefix = "? ";
	private Integer validationRealPositive = null;
	private Integer validationPreditive = null;
	private LinkedHashMap<String, Integer> validationPositivePrediction = new LinkedHashMap<String, Integer>();
	
	public ConfusionMatrixPredictors(String titleToResult, Set<String> predictors) {
		title = titleToResult;
		subtitles.clear();
		confusionMatrices = new LinkedHashMap<String, ConfusionMatrix>(); 
		for (String predictor: predictors) {
			confusionMatrices.put(predictor, new ConfusionMatrix());
		}
	}
	public Set<String> getPredictores() {
		return (confusionMatrices.keySet());
	}	
	
	public void addSubtitle(String subtitle) {
		this.subtitles.add(subtitle);
	}
	
	public void addField(String name, String value) {
		this.otherFields.put(name, value);
	}
	
	
	
	
	public void incFalseNegativeForAll() {
		commonFalseNegative++;
		for (ConfusionMatrix confusionMatrix : confusionMatrices.values()) {
			confusionMatrix.incFalseNegative();
		}
	}

	public void incTrueNegativeForAll() {
		commonTrueNegative++;
		for (ConfusionMatrix confusionMatrix : confusionMatrices.values()) {
			confusionMatrix.incTrueNegative();
		}
	}
	
	public void incTruePositiveFor(HashSet<String> predictors) {
		for (String predictor: predictors) {
			confusionMatrices.get(predictor).incTruePositive();
		}
	}

	public void incFalseNegativeForAllExcept(HashSet<String> exceptPredictors) {
		for (String predictor: confusionMatrices.keySet()) {
			if (!exceptPredictors.contains(predictor)) {
				confusionMatrices.get(predictor).incFalseNegative();
			}
		}
	}
	
	public void incFalsePositiveFor(HashSet<String> predictors) {
		for (String predictor: predictors) {
			confusionMatrices.get(predictor).incFalsePositive();
		}
	}

		
	public void incTrueNegativeForAllExcept(HashSet<String> exceptPredictors) {
		for (String predictor: confusionMatrices.keySet()) {
			if (!exceptPredictors.contains(predictor)) {
				confusionMatrices.get(predictor).incTrueNegative();
			}
		}
	}
	
	public ConfusionMatrix get(String predictor) {
		return (confusionMatrices.get(predictor));
	}

	
	public void setValidationRealPositive(int value) {
		this.validationRealPositive = value;
	}
	public void setValidationPrediction(int value) {
		this.validationPreditive = value;
	}
	public void setValidationPositivePrediction(String predictor, int value) {
		this.validationPositivePrediction.put(predictor, value);
	}
	
	
	public PredictionRound newRound() {
		this.predictionRound = new PredictionRound(confusionMatrices.keySet());
		return (this.predictionRound);
	}

	public PredictionRound round() throws Exception {
		if (this.predictionRound == null) {
			throw new Exception("There is no current round.");
		}
		return (this.predictionRound);
	}
	
	public void processCurrentRoundAndDestroy() throws Exception {
		this.processPredictionRound(this.round());
		this.predictionRound = null;
	}

	public void processPredictionRound(PredictionRound predictionRound) {
		try {
			predictionRound.putPredictionsInConfusionMatrices(confusionMatrices);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private void writeValidationRealPositiveToCsvFile(PersistenceMechanism persistenceMechanism) {
		if (this.validationRealPositive!=null) {
			for (String predictor: confusionMatrices.keySet()) {
				if (confusionMatrices.get(predictor).getRealPositive() != this.validationRealPositive) {
					persistenceMechanism.write(validationMessagePrefix + "Real Positive (" + predictor + ") found differ from expected", confusionMatrices.get(predictor).getRealPositive(), this.validationRealPositive);
				}
			}
		} else {
			Integer sampleValue = null;
			boolean isDiff = false;
			for (String predictor: confusionMatrices.keySet()) {
				if (sampleValue == null) {
					sampleValue = confusionMatrices.get(predictor).getRealPositive(); 
				}
				isDiff = (isDiff || (sampleValue != confusionMatrices.get(predictor).getRealPositive()));
			}
			if (isDiff) {
				for (String predictor: confusionMatrices.keySet()) {
					persistenceMechanism.write(validationMessagePrefix + "Real Positive (" + predictor + ") = ", confusionMatrices.get(predictor).getRealPositive());
				}
			}
		}
	}

	private void writeValidationRealNegativeToCsvFile(PersistenceMechanism persistenceMechanism) {
		Integer sampleValue = null;
		boolean isDiff = false;
		for (String predictor: confusionMatrices.keySet()) {
			if (sampleValue == null) {
				sampleValue = confusionMatrices.get(predictor).getRealNegative(); 
			}
			isDiff = (isDiff || (sampleValue != confusionMatrices.get(predictor).getRealNegative()));
		}
		if (isDiff) {
			for (String predictor: confusionMatrices.keySet()) {
				persistenceMechanism.write(validationMessagePrefix + "Real Negative (" + predictor + ") = ", confusionMatrices.get(predictor).getRealNegative());
			}
		}
	}
	
	private void writeValidationRealSizeToCsvFile(PersistenceMechanism persistenceMechanism) {
		Integer sampleValue = null;
		boolean isDiff = false;
		for (String predictor: confusionMatrices.keySet()) {
			if (sampleValue == null) {
				sampleValue = confusionMatrices.get(predictor).getRealSize(); 
			}
			isDiff = (isDiff || (sampleValue != confusionMatrices.get(predictor).getRealSize()));
		}
		if (isDiff) {
			for (String predictor: confusionMatrices.keySet()) {
				persistenceMechanism.write(validationMessagePrefix + "Real Size (" + predictor + ") = ", confusionMatrices.get(predictor).getRealSize());
			}
		}
	}

	
	private void writeValidationPredictionToCsvFile(PersistenceMechanism persistenceMechanism) {
		for (String predictor: confusionMatrices.keySet()) {
			int predictionCount = confusionMatrices.get(predictor).getPredictedSize();
			if ( (this.validationPreditive!=null) && (predictionCount != this.validationPreditive) ) {
				persistenceMechanism.write(validationMessagePrefix + "Prediction (" + predictor + ") found differ from expected", predictionCount, this.validationPreditive);
			}
		}
		for (String predictor : this.validationPositivePrediction.keySet()) {
			int positivePredictionExpected = this.validationPositivePrediction.get(predictor);
			int positivePredictionFound = (int) (confusionMatrices.get(predictor).getTruePositive() + confusionMatrices.get(predictor).getFalsePositive());
			if (positivePredictionFound != positivePredictionExpected) { 
				persistenceMechanism.write(validationMessagePrefix + "Positive Predictions (" + predictor + ") found differ from expected", positivePredictionFound, positivePredictionExpected);
			}
		}
	}
	private void writeValidationPredictionSizeToCsvFile(PersistenceMechanism persistenceMechanism) {
		Integer sampleValue = null;
		boolean isDiff = false;
		for (String predictor: confusionMatrices.keySet()) {
			if (sampleValue == null) {
				sampleValue = confusionMatrices.get(predictor).getPredictedSize(); 
			}
			isDiff = (isDiff || (sampleValue != confusionMatrices.get(predictor).getPredictedSize()));
		}
		if (isDiff) {
			for (String predictor: confusionMatrices.keySet()) {
				persistenceMechanism.write(validationMessagePrefix + "Predicted Size (" + predictor + ") = ", confusionMatrices.get(predictor).getPredictedSize());
			}
		}
	}
	private void writeValidationPredictionSizeByRealSizeToCsvFile(PersistenceMechanism persistenceMechanism) {
		for (String predictor : this.validationPositivePrediction.keySet()) {
			int predictedSize = this.confusionMatrices.get(predictor).getPredictedSize();
			int realSize = this.confusionMatrices.get(predictor).getRealSize();
			if (predictedSize != realSize) { 
				persistenceMechanism.write(validationMessagePrefix + "Predicted Size (" + predictor + ") found differ from Real Size", predictedSize, realSize);
			}
		}
		for (String predictor: confusionMatrices.keySet()) {
			float testPerSample = ( confusionMatrices.get(predictor).getPredictedSize() / (float) confusionMatrices.get(predictor).getRealSize() );
			if (testPerSample != 1.000000) {
				persistenceMechanism.write(validationMessagePrefix + "Test per sample (" + predictor + ") = ", ( confusionMatrices.get(predictor).getPredictedSize() / (float) confusionMatrices.get(predictor).getRealSize() ) );
			}
		}

	}

	
	private void writeValidationToCsvFile(PersistenceMechanism persistenceMechanism) {
		writeValidationRealPositiveToCsvFile(persistenceMechanism);
		writeValidationRealNegativeToCsvFile(persistenceMechanism);
		writeValidationRealSizeToCsvFile(persistenceMechanism);
		writeValidationPredictionToCsvFile(persistenceMechanism);
		writeValidationPredictionSizeToCsvFile(persistenceMechanism);
		writeValidationPredictionSizeByRealSizeToCsvFile(persistenceMechanism);
		}
	
	
	public void enableValidations(boolean onOff) {
		this.enableValidations = onOff;
	}
	
	
	
	public void writeToCsvFile(PersistenceMechanism persistenceMechanism) {
		
		persistenceMechanism.write(title.toUpperCase());

		for (String subtitleLine: this.subtitles) {
			persistenceMechanism.write(subtitleLine);
		}
		
		if (this.enableValidations) {
			this.writeValidationToCsvFile(persistenceMechanism);
		}

		for (String fieldLabel: this.otherFields.keySet()) {
			persistenceMechanism.write(fieldLabel + " = ", this.otherFields.get(fieldLabel));
		}

		
		if (commonTrueNegative > 0) {
			persistenceMechanism.write("Common True Negative = ", commonTrueNegative);
		}
		if (commonFalseNegative > 0) {
			persistenceMechanism.write("Common False Negative = ", commonFalseNegative);
		}

		/*
		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Predicted Size (" + predictor + ") = ", confusionMatrices.get(predictor).getPredictedSize() );
		}
		*/
		
		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Sample Size (" + predictor + ") = ", confusionMatrices.get(predictor).getSampleSize() );
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("True Negative (" + predictor + ") = ", confusionMatrices.get(predictor).getTrueNegative());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("False Negative (" + predictor + ") = ", confusionMatrices.get(predictor).getFalseNegative());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("True Positive (" + predictor + ") = ", confusionMatrices.get(predictor).getTruePositive());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("False Positive (" + predictor + ") = ", confusionMatrices.get(predictor).getFalsePositive());
		}

		
		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Accuracy (" + predictor + ") = ", confusionMatrices.get(predictor).getAccuracy());
		}
		
		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Precision (" + predictor + ") = ", confusionMatrices.get(predictor).getPrecision());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Recall (" + predictor + ") = ", confusionMatrices.get(predictor).getRecall());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("F-measure (" + predictor + ") = ", confusionMatrices.get(predictor).getF1Measure());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Matthews Correlation Coefficient (" + predictor + ") = ", confusionMatrices.get(predictor).getMatthewsCorrelationCoefficient());
		}

		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Kappa (" + predictor + ") = ", confusionMatrices.get(predictor).getKappa());
		}

		/*
		float truePositiveReward  = 10; 
		float trueNegativeReward  = 1; 
		float falsePositiveReward = -3;
		float falseNegativeReward = -10;
		for (String predictor: confusionMatrices.keySet()) {
			persistenceMechanism.write("Net Reward (Weight: TP=" + truePositiveReward + " TN=" + trueNegativeReward + " FP=" + falsePositiveReward + " FN=" + falseNegativeReward + ") (" + predictor + ") = ", confusionMatrices.get(predictor).getNetReward(truePositiveReward, trueNegativeReward, falsePositiveReward, falseNegativeReward));
		}
		*/
		
		}

}
