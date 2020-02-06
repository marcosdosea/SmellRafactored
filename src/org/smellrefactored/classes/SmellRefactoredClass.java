package org.smellrefactored.classes;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
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
import org.smellrefactored.statistics.PredictionRound;

public class SmellRefactoredClass {

	final private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true; // Increases processing time.
	
	final private boolean IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT = true; // Increases processing time.
	
	private HashSet<String> getLongClassRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_CLASS.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_SUBCLASS.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_SUPERCLASS.toString());
		refactoringTypes.add(RefactoringType.CONVERT_ANONYMOUS_CLASS_TO_TYPE.toString());
		// // refactoringTypes.add(RefactoringType.INTRODUCE_POLYMORPHISM.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_ATTRIBUTE.toString());
		refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		return refactoringTypes;
	}

	private HashSet<String> getClassRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		// Add all smell refactoring groups here.
		refactoringTypes.addAll(this.getLongClassRefactoringTypes());
		return refactoringTypes;
	}

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private RefactoringEvents refactoringEvents;
	CommitClassSmell commitClassSmell;

	private ArrayList<String> smellCommitIds;
	CommitRange commitRange;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluation;
	OutputClassFileManager classOutputFiles;
	
	public SmellRefactoredClass(RefactoringEvents refactoringEvents, ArrayList<String> smellCommitIds, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) throws IOException {
		this.refactoringEvents = refactoringEvents;
		this.smellCommitIds = smellCommitIds;
		this.commitRange = commitRange;
		this.commitClassSmell = new CommitClassSmell(commitSmell);
		this.resultFileName = resultFileName;
		pmResultEvaluation = new CSVFile(resultFileName + "-evaluation-classes.csv", false);
		classOutputFiles = new OutputClassFileManager(this.commitRange, this.commitClassSmell.getTechniquesThresholds().keySet(), this.resultFileName);
	}
	
	public void getSmellRefactoredClasses() {
		try {
			logger.info("** Starting class analysis [[[");
			
			pmResultEvaluation.write("ANALYSIS OF PREDICTION OF REFACTORING IN METHODS");
			pmResultEvaluation.write("Performed on:", Instant.now().atOffset(ZoneOffset.UTC).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME).replace( "T" , " "));
			pmResultEvaluation.write("Repository url:", this.commitRange.getRepositoryUrl());
			pmResultEvaluation.write("Commit range:", this.commitRange.getInitialCommitId(), this.commitRange.getFinalCommitId());
			pmResultEvaluation.write("");
			pmResultEvaluation.write("REFACTORINGS");
			pmResultEvaluation.write("Total number of refactorings detected:", refactoringEvents.getAllMergedIntoMaster().size(), this.refactoringEvents.size());
			pmResultEvaluation.write("Refactorings related to classes:", this.refactoringEvents.countTypes(getClassRefactoringTypes()));
			for (String refactoringType: this.getClassRefactoringTypes()) {
				pmResultEvaluation.write("Number of " + refactoringType + ":", this.refactoringEvents.countType(refactoringType));
			}
			pmResultEvaluation.write("Refactorings on the initial commit were ignored");
			pmResultEvaluation.write("");
			pmResultEvaluation.write("SMELLS");
			pmResultEvaluation.write("Initial number of commits to begin analysis:", this.smellCommitIds.size());
			pmResultEvaluation.write("Threshold derivation techniques:", this.commitClassSmell.getTechniquesThresholds().size(), this.commitClassSmell.getTechniquesThresholds().keySet());
			pmResultEvaluation.write("Smells on the final commit were ignored");
			if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
				pmResultEvaluation.write("Unconfirmed predictions that were repeated following commit were ignored");
			}
			pmResultEvaluation.write("");
			
			classOutputFiles.writeHeaders();

			evaluateInDetailSmellChangeClass(ClassDataSmelly.LONG_CLASS, this.getLongClassRefactoringTypes(), this.smellCommitIds);
			
			classOutputFiles.close();

			logger.info("]]] Class analyzes completed.");
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeClass(String smellType, HashSet<String> targetTefactoringTypes, ArrayList<String> smellCommitIds) throws Exception {
		evaluateSmellChangeClass(smellCommitIds, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeClass(smellCommitIds, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}
	
	private void evaluateSmellChangeClass(ArrayList<String> smellCommitIds, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {
		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(typeSmell + " " + targetTefactoringTypes.toString(), this.commitClassSmell.getTechniquesThresholds().keySet());
		confusionMatrices.enableValidations(!IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT);
		// TP e FN
		computeTruePositiveAndFalseNegative(typeSmell, targetTefactoringTypes, confusionMatrices);
		for (String smellCommitId: smellCommitIds) {
			FilterSmellResult smellResult = this.commitClassSmell.getSmellsFromCommit(smellCommitId);
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
		 * 	Integer positivePredictionExpected = this.commitSmell.countClassSmellPredictionForTechniqueInCommit(smellResult, typeSmell, technique);
		 * 	confusionMatrices.setValidationPositivePrediction(technique, positivePredictionExpected);
		 * }
		*/

		pmResultEvaluation.write("");
		confusionMatrices.addField("Total number of refactorings", this.refactoringEvents.countTypes(targetTefactoringTypes));
		confusionMatrices.writeToCsvFile(pmResultEvaluation);
		// confusionMatrices.saveToCsvFile(this.resultFileName + "-confusionMatrices-class-" + typeSmell + "-" + targetTefactoringTypes.toString() + ".csv");
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
		RefactoringClassEvents refactoringClassEvents = new RefactoringClassEvents(this.refactoringEvents);
		HashSet<ClassDataSmelly> smellyClassesForSelectedTechniqueSmell = this.commitClassSmell.getSmellingClassesBySmellAndTechnique(smellResult, smellType, technique);
		for (ClassDataSmelly classSmelly : smellyClassesForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(classSmelly.getCommit());
			if (nextCommit != null) {
 				if (! refactoringClassEvents.hasRefactoringsInCommit(nextCommit.getId(), classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = this.commitClassSmell.hasClassSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse()); 
 					}
					if (!ignoreCurrentPrediction) {
						confusionMtrix.incFalsePositive();
						classOutputFiles.writeFalsePositive(classSmelly);
					}
				}
			} else {
				confusionMtrix.incFalsePositive();
				classOutputFiles.writeFalsePositive(classSmelly);
			}
		}
	}

	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		RefactoringClassEvents refactoringClassEvents = new RefactoringClassEvents(this.refactoringEvents);
		HashSet<ClassDataSmelly> notSmellyClassesForSelectedTechniqueSmell = this.commitClassSmell.getNotSmellingClassesBySmellAndTechnique(smellResult, smellType, technique); 
		for (ClassDataSmelly classNotSmelly : notSmellyClassesForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(classNotSmelly.getCommit());
			if (nextCommit != null) {
				if (!refactoringClassEvents.hasRefactoringsInCommit(nextCommit.getId(), classNotSmelly.getDiretorioDaClasse(), classNotSmelly.getNomeClasse(), targetTefactoringTypes)) {
					confusionMtrix.incTrueNegative();
					classOutputFiles.writeTrueNegative(classNotSmelly);
				}
			} else {
				confusionMtrix.incTrueNegative();
				classOutputFiles.writeTrueNegative(classNotSmelly);
			}	
		}
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
			ClassDataSmelly classSmellOrNotSmell = this.commitClassSmell.getSmellOrNotSmellCommitForClass(previousCommit.getId(), refactoring.getFileNameBefore(), refactoring.getClassName(), smellType); 
			if ( (classSmellOrNotSmell != null) && (classSmellOrNotSmell.getSmell() != null) && (!classSmellOrNotSmell.getSmell().isEmpty()) ) {
				predictionRound.setTrue(classSmellOrNotSmell.getListaTecnicas());
				predictionRound.setFalseAllExcept(classSmellOrNotSmell.getListaTecnicas());
				classOutputFiles.writeTruePositive(refactoring, classSmellOrNotSmell);
				if (predictionRound.isAnyoneOutOfRound()) {
					classOutputFiles.writeFalseNegative(refactoring, classSmellOrNotSmell);
				}
			} else {
				predictionRound.setFalseForAllOutOfRound();
				classOutputFiles.writeFalseNegative(refactoring, classSmellOrNotSmell);
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}
	
}
