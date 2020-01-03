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
		return (truePositive / this.getPredictedPositive());
	}
	public float getPositivePredictiveValue() {
		return (this.getPrecision());
	}
	
	public float getRecall() {
		return (truePositive / this.getRealPositive());
	}
	public float getSensitivity() {
		return (this.getRecall());
	}
	public float getHitRate() {
		return (this.getRecall());
	}
	public float getTruePositiveRate() {
		return (this.getRecall());
	}
	
	public float getSpecificity() {
		return (trueNegative / this.getRealNegative());
	}
	public float getSelectivity() {
		return (this.getSpecificity());
	}
	public float getTrueNegativeRate() {
		return (this.getSpecificity());
	}
	
	public float getYoudensIndex() {
		return (this.getSensitivity() - (1 - this.getSpecificity()));
	} 
	
	
	public float getFMeasure(float beta) {
		float precision = this.getPrecision();
		float recall = this.getRecall();
		float beta2 = (float) Math.pow(beta, 2);
		return (  ((1 + beta2) * precision * recall) / (precision + recall)  );
	}

	public float getFMeasure() {
		return (this.getFMeasure(1));
	}

	

	
	public float getPrevalence() {
		return (this.getRealPositive() / this.getSampleSize());
	}
	
	public float getAccuracy() {
		return (this.getHits() / this.getSampleSize());
	}

	public float getErrorRate() {
		return (this.getMisses() / this.getSampleSize());
	}
	
	public float getNegativePredictiveValue() {
        return (this.getTrueNegative() / this.getPredictedNegative());		
	}

	public float getMissRate() {
		return (this.getFalseNegative() / this.getRealPositive());
	}
	public float getFalseNegativeRate() {
		return (this.getMissRate());
	}
	
	public float getFallOut() {
		return (this.getFalsePositive() / this.getRealNegative());
	}
	public float getFalsePositiveRate() {
		return (this.getFallOut());
	}
	
	public float getFalseDiscoveryRate() {
		return (this.getFalsePositive() / this.getPredictedPositive());
	}
	
	public float getFalseOmissionRate() {
		return (this.getFalseNegative()  / this.getPredictedNegative());
	}
	
	public float getMatthewsCorrelationCoefficient() {
		float divider = (float) Math.sqrt(this.getPredictedPositive() * this.getRealPositive() * this.getRealNegative() * this.getPredictedNegative());
		return ( (this.getTruePositive() * this.getTrueNegative()) - (this.getFalsePositive() * this.getFalseNegative()) ) / divider;  
	}

	public float getKappa() {
		float totalAccuracy = this.getAccuracy();
		float randomAccuracyDivider = (float) Math.pow(this.getSampleSize(), 2);
		float randomAccuracyNumerator = (this.getRealNegative()  * this.getPredictedNegative()) + (this.getRealPositive() * this.getPredictedPositive());
		float randomAccuracy = randomAccuracyNumerator / randomAccuracyDivider;
		float kappaDivider = (1-randomAccuracy);
		return (totalAccuracy - randomAccuracy) / kappaDivider;
	}
	
}
