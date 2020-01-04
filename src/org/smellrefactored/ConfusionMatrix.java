package org.smellrefactored;

public class ConfusionMatrix {

	private float truePositive = 0;	
	private float falsePositive = 0;	
	private float trueNegative = 0;	
	private float falseNegative = 0;	
	

	
	public void setTruePositive(float truePositive) {
		this.truePositive = truePositive;
	}
	public void incTruePositive() {
		this.truePositive++;
	}
	public float getTruePositive() {
		return truePositive;
	}

	
	
	public void setFalsePositive(float falsePositive) {
		this.falsePositive = falsePositive;
	}
	public void incFalsePositive() {
		this.falsePositive++;
	}
	public float getFalsePositive() {
		return falsePositive;
	}
	public float getType1Error() {
		return this.getFalsePositive();
	}

	
	public void setTrueNegative(float trueNegative) {
		this.trueNegative = trueNegative;
	}
	public void incTrueNegative() {
		this.trueNegative++;
	}
	public float getTrueNegative() {
		return trueNegative;
	}

	
	public void setFalseNegative(float falseNegative) {
		this.falseNegative = falseNegative;
	}
	public void incFalseNegative() {
		this.falseNegative++;
	}
	public float getFalseNegative() {
		return falseNegative;
	}
	public float getType2Error() {
		return this.getFalseNegative();
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

	public float getF1Measure() {
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

	public float getFalseNegativeRate() {
		return (this.getFalseNegative() / this.getRealPositive());
	}
	public float getMissRate() {
		return (this.getFalseNegativeRate());
	}
	
	public float getFalsePositiveRate() {
		return (this.getFalsePositive() / this.getRealNegative());
	}
	public float getFallOut() {
		return (this.getFalsePositiveRate());
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

	private float getKappaRandomAccuracy() {
		float randomAccuracyNumerator = (this.getRealNegative()  * this.getPredictedNegative()) + (this.getRealPositive() * this.getPredictedPositive());
		float randomAccuracyDivider = (float) Math.pow(this.getSampleSize(), 2);
		return (randomAccuracyNumerator / randomAccuracyDivider);
	} 
	public float getKappa() {
		float randomAccuracy = this.getKappaRandomAccuracy();
		return (this.getAccuracy() - randomAccuracy) / (1 - randomAccuracy);
	}

	
	// Based on http://standardwisdom.com/softwarejournal/2011/12/matthews-correlation-coefficient-how-well-does-it-do/
	public float getNetReward(float truePositiveReward, float trueNegativeReward, float falsePositiveReward, float falseNegativeReward) {
		return (
				( (this.truePositive * truePositiveReward) + (this.trueNegative * trueNegativeReward) ) 
				+
				( (this.falsePositive * falsePositiveReward) + (this.falseNegative * falseNegativeReward) )
				);
	}
	
	
	public float getPositiveLikelihoodRatio( ) {
		return (this.getTruePositiveRate() / this.getFalsePositiveRate());		
	}
	public float getNegativeLikelihoodRatio() {
		return (this.getFalseNegativeRate() / this.getTrueNegativeRate());
	}
	public float getOddsRatio() {
		return (this.getPositiveLikelihoodRatio() / this.getNegativeLikelihoodRatio());
	} 
	
}
