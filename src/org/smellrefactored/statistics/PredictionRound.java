package org.smellrefactored.statistics;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class PredictionRound {

	private Boolean defaultCondition = null;
	private Boolean realCondition = null;
	private HashSet<String> allPredictors = new HashSet<String>();
	private LinkedHashMap<String, Boolean> round = new LinkedHashMap<String, Boolean>();

	public PredictionRound(Set<String> predictors) {
		for (String predictor: predictors) {
			this.allPredictors.add(predictor);
		}
	}
	
	public void resetRound() {
		round.clear();
	}

	
	public void setDefaultCondition(Boolean defaultCondition) {
		this.defaultCondition = defaultCondition;
	}
	public void setCondition(Boolean condition) throws Exception {
		if ( (this.realCondition != null) && (condition != this.realCondition) ) {
			throw new Exception("Condition has already been defined.");
		}
		this.realCondition = condition;
	}
	public Boolean getCondition() throws Exception {
		Boolean result = (this.realCondition != null ? this.realCondition : this.defaultCondition); 
		if (result  == null) {
			throw new Exception("The condition has not been defined.");
		}
		return result;
	}
	
	
	
	private void thowExceptionIfPredictorIsAlreadyInRound(String predictor, Boolean prediction) throws Exception {
		if (round.containsKey(predictor)) {
			if (round.get(predictor) != prediction) {
				throw new Exception("predictor '" + predictor + "' is already in the round.");
			}
		}
	}

	private void thowExceptionIfPredictorIsNotInRound(String predictor) throws Exception {
		if (!round.containsKey(predictor)) {
			throw new Exception("predictor '" + predictor + "' does not exist in the round.");
		}
	}

	
	public void set(HashSet<String> predictors, Boolean prediction) throws Exception {
		for (String predictor: predictors) {
			thowExceptionIfPredictorIsAlreadyInRound(predictor, prediction);
			round.put(predictor, prediction);
		}
	}
	public void setTrue(HashSet<String> predictors) throws Exception {
		set(predictors, true);
	}
	public void setFalse(HashSet<String> predictors) throws Exception {
		set(predictors, false);
	}
	public void setNull(HashSet<String> predictors) throws Exception {
		set(predictors, null);
	}

	
	public void setAllExcept(HashSet<String> exceptPredictors, Boolean prediction) throws Exception {
		for (String predictor: allPredictors) {
			if (!exceptPredictors.contains(predictor)) {
				thowExceptionIfPredictorIsAlreadyInRound(predictor, prediction);
				round.put(predictor, prediction);
			}
		}
	}
	public void setTrueAllExcept(HashSet<String> exceptPredictors) throws Exception {
		setAllExcept(exceptPredictors, true);
	}
	public void setFalseAllExcept(HashSet<String> exceptPredictors) throws Exception {
		setAllExcept(exceptPredictors, false);
	}
	public void setNullAllExcept(HashSet<String> exceptPredictors) throws Exception {
		setAllExcept(exceptPredictors, null);
	}

	
	public void setIfOutOfRound(HashSet<String> predictors, Boolean prediction) {
		for (String predictor: predictors) {
			if (!round.containsKey(predictor)) {
				round.put(predictor, prediction);
			}
		}
	}
	public void setTrueIfOutOfRound(HashSet<String> predictors) {
		setIfOutOfRound(predictors, true);
	}
	public void setFalseIfOutOfRound(HashSet<String> predictors) {
		setIfOutOfRound(predictors, false);
	}
	public void setNullIfOutOfRound(HashSet<String> predictors) {
		setIfOutOfRound(predictors, null);
	}
	

	public void setForAllOutOfRound(Boolean prediction) {
		for (String predictor: allPredictors) {
			if (!round.containsKey(predictor)) {
				round.put(predictor, prediction);
			}
		}
	}
	public void setTrueForAllOutOfRound() {
		setForAllOutOfRound(true);
	}
	public void setFalseForAllOutOfRound() {
		setForAllOutOfRound(false);
	}
	public void setNullForAllOutOfRound() {
		setForAllOutOfRound(null);
	}

	
	public void setForAllOutOfRoundExcept(HashSet<String> exceptPredictors, Boolean prediction) {
		for (String predictor: allPredictors) {
			if (!round.containsKey(predictor)) {
				if (!exceptPredictors.contains(predictor)) {
					round.put(predictor, prediction);
				}
			}
		}
	}
	public void setTrueForAllOutOfRoundExcept(HashSet<String> exceptPredictors) {
		setForAllOutOfRoundExcept(exceptPredictors, true);
	}
	public void setFalseForAllOutOfRoundExcept(HashSet<String> exceptPredictors) {
		setForAllOutOfRoundExcept(exceptPredictors, false);
	}
	public void setNullForAllOutOfRoundExcept(HashSet<String> exceptPredictors) {
		setForAllOutOfRoundExcept(exceptPredictors, null);
	}

	public Boolean getPredition(String predictor) {
		Boolean prediction = null;
		if (round.containsKey(predictor)) {
			prediction = round.get(predictor);
		}
		return (prediction);
	}
	
	public boolean isAnyoneOutOfRound() {
		return (allPredictors.size() > 0) && (round.size() < allPredictors.size());
	}

	public boolean isAnyoneInRound() {
		return (round.size() > 0);
	}
	
	public void putPredictionInConfusionMatrix(String predictor, ConfusionMatrix confusionMatrix) throws Exception {
		if (confusionMatrix == null) {
			throw new Exception("there is no confusion matrix for predictor " + predictor + ".");
		}
		thowExceptionIfPredictorIsNotInRound(predictor);
		Boolean condition = this.getCondition();
		Boolean prediction = round.get(predictor);
		if (prediction != null) {
			if ( (condition) && (prediction) ) {
				confusionMatrix.incTruePositive();
			} else if ( (condition) && (!prediction) ) {
				confusionMatrix.incFalseNegative();
			} else if ( (!condition) && (prediction) ) {
				confusionMatrix.incFalsePositive();
			} else if ( (!condition) && (!prediction) ) {
				confusionMatrix.incTrueNegative();
			}
		}
	}

	public void putPredictionsInConfusionMatrices(LinkedHashMap<String, ConfusionMatrix> confusionMatrices) throws Exception {
		for (String predictor: this.allPredictors) {
			thowExceptionIfPredictorIsNotInRound(predictor);
		}
		for (String predictor: this.allPredictors) {
			ConfusionMatrix confusionMatrix = confusionMatrices.get(predictor);
			putPredictionInConfusionMatrix(predictor, confusionMatrix);
		}
	}
	
}
