package org.smellrefactored;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.statistics.ConfusionMatrix;
import org.smellrefactored.statistics.ConfusionMatrixPredictors;
import org.smellrefactored.statistics.PredictionRound;

public class SmellRefactoredMethod {

	final private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true; // Increases processing time.
	
	final private boolean IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT = true; // Increases processing time.
	
	private HashSet<String> getLongMethodRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		// refactoringTypes.add(RefactoringType.INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MERGE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.CHANGE_METHOD_SIGNATURE.toString());
		// refactoringTypes.add(RefactoringType.CHANGE_RETURN_TYPE.toString());
		return refactoringTypes;
	}

	private HashSet<String> getComplexMethodRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		// refactoringTypes.add(RefactoringType.INLINE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MERGE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.CHANGE_METHOD_SIGNATURE.toString());
		// refactoringTypes.add(RefactoringType.CHANGE_RETURN_TYPE.toString());
		return refactoringTypes;
	}
	
	private HashSet<String> getHighEfferentCouplingRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		// refactoringTypes.add(RefactoringType.INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MERGE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.CHANGE_METHOD_SIGNATURE.toString());
		// refactoringTypes.add(RefactoringType.CHANGE_RETURN_TYPE.toString());
		return refactoringTypes;
	}

	private HashSet<String> getManyParametersRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		// refactoringTypes.add(RefactoringType.INLINE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MERGE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.CHANGE_METHOD_SIGNATURE.toString());
		// // refactoringTypes.add(RefactoringType.CHANGE_RETURN_TYPE.toString());
		return refactoringTypes;
	}
	
	private HashSet<String> getMethodRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		// Add all smell refactoring groups here.
		refactoringTypes.addAll(this.getLongMethodRefactoringTypes());
		refactoringTypes.addAll(this.getComplexMethodRefactoringTypes());
		refactoringTypes.addAll(this.getHighEfferentCouplingRefactoringTypes());
		refactoringTypes.addAll(this.getManyParametersRefactoringTypes());
		return refactoringTypes;
	}
	
	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private RefactoringEvents refactoringEvents;
	private CommitMethodSmell commitMethodSmell;
	private ArrayList<String> smellCommitIds;
	private CommitRange commitRange;
	private String resultFileName;
	private PersistenceMechanism pmResultEvaluation;
	private OutputFilesMethod methodOutputFiles;

	public SmellRefactoredMethod(RefactoringEvents refactoringEvents, ArrayList<String> smellCommitIds, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) {
		this.refactoringEvents = refactoringEvents;
		this.smellCommitIds = smellCommitIds;
		this.commitRange = commitRange;
		this.commitMethodSmell = new CommitMethodSmell(commitSmell);
		this.resultFileName = resultFileName;
		pmResultEvaluation = new CSVFile(resultFileName + "-evaluation-methods.csv", false);
		methodOutputFiles = new OutputFilesMethod(this.resultFileName);
	}
	
	public void getSmellRefactoredMethods() {
		try {
			logger.info("** Starting method analysis [[[");
			
			pmResultEvaluation.write("ANALYSIS OF PREDICTION OF REFACTORING IN METHODS");
			pmResultEvaluation.write("Performed on:", Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace( "T" , " "));
			pmResultEvaluation.write("Repository url:", this.commitRange.getRepositoryUrl());
			pmResultEvaluation.write("Commit range:", this.commitRange.getInitialCommitId(), this.commitRange.getFinalCommitId());
			pmResultEvaluation.write("");
			pmResultEvaluation.write("REFACTORINGS");
			pmResultEvaluation.write("Total number of refactorings detected:", refactoringEvents.getAllMergedIntoMaster().size());
			pmResultEvaluation.write("Refactorings related to methods:", this.refactoringEvents.countTypes(getMethodRefactoringTypes()));
			for (String refactoringType: this.getMethodRefactoringTypes()) {
				pmResultEvaluation.write("Number of " + refactoringType + ":", this.refactoringEvents.countType(refactoringType));
			}
			pmResultEvaluation.write("Refactorings on the initial commit were ignored");
			pmResultEvaluation.write("");
			pmResultEvaluation.write("SMELLS");
			pmResultEvaluation.write("Initial number of commits to begin analysis:", this.smellCommitIds.size());
			pmResultEvaluation.write("Threshold derivation techniques:", this.commitMethodSmell.getTechniquesThresholds().size(), this.commitMethodSmell.getTechniquesThresholds().keySet());
			pmResultEvaluation.write("Smells on the final commit were ignored");
			if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
				pmResultEvaluation.write("Unconfirmed predictions that were repeated following commit were ignored");
			}
			pmResultEvaluation.write("");
			
			methodOutputFiles.writeHeaders();

			evaluateInDetailSmellChangeOperation(MethodDataSmelly.LONG_METHOD           , this.getLongMethodRefactoringTypes(), this.smellCommitIds);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.COMPLEX_METHOD        , this.getComplexMethodRefactoringTypes(), this.smellCommitIds);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.HIGH_EFFERENT_COUPLING, this.getHighEfferentCouplingRefactoringTypes(), this.smellCommitIds);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.MANY_PARAMETERS       , this.getManyParametersRefactoringTypes(), this.smellCommitIds);
			
			methodOutputFiles.close();

			logger.info("]]] Mathod analyzes completed.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, ArrayList<String> smellCommitIds) throws Exception {
		evaluateSmellChangeOperation(smellCommitIds, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeOperation(smellCommitIds, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}
	
	private void evaluateSmellChangeOperation(ArrayList<String> smellCommitIds,
			String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {
		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(typeSmell + " " + targetTefactoringTypes.toString(), this.commitMethodSmell.getTechniquesThresholds().keySet());
		confusionMatrices.enableValidations(!IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT);
		// TP and FN
		computeTruePositiveAndFalseNegative(typeSmell, targetTefactoringTypes, confusionMatrices);
		for (String smellCommitId: smellCommitIds) {
			FilterSmellResult smellResult = this.commitMethodSmell.getSmellsFromCommit(smellCommitId);
			// FP and TN
			computeFalsePositiveAndTrueNegativeForAllTechniques(smellResult, typeSmell, targetTefactoringTypes, confusionMatrices);
		}
		/* 
         * Warning: The validation of the confusion matrix by the total of positive predictions 
         *          does not apply to the analysis of smells only from the initial commit.
         * 
		 * int realPositive = SmellRefactoredManager.countRealPositive(refactoringsCounter, targetTefactoringTypes);
		 * confusionMatrices.setValidationRealPositive(realPositive);
		 * for (String technique : this.commitSmell.getTechniquesThresholds().keySet()) {
		 * 	Integer positivePredictionExpected = this.commitSmell.countMethodSmellPredictionForTechniqueInCommit(smellResult, typeSmell, technique);
		 * 	confusionMatrices.setValidationPositivePrediction(technique, positivePredictionExpected);
		 * }
		*/
		pmResultEvaluation.write("");
		confusionMatrices.addField("Total number of refactorings", this.refactoringEvents.countTypes(targetTefactoringTypes));
		confusionMatrices.writeToCsvFile(pmResultEvaluation);
		// confusionMatrices.saveToCsvFile(this.resultFileName + "-confusionMatrices-method-" + typeSmell + "-" + targetTefactoringTypes.toString() + ".csv");
	}
	
	private void computeTruePositiveAndFalseNegative(String smellType,
			HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (RefactoringEvent refactoring : refactoringEvents.getAllMergedIntoMaster()) {
			if (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				continue;
			}
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setCondition(true);
			CommitData previousCommit = this.commitRange.getPreviousCommit(refactoring.getCommitId());
			MethodDataSmelly methodSmellOrNotSmell = this.commitMethodSmell.getSmellOrNotSmellCommitForMethod(previousCommit.getId(), refactoring.getFileNameBefore(), refactoring.getClassName(), refactoring.getMethodName(), smellType); 
			if ( (methodSmellOrNotSmell != null) && (methodSmellOrNotSmell.getSmell() != null) && (!methodSmellOrNotSmell.getSmell().isEmpty()) ) {
				predictionRound.setTrue(methodSmellOrNotSmell.getListaTecnicas());
				predictionRound.setFalseAllExcept(methodSmellOrNotSmell.getListaTecnicas());
				this.methodOutputFiles.writeTruePositiveToCsvFiles(refactoring, methodSmellOrNotSmell);
				if (predictionRound.isAnyoneOutOfRound()) {
					this.methodOutputFiles.writeFalseNegativeToCsvFiles(refactoring, methodSmellOrNotSmell);
				}
			} else {
				predictionRound.setFalseForAllOutOfRound();
				this.methodOutputFiles.writeFalsePositiveToCsvFiles(refactoring, methodSmellOrNotSmell);
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}
	
	private void computeFalsePositiveAndTrueNegativeForAllTechniques(FilterSmellResult smellResult,
			String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (String technique : confusionMatrices.getPredictores()) {
			computeFalsePositiveBySmellAndTechnique(smellResult, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			computeTrueNegativeBySmellAndTechnique(smellResult, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			}
	}
	
	private void computeFalsePositiveBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<MethodDataSmelly> smellyMethodsForSelectedTechniqueSmell = this.commitMethodSmell.getSmellingMethodsBySmellAndTechnique(smellResult, smellType, technique);
		for (MethodDataSmelly methodSmelly : smellyMethodsForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(methodSmelly.getCommit());
			if (nextCommit != null) {
 				if (!this.refactoringEvents.hasMethodRefactoringsInCommit(nextCommit.getId(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = this.commitMethodSmell.hasMethodSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo()); 
 					}
					if (!ignoreCurrentPrediction) {
						confusionMtrix.incFalsePositive();
						this.methodOutputFiles.writeNegativeToCsvFiles(methodSmelly);
					}
				}
			} else {
				confusionMtrix.incFalsePositive();
				this.methodOutputFiles.writeNegativeToCsvFiles(methodSmelly);
			}
		}
	}
	
	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<MethodDataSmelly> notSmellyMethodsForSelectedTechniqueSmell = this.commitMethodSmell.getNotSmellingMethodsBySmellAndTechnique(smellResult, smellType, technique); 
		for (MethodDataSmelly methodNotSmelly : notSmellyMethodsForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(methodNotSmelly.getCommit());
			if (nextCommit != null) {
				if (!this.refactoringEvents.hasMethodRefactoringsInCommit(nextCommit.getId(), methodNotSmelly.getDiretorioDaClasse(), methodNotSmelly.getNomeClasse(), methodNotSmelly.getNomeMetodo(), targetTefactoringTypes)) {
					confusionMtrix.incTrueNegative();
					this.methodOutputFiles.writeNegativeToCsvFiles(methodNotSmelly);
				}
			} else {
				confusionMtrix.incTrueNegative();
				this.methodOutputFiles.writeNegativeToCsvFiles(methodNotSmelly);
			}	
		}
	}
	
}