package org.smellrefactored.statistics;

public class ConfusionMatrix {

	private int truePositive = 0;	
	private int falsePositive = 0;	
	private int trueNegative = 0;	
	private int falseNegative = 0;	
	

	
	public void setTruePositive(int truePositive) {
		this.truePositive = truePositive;
	}
	public void incTruePositive() {
		this.truePositive++;
	}
	public int getTruePositive() {
		return truePositive;
	}

	
	
	public void setFalsePositive(int falsePositive) {
		this.falsePositive = falsePositive;
	}
	public void incFalsePositive() {
		this.falsePositive++;
	}
	public int getFalsePositive() {
		return falsePositive;
	}
	public int getType1Error() {
		return this.getFalsePositive();
	}

	
	public void setTrueNegative(int trueNegative) {
		this.trueNegative = trueNegative;
	}
	public void incTrueNegative() {
		this.trueNegative++;
	}
	public int getTrueNegative() {
		return trueNegative;
	}

	
	public void setFalseNegative(int falseNegative) {
		this.falseNegative = falseNegative;
	}
	public void incFalseNegative() {
		this.falseNegative++;
	}
	public int getFalseNegative() {
		return falseNegative;
	}
	public int getType2Error() {
		return this.getFalseNegative();
	}
		

	
	public int getRealPositive() {
		return (truePositive + falseNegative);
	}

	public int getRealNegative() {
		return (falsePositive + trueNegative);
	}
	
	public int getPredictedPositive() {
		return (truePositive + falsePositive);
	}

	public int getPredictedNegative() {
		return (falseNegative + trueNegative);
	}
	

	
	public int getSampleSize() {
		return (this.getRealPositive() + this.getRealNegative());
	}

	public int getRealSize() {
		return (this.getRealPositive() + this.getRealNegative());
	}
	public int getPredictedSize() {
		return (this.getPredictedPositive() + this.getPredictedNegative());
	}
	
	public int getHits() {
		return (truePositive + trueNegative);
	} 
	
	public int getMisses() {
		return (falsePositive + falseNegative);
	}
	

	
	public float getPrecision() {
		return (truePositive / (float) this.getPredictedPositive());
	}
	public float getPositivePredictiveValue() {
		return (this.getPrecision());
	}
	
	public float getRecall() {
		return (truePositive / (float) this.getRealPositive());
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
		return (trueNegative / (float) this.getRealNegative());
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
		return (  ((1 + beta2) * precision * recall) / (float) (precision + recall)  );
	}

	public float getF1Measure() {
		return (this.getFMeasure(1));
	}

	

	
	public float getPrevalence() {
		return (this.getRealPositive() / (float) this.getSampleSize());
	}
	
	public float getAccuracy() {
		return (this.getHits() / (float) this.getSampleSize());
	}

	public float getErrorRate() {
		return (this.getMisses() / (float) this.getSampleSize());
	}
	
	public float getNegativePredictiveValue() {
        return (this.getTrueNegative() / (float) this.getPredictedNegative());		
	}

	public float getFalseNegativeRate() {
		return (this.getFalseNegative() / (float) this.getRealPositive());
	}
	public float getMissRate() {
		return (this.getFalseNegativeRate());
	}
	
	public float getFalsePositiveRate() {
		return (this.getFalsePositive() / (float) this.getRealNegative());
	}
	public float getFallOut() {
		return (this.getFalsePositiveRate());
	}
	
	public float getFalseDiscoveryRate() {
		return (this.getFalsePositive() / (float) this.getPredictedPositive());
	}
	
	public float getFalseOmissionRate() {
		return (this.getFalseNegative()  / (float) this.getPredictedNegative());
	}
	
	public float getMatthewsCorrelationCoefficient() {
		float divider = (float) Math.sqrt( (float) this.getPredictedPositive() * (float) this.getRealPositive() * (float) this.getRealNegative() * (float) this.getPredictedNegative());
		return ( (this.getTruePositive() * this.getTrueNegative()) - (this.getFalsePositive() * this.getFalseNegative()) ) / divider;  
	}

	private float getKappaRandomAccuracy() {
		float randomAccuracyNumerator = ((float) this.getRealNegative()  * (float) this.getPredictedNegative()) + ((float) this.getRealPositive() * (float) this.getPredictedPositive());
		float randomAccuracyDivider = (float) Math.pow(this.getSampleSize(), 2);
		return (randomAccuracyNumerator / randomAccuracyDivider);
	} 
	public float getKappa() {
		float randomAccuracy = this.getKappaRandomAccuracy();
		return (this.getAccuracy() - randomAccuracy) / (1 - randomAccuracy);
	}

	
	// Based on http://standardwisdom.com/softwarejournal/2011/12/matthews-correlation-coefficient-how-well-does-it-do/
	public float getNetReward(float truePositiveReward, float trueNegativeReward, float falsePositiveReward, float falseNegativeReward) {
		float hitReward = (this.truePositive * truePositiveReward) + (this.trueNegative * trueNegativeReward); 
		float errorReward = (this.falsePositive * falsePositiveReward) + (this.falseNegative * falseNegativeReward);
		return (hitReward + errorReward); 
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
