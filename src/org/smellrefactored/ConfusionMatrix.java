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
		

	
	public float getRealPositive() {
		return (truePositive + falseNegative);
	}

	public float getRealNegative() {
		return (falsePositive + trueNegative);
	}
	
	public float getPredictedPositive() {
		return (truePositive + falsePositive);
	}

	public float getPredictedNegative() {
		return (falseNegative + trueNegative);
	}
	

	
	public float getSampleSize() {
		return (this.getRealPositive() + this.getRealNegative());
	}

	public float getHits() {
		return (truePositive + trueNegative);
	} 
	
	public float getMisses() {
		return (falsePositive + falseNegative);
	}
	

	
	public float getPrecision() {
		return (this.getPredictedPositive()) != 0 ? truePositive / (this.getPredictedPositive()) : 0;
	}
	public float getPositivePredictiveValue() {
		return this.getPrecision();
	}
	
	public float getRecall() {
		return  this.getRealPositive() != 0 ? truePositive / this.getRealPositive() : 0;
	}
	public float getSensitivity() {
		return  this.getRecall();
	}
	public float getHitRate() {
		return  this.getRecall();
	}
	public float getTruePositiveRate() {
		return  this.getRecall();
	}
	
	public float getSpecificity() {
		return  this.getRealNegative() != 0 ? trueNegative / this.getRealNegative() : 0;
	}
	public float getSelectivity() {
		return  this.getSpecificity();
	}
	public float getTrueNegativeRate() {
		return  this.getSpecificity();
	}
	
	
	
	public float getFMeasure(float beta) {
		float precision = this.getPrecision();
		float recall = this.getRecall();
		float beta2 = (float) Math.pow(beta, 2);
		return ((precision + recall) != 0 ? ( (1 + beta2) * precision * recall) / (precision + recall) : 0);
	}

	public float getFMeasure() {
		return (this.getFMeasure(1));
	}

	

	
	public float getPrevalence() {
		return (this.getSampleSize()) != 0 ? (this.getRealPositive()) / (this.getSampleSize()) : 0;
	}
	
	public float getAccuracy() {
		return (this.getSampleSize()) != 0 ? (this.getHits()) / (this.getSampleSize()) : 0;
	}

	public float getErrorRate() {
		return (this.getSampleSize()) != 0 ? (this.getMisses()) / (this.getSampleSize()) : 0;
	}
	
	public float getNegativePredictiveValue() {
        return this.getPredictedNegative() != 0 ? this.getTrueNegative() / this.getPredictedNegative() : 0;		
	}

	public float getMissRate() {
		return (this.getRealPositive()) != 0 ? (this.getFalseNegative()) / (this.getRealPositive()) : 0;
	}
	public float getFalseNegativeRate() {
		return this.getMissRate();
	}
	
	public float getFallOut() {
		return (this.getRealNegative()) != 0 ? (this.getFalsePositive()) / (this.getRealNegative()) : 0;
	}
	public float getFalsePositiveRate() {
		return this.getFallOut();
	}
	
	public float getFalseDiscoveryRate() {
		return this.getPredictedPositive() != 0 ? this.getFalsePositive() / this.getPredictedPositive() : 0;
	}
	
	public float getFalseOmissionRate() {
		return this.getPredictedNegative() != 0 ? this.getFalseNegative()  / this.getPredictedNegative() : 0;
	}
	
	public float getMatthewsCorrelationCoefficient() {
		float divider = (float) Math.sqrt(this.getPredictedPositive() * this.getRealPositive() * this.getRealNegative() * this.getPredictedNegative());
		return (divider != 0) ? ( (this.getTruePositive() * this.getTrueNegative()) - (this.getFalsePositive() * this.getFalseNegative()) ) / divider  : 0;  
	}
	
	
	public String toString() {
		return ("TruePositive=" + String.valueOf(this.getTruePositive())
			+ "FalsePositive=" + String.valueOf(this.getFalsePositive())
			+ "TrueNegative=" + String.valueOf(this.getTrueNegative())
			+ "FalseNegative=" + String.valueOf(this.getFalseNegative())
			+ "RealPositive=" + String.valueOf(this.getRealPositive())
			+ "RealNegative=" + String.valueOf(this.getRealNegative())
			+ "PredictedPositive=" + String.valueOf(this.getPredictedPositive())
			+ "PredictedNegative=" + String.valueOf(this.getPredictedNegative())
			+ "SampleSize=" + String.valueOf(this.getSampleSize())
			+ "Hits=" + String.valueOf(this.getHits())
			+ "Misses=" + String.valueOf(this.getMisses())
			+ "Precision=" + String.valueOf(this.getPrecision())
			+ "Recall=" + String.valueOf(this.getRecall())
			+ "Sensitivity=" + String.valueOf(this.getSensitivity())
			+ "HitRate=" + String.valueOf(this.getHitRate())
			+ "TruePositiveRate=" + String.valueOf(this.getTruePositiveRate())
			+ "Specificity=" + String.valueOf(this.getSpecificity())
			+ "Selectivity=" + String.valueOf(this.getSelectivity())
			+ "TrueNegativeRate=" + String.valueOf(this.getTrueNegativeRate())
			+ "FMeasure=" + String.valueOf(this.getFMeasure())
			+ "Prevalence=" + String.valueOf(this.getPrevalence())
			+ "Accuracy=" + String.valueOf(this.getAccuracy())
			+ "ErrorRate=" + String.valueOf(this.getErrorRate())
			+ "NegativePredictiveValue=" + String.valueOf(this.getNegativePredictiveValue())
			+ "MissRate=" + String.valueOf(this.getMissRate())
			+ "FalseNegativeRate=" + String.valueOf(this.getFalseNegativeRate())
			+ "FallOut=" + String.valueOf(this.getFallOut())
			+ "FalsePositiveRate=" + String.valueOf(this.getFalsePositiveRate())
			+ "FalseDiscoveryRate=" + String.valueOf(this.getFalseDiscoveryRate())
			+ "FalseOmissionRate=" + String.valueOf(this.getFalseOmissionRate())
			+ "MatthewsCorrelationCoefficient=" + String.valueOf(this.getMatthewsCorrelationCoefficient())
			);
	}
	
}
