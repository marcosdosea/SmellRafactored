package org.smellrefactored.methods;

import java.io.IOException;
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
import org.smellrefactored.CommitData;
import org.smellrefactored.CommitRange;
import org.smellrefactored.CommitSmell;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.RefactoringEvents;
import org.smellrefactored.SmellRefactoredManager;
import org.smellrefactored.statistics.ConfusionMatrix;
import org.smellrefactored.statistics.ConfusionMatrixPredictors;

public class SmellRefactoredMethod {

	final private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true; // Increases processing time.
	
	final private boolean IGNORE_PREDICTION_FOR_DELAYED_REFACTORINGS = true; // Increases processing time.
	
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
	private OutputMethodFileManager methodOutputFiles;

	public SmellRefactoredMethod(RefactoringEvents refactoringEvents, ArrayList<String> smellCommitIds, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) throws IOException {
		this.refactoringEvents = refactoringEvents;
		this.smellCommitIds = smellCommitIds;
		this.commitRange = commitRange;
		this.commitMethodSmell = new CommitMethodSmell(commitSmell);
		this.resultFileName = resultFileName;
		pmResultEvaluation = new CSVFile(resultFileName + "-evaluation-methods.csv", false);
	}
	
	public void getSmellRefactoredMethods() throws Exception {
		logger.info("** Starting method analysis [[[");
		pmResultEvaluation.write("ANALYSIS OF PREDICTION OF REFACTORING IN METHODS");
		pmResultEvaluation.write("Performed on:", Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace( "T" , " "));
		pmResultEvaluation.write("Repository url:", this.commitRange.getRepositoryUrl());
		pmResultEvaluation.write("Commit range:", this.commitRange.getInitialCommitId(), this.commitRange.getFinalCommitId());
		pmResultEvaluation.write("Number of commits detected in the range:", this.commitRange.size());
		pmResultEvaluation.write("");
		pmResultEvaluation.write("REFACTORINGS");
		pmResultEvaluation.write("Total number of refactorings detected:", this.refactoringEvents.getAllMergedIntoMaster().size(), this.refactoringEvents.size());
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
		if (IGNORE_PREDICTION_FOR_DELAYED_REFACTORINGS) {
			pmResultEvaluation.write("Unconfirmed predictions that were repeated following commit were ignored");
		}
		pmResultEvaluation.write("");
		evaluateInDetailSmellChangeOperation(MethodDataSmelly.LONG_METHOD           , this.getLongMethodRefactoringTypes(), this.smellCommitIds);
		evaluateInDetailSmellChangeOperation(MethodDataSmelly.COMPLEX_METHOD        , this.getComplexMethodRefactoringTypes(), this.smellCommitIds);
		evaluateInDetailSmellChangeOperation(MethodDataSmelly.HIGH_EFFERENT_COUPLING, this.getHighEfferentCouplingRefactoringTypes(), this.smellCommitIds);
		evaluateInDetailSmellChangeOperation(MethodDataSmelly.MANY_PARAMETERS       , this.getManyParametersRefactoringTypes(), this.smellCommitIds);
		logger.info("]]] Mathod analyzes completed.");
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, ArrayList<String> smellCommitIds) throws Exception {
		evaluateSmellChangeOperation(smellCommitIds, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeOperation(smellCommitIds, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}
	
	private void evaluateSmellChangeOperation(ArrayList<String> smellCommitIds, String smellType, HashSet<String> targetTefactoringTypes) throws Exception {
		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(smellType + " " + targetTefactoringTypes.toString(), this.commitMethodSmell.getTechniquesThresholds().keySet());
		confusionMatrices.enableValidations(!IGNORE_PREDICTION_FOR_DELAYED_REFACTORINGS);
		for (String technique: this.commitMethodSmell.getTechniquesThresholds().keySet()) {
			methodOutputFiles = new OutputMethodFileManager(this.commitRange, smellType, technique, targetTefactoringTypes, this.resultFileName);
			methodOutputFiles.writeHeaders();
			ConfusionMatrix confusionMatrix = confusionMatrices.get(technique);
			// TP and FN
			computeTruePositiveAndFalseNegative(smellType, technique, targetTefactoringTypes, confusionMatrix);
			for (String smellCommitId: smellCommitIds) {
				FilterSmellResult smellResultForCommitSmellTechnique = this.commitMethodSmell.getSmellsFromCommitSmellTypeTechnique(smellCommitId, smellType, technique);
				if (smellCommitId.equals(smellCommitIds.get(0)) ) {
					int methodsWithSmellOnFirstCommitCount = (smellResultForCommitSmellTechnique.getMetodosSmell() == null ? 0 : smellResultForCommitSmellTechnique.getMetodosSmell().size());
					confusionMatrices.addField("Methods with the " + smellType + " smell in the first commit (" + technique + ")", methodsWithSmellOnFirstCommitCount);
					int methodsWithoutSmellOnFirstCommitCount = (smellResultForCommitSmellTechnique.getMetodosNotSmelly() == null ? 0 : smellResultForCommitSmellTechnique.getMetodosNotSmelly().size());
					confusionMatrices.addField("Methods without the " + smellType + " smell in the first commit (" + technique + ")", methodsWithoutSmellOnFirstCommitCount);
				}
				// FP and TN
				computeFalsePositiveBySmellAndTechnique(smellResultForCommitSmellTechnique, technique, smellType, targetTefactoringTypes, confusionMatrix);
				computeTrueNegativeBySmellAndTechnique(smellResultForCommitSmellTechnique, technique, smellType, targetTefactoringTypes, confusionMatrix);
			}
			methodOutputFiles.close();
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
	
	private void computeTruePositiveAndFalseNegative(String smellType, String technique,
			HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMatrix) throws Exception {
		for (RefactoringEvent refactoring : refactoringEvents.getAllMergedIntoMaster()) {
			if (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				continue;
			}
			CommitData previousCommit = this.commitRange.getPreviousCommit(refactoring.getCommitId());
			MethodDataSmelly methodSmellOrNotSmell = this.commitMethodSmell.getSmellOrNotSmellCommitForMethod(previousCommit.getId(), refactoring.getFileNameBefore(), refactoring.getClassName(), refactoring.getMethodName(), smellType, technique); 
			if ( (methodSmellOrNotSmell != null) && (methodSmellOrNotSmell.getSmell() != null) && (!methodSmellOrNotSmell.getSmell().isEmpty()) && (methodSmellOrNotSmell.getSmell().contains(smellType)) ) {
				confusionMatrix.incTruePositive();
				methodOutputFiles.writeTruePositive(refactoring, methodSmellOrNotSmell);
			} else {
				confusionMatrix.incFalseNegative();
				methodOutputFiles.writeFalseNegative(refactoring, methodSmellOrNotSmell);
			}
		}
	}
	
	private void computeFalsePositiveBySmellAndTechnique(FilterSmellResult smellResultForCommitSmellTechnique, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		RefactoringMethodEvents refactoringMethodEvents = new RefactoringMethodEvents(this.refactoringEvents);
		for (MethodDataSmelly methodSmelly : smellResultForCommitSmellTechnique.getMetodosSmell()) {
			CommitData nextCommit = this.commitRange.getNextCommit(methodSmelly.getCommit());
			if (nextCommit != null) {
 				if (!refactoringMethodEvents.hasRefactoringsInCommit(nextCommit.getId(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_PREDICTION_FOR_DELAYED_REFACTORINGS) {
 						if (SmellRefactoredManager.ANALYZE_FIRST_COMMIT_ONLY) {
 							ignoreCurrentPrediction = refactoringMethodEvents.hasRefactoringInThisCommitOrInFuture(nextCommit.getId(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes);
 						} else {
 							ignoreCurrentPrediction = this.commitMethodSmell.hasMethodSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo());  							
 						}
 					}
					if (ignoreCurrentPrediction) {
						this.methodOutputFiles.writeIgnoredFalsePositive(methodSmelly);
					} else {
						confusionMtrix.incFalsePositive();
						this.methodOutputFiles.writeFalsePositive(methodSmelly);
					}
				}
			} else {
				confusionMtrix.incFalsePositive();
				this.methodOutputFiles.writeFalsePositive(methodSmelly);
			}
		}
	}
	
	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult smellResultForCommitSmellTechnique, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		RefactoringMethodEvents refactoringMethodEvents = new RefactoringMethodEvents(this.refactoringEvents);
		for (MethodDataSmelly methodNotSmelly : smellResultForCommitSmellTechnique.getMetodosNotSmelly()) {
			CommitData nextCommit = this.commitRange.getNextCommit(methodNotSmelly.getCommit());
			if (nextCommit != null) {
				if (!refactoringMethodEvents.hasRefactoringsInCommit(nextCommit.getId(), methodNotSmelly.getDiretorioDaClasse(), methodNotSmelly.getNomeClasse(), methodNotSmelly.getNomeMetodo(), targetTefactoringTypes)) {
					confusionMtrix.incTrueNegative();
					this.methodOutputFiles.writeTrueNegative(methodNotSmelly);
				}
			} else {
				confusionMtrix.incTrueNegative();
				this.methodOutputFiles.writeTrueNegative(methodNotSmelly);
			}	
		}
	}
	
}
