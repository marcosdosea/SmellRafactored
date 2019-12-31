package org.smellrefactored;

public class ConfusionMatrix {

	private float truePositive = 0;	
	private float falsePositive = 0;	
	private float trueNegative = 0;	
	private float falseNegative = 0;	
	

	public float getTruePositive() {
		return truePositive;
	}

	public void incTruePositive() {
		this.truePositive++;
	}

	public void setTruePositive(float truePositive) {
		this.truePositive = truePositive;
	}

	
	public float getFalsePositive() {
		return falsePositive;
	}

	public void incFalsePositive() {
		this.falsePositive++;
	}

	public void setFalsePositive(float falsePositive) {
		this.falsePositive = falsePositive;
	}

	
	public float getTrueNegative() {
		return trueNegative;
	}

	public void incTrueNegative() {
		this.trueNegative++;
	}

	public void setTrueNegative(float trueNegative) {
		this.trueNegative = trueNegative;
	}

	
	public float getFalseNegative() {
		return falseNegative;
	}

	public void incFalseNegative() {
		this.falseNegative++;
	}

	public void setFalseNegative(float falseNegative) {
		this.falseNegative = falseNegative;
	}
		
	
	public float getPrecision() {
		return (truePositive + falsePositive) != 0 ? truePositive / (truePositive + falsePositive) : 0;
	}
	
	public float getRecall() {
		return  (truePositive + falseNegative) != 0 ? truePositive / (truePositive + falseNegative) : 0;
	}
	
	public float getFMeasure() {
		float precision = getPrecision();
		float recall = getRecall();
		return (precision + recall) != 0 ? (2 * precision * recall) / (precision + recall) : 0;
	}

	public float getAccuracy() {
		return (falsePositive + truePositive + falseNegative + trueNegative) != 0
				? (truePositive + trueNegative) / (falsePositive + truePositive + falseNegative + trueNegative)
				: 0;
	}

	public String toString() {
		return "TP=" + truePositive 
				+ " FP=" + String.valueOf(falsePositive)  
				+ " FN=" + String.valueOf(falseNegative) 
				+ " TN=" + String.valueOf(trueNegative) 
				+ " Precision=" + String.valueOf(this.getPrecision()) 
				+ " Recall=" + String.valueOf(this.getRecall()) 
				+ " F-measure=" + String.valueOf(this.getFMeasure()) 
				+ " Accuracy=" + String.valueOf(this.getAccuracy());
	}
	
}
