package org.smellrefactored;

import org.junit.Assert;
import org.junit.Test;
import org.smellrefactored.statistics.ConfusionMatrix;

public class ConfusionMatrixTest {

	@Test
	public void test() {
		ConfusionMatrix confusionMatrix = new ConfusionMatrix();

		confusionMatrix.setTruePositive(34256);
		confusionMatrix.setTrueNegative(96773);
		confusionMatrix.setFalsePositive(32758);
		confusionMatrix.setFalseNegative(88521);

		Assert.assertEquals(34256, confusionMatrix.getTruePositive(), 0);
		Assert.assertEquals(96773, confusionMatrix.getTrueNegative(), 0);
		Assert.assertEquals(32758, confusionMatrix.getFalsePositive(), 0);
		Assert.assertEquals(88521, confusionMatrix.getFalseNegative(), 0);

		confusionMatrix.incTruePositive();
		confusionMatrix.incTrueNegative();
		confusionMatrix.incFalsePositive();
		confusionMatrix.incFalseNegative();

		Assert.assertEquals(34257, confusionMatrix.getTruePositive(), 0);
		Assert.assertEquals(96774, confusionMatrix.getTrueNegative(), 0);
		Assert.assertEquals(32759, confusionMatrix.getFalsePositive(), 0);
		Assert.assertEquals(88522, confusionMatrix.getFalseNegative(), 0);
		
		
		Assert.assertEquals((34257*10 + 96774*5) + (32759*-8 + 88522*-3) , confusionMatrix.getNetReward( 10, 5, -8, -3), 0);
				
		Assert.assertEquals(122779, confusionMatrix.getRealPositive(), 0);
		Assert.assertEquals(129533, confusionMatrix.getRealNegative(), 0);
		Assert.assertEquals(67016, confusionMatrix.getPredictedPositive(), 0);
		Assert.assertEquals(185296, confusionMatrix.getPredictedNegative(), 0);
		
		Assert.assertEquals(252312, confusionMatrix.getSampleSize(), 0);
		
		
		Assert.assertEquals(0.2790, confusionMatrix.getSensitivity(), 0.0001); // TPR = TP / (TP + FN)
		Assert.assertEquals(0.2790, confusionMatrix.getRecall(), 0.0001); // TPR = TP / (TP + FN)
		Assert.assertEquals(0.7471, confusionMatrix.getSpecificity(), 0.0001); // SPC = TN / (FP + TN)
		Assert.assertEquals(0.5112, confusionMatrix.getPrecision(), 0.0001); // PPV = TP / (TP + FP)
		Assert.assertEquals(0.5223, confusionMatrix.getNegativePredictiveValue(), 0.0001); // NPV = TN / (TN + FN)
		Assert.assertEquals(0.2529, confusionMatrix.getFalsePositiveRate(), 0.0001); // FPR = FP / (FP + TN)
		Assert.assertEquals(0.4888, confusionMatrix.getFalseDiscoveryRate(), 0.0001); // FDR = FP / (FP + TP)
		Assert.assertEquals(0.7210, confusionMatrix.getFalseNegativeRate(), 0.0001); // FNR = FN / (FN + TP)
		Assert.assertEquals(0.5193, confusionMatrix.getAccuracy(), 0.0001); // ACC = (TP + TN) / (P + N)
		Assert.assertEquals(0.3610, confusionMatrix.getF1Measure(), 0.0001); // F1 = 2TP / (2TP + FP + FN)
		Assert.assertEquals(0.0296, confusionMatrix.getMatthewsCorrelationCoefficient(), 0.0001); // TP*TN - FP*FN / sqrt((TP+FP)*(TP+FN)*(TN+FP)*(TN+FN))
		Assert.assertEquals(0.026, confusionMatrix.getKappa(), 0.001);
	}

}
