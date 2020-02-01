package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
	
	PersistenceMechanism pmResultEvaluationClass;
	OutputFilesClass classOutputFiles;
	
	public SmellRefactoredClass(RefactoringEvents refactoringEvents, ArrayList<String> smellCommitIds, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) {
		this.refactoringEvents = refactoringEvents;
		this.smellCommitIds = smellCommitIds;
		this.commitRange = commitRange;
		this.commitClassSmell = new CommitClassSmell(commitSmell);
		this.resultFileName = resultFileName;
		pmResultEvaluationClass = new CSVFile(resultFileName + "-evaluation-classes.csv", false);
		classOutputFiles = new OutputFilesClass(this.resultFileName);
	}
	
	public void getSmellRefactoredClasses() {
		try {
			logger.info("Starting class analysis...");
			ArrayList<RefactoringEvent> listRefactoringMergedIntoMaster = new ArrayList<RefactoringEvent>();
			for (RefactoringEvent refactoring : refactoringEvents.getAll()) {
				for (CommitData commit : commitRange.getCommitsMergedIntoMaster() ) {
					if (refactoring.getCommitId().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (this.getClassRefactoringTypes().contains(refactoring.getRefactoringType())) {
							if (refactoring.getClassName() == null) {
								logger.error("NULL class name for " + refactoring.getRefactoringType() + " refactoring type");
							}
							if (refactoring.getClassName().contains("[") || refactoring.getClassName().contains("]") || refactoring.getClassName().contains(",") || refactoring.getClassName().contains(" ")) {
								logger.error("DIRTY class name for " + refactoring.getRefactoringType() + " refactoring type");
							}
						}
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);
			
			pmResultEvaluationClass.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluationClass.write("Numero total de refatoracoes detectadas:", listRefactoringMergedIntoMaster.size(), this.refactoringEvents.size());
			pmResultEvaluationClass.write("Numero de refatoracoes relacionadas a classes:", this.refactoringEvents.countTypes(getClassRefactoringTypes()), getClassRefactoringTypes());
			pmResultEvaluationClass.write("Numero de refatoracoes relacionadas a " + ClassDataSmelly.LONG_CLASS + ":", this.refactoringEvents.countTypes(getLongClassRefactoringTypes()), getLongClassRefactoringTypes());
			for (String refactoringType: this.getClassRefactoringTypes()) {
				pmResultEvaluationClass.write("Numero de refatoracoes do tipo " + refactoringType + ":", this.refactoringEvents.countType(refactoringType));
			}
			pmResultEvaluationClass.write("Numero de commits a analisar smells:", this.smellCommitIds.size());
			
			classOutputFiles.writeHeaders();

			evaluateInDetailSmellChangeClass(ClassDataSmelly.LONG_CLASS, this.getLongClassRefactoringTypes(), this.smellCommitIds, listRefactoringMergedIntoMaster);
			
			classOutputFiles.close();

			logger.info("Class analyzes completed.");
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeClass(String smellType, HashSet<String> targetTefactoringTypes, ArrayList<String> smellCommitIds,
			ArrayList<RefactoringEvent> listRefactoringMergedIntoMaster) throws Exception {
		evaluateSmellChangeClass(smellCommitIds, listRefactoringMergedIntoMaster, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeClass(smellCommitIds, listRefactoringMergedIntoMaster, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}
	
	private void evaluateSmellChangeClass(ArrayList<String> smellCommitIds, ArrayList<RefactoringEvent> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {

		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(typeSmell + " " + targetTefactoringTypes.toString(), this.commitClassSmell.getTechniquesThresholds().keySet());
		confusionMatrices.enableValidations(!IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT);
	
		// TP e FN
		computeTruePositiveAndFalseNegative(listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		for (String smellCommitId: smellCommitIds) {
			FilterSmellResult smellResult = this.commitClassSmell.obterSmellsCommit(smellCommitId);
			// FP and TN
			computeFalsePositiveAndTrueNegativeForAllTechniques(smellResult, listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
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

		pmResultEvaluationClass.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationClass);
	}
	
	private void computeFalsePositiveAndTrueNegativeForAllTechniques(FilterSmellResult smellResult,
			ArrayList<RefactoringEvent> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (String technique : confusionMatrices.getPredictores()) {
			computeFalsePositiveBySmellAndTechnique(smellResult, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			computeTrueNegativeBySmellAndTechnique(smellResult, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			}
	}
	
	private void computeFalsePositiveBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<ClassDataSmelly> smellyClassesForSelectedTechniqueSmell = this.commitClassSmell.getSmellingClassesBySmellAndTechnique(smellResult, smellType, technique);
		for (ClassDataSmelly classSmelly : smellyClassesForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(classSmelly.getCommit());
			if (nextCommit != null) {
 				if (! this.refactoringEvents.hasClassRefactoringsInCommit(nextCommit.getId(), classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = this.commitClassSmell.hasClassSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse()); 
 					}
					if (!ignoreCurrentPrediction) {
						confusionMtrix.incFalsePositive();
						classOutputFiles.writeNegativeToCsvFiles(classSmelly);
					}
				}
			} else {
				confusionMtrix.incFalsePositive();
				classOutputFiles.writeNegativeToCsvFiles(classSmelly);
			}
		}
	}

	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<ClassDataSmelly> notSmellyClassesForSelectedTechniqueSmell = this.commitClassSmell.getNotSmellingClassesBySmellAndTechnique(smellResult, smellType, technique); 
		for (ClassDataSmelly classNotSmelly : notSmellyClassesForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(classNotSmelly.getCommit());
			if (nextCommit != null) {
				if (!this.refactoringEvents.hasClassRefactoringsInCommit(nextCommit.getId(), classNotSmelly.getDiretorioDaClasse(), classNotSmelly.getNomeClasse(), targetTefactoringTypes)) {
					confusionMtrix.incTrueNegative();
					classOutputFiles.writeNegativeToCsvFiles(classNotSmelly);
				}
			} else {
				confusionMtrix.incTrueNegative();
				classOutputFiles.writeNegativeToCsvFiles(classNotSmelly);
			}	
		}
	}
	
	private void computeTruePositiveAndFalseNegative(ArrayList<RefactoringEvent> listRefactoring, String smellType,
			HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (RefactoringEvent refactoring : listRefactoring) {
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
				classOutputFiles.writeTruePositiveToCsvFiles(refactoring, classSmellOrNotSmell);
				if (predictionRound.isAnyoneOutOfRound()) {
					classOutputFiles.writeFalseNegativeToCsvFiles(refactoring, classSmellOrNotSmell);
				}
			} else {
				predictionRound.setFalseForAllOutOfRound();
				classOutputFiles.writeFalsePositiveToCsvFiles(refactoring, classSmellOrNotSmell);
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}
	
	
	
}
