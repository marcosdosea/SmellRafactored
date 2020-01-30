package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;

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
	
	final private boolean ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG = false; // Increases processing time.
	
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
		// Add all smell refactor groups here.
		refactoringTypes.addAll(this.getLongClassRefactoringTypes());
		return refactoringTypes;
	}


	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private RefactoringEvents refactoringEvents;
	CommitSmell commitSmell;

	private String initialCommit;
	CommitRange commitRange;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluationClass;
	OutputFilesClass classOutputFiles;
	
	public SmellRefactoredClass(RefactoringEvents refactoringEvents, String initialCommit, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) {
		this.refactoringEvents = refactoringEvents;
		this.initialCommit = initialCommit;
		this.commitRange = commitRange;
		this.commitSmell = commitSmell;
		this.resultFileName = resultFileName;
		pmResultEvaluationClass = new CSVFile(resultFileName + "-evaluation-classes.csv", false);
		classOutputFiles = new OutputFilesClass(this.resultFileName);
	}
	
	public void getSmellRefactoredClasses() {
		try {
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

			FilterSmellResult smellsCommitInitial = this.commitSmell.obterSmellsCommit(initialCommit);
			pmResultEvaluationClass.write("Numero Classes Smell Commit Inicial:",
					smellsCommitInitial.getClassesSmell().size());
			pmResultEvaluationClass.write("Numero Classes NOT Smell Commit Inicial:",
					smellsCommitInitial.getClassesNotSmelly().size());

			classOutputFiles.writeHeaders();

			evaluateInDetailSmellChangeOperation(ClassDataSmelly.LONG_CLASS, this.getLongClassRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			
			classOutputFiles.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, FilterSmellResult smellsCommitInitial,
			ArrayList<RefactoringEvent> listRefactoringMergedIntoMaster) throws Exception {
		evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}
	
	private void evaluateSmellChangeClass(FilterSmellResult commitInitial, ArrayList<RefactoringEvent> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {

		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(typeSmell + " " + targetTefactoringTypes.toString(), this.commitSmell.getTechniquesThresholds().keySet());
		confusionMatrices.enableValidations(!IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT);
	
		// TP e FN
		computeTruePositiveAndFalseNegative(listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		// FP and TN
		computeFalsePositiveAndTrueNegativeForAllTechniques(commitInitial, listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		
		/* 
	     * Warning: The validation of the confusion matrix by the total of positive predictions 
	     *          does not apply to the analysis of smells only from the initial commit.
	     * 
		 * int realPositive = SmellRefactoredManager.countRealPositive(refactoringsCounter, targetTefactoringTypes);
		 * confusionMatrices.setValidationRealPositive(realPositive);
		 * for (String technique : this.commitSmell.getTechniquesThresholds().keySet()) {
		 * 	Integer positivePredictionExpected = countPositivePredictionForTechnique(commitInitial, typeSmell, technique);
		 * 	confusionMatrices.setValidationPositivePrediction(technique, positivePredictionExpected);
		 * }
		*/

		pmResultEvaluationClass.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationClass);
	}
	
	private void computeFalsePositiveAndTrueNegativeForAllTechniques(FilterSmellResult commitInitial,
			ArrayList<RefactoringEvent> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (String technique : confusionMatrices.getPredictores()) {
			computeFalsePositiveBySmellAndTechnique(commitInitial, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			computeTrueNegativeBySmellAndTechnique(commitInitial, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			}
	}
	
	private void computeFalsePositiveBySmellAndTechnique(FilterSmellResult commitInitial, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<ClassDataSmelly> smellyClassesForSelectedTechniqueSmell = getSmellingClassesBySmellAndTechnique(commitInitial, smellType, technique);
		for (ClassDataSmelly classSmelly : smellyClassesForSelectedTechniqueSmell) {
			if ( (ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!hasSmellPredictionForTechniqueInCommit(classSmelly.getCommit(), smellType, technique, classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse())) ) {
				throw new Exception("Existing class smelly not found (" + classSmelly.getCommit() + " | " + smellType + " | " + technique + " | " + classSmelly.getDiretorioDaClasse() + " | " + classSmelly.getNomeClasse() + ".");
			} 
			CommitData nextCommit = this.commitRange.getNextCommit(classSmelly.getCommit());
			if (nextCommit != null) {
 				if (! this.refactoringEvents.hasClassRefactoringsInCommit(nextCommit.getId(), classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = hasSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse()); 
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

	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult commitInitial, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<ClassDataSmelly> notSmellyClassesForSelectedTechniqueSmell = getNotSmellingClassesBySmellAndTechnique(commitInitial, smellType, technique); 
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
			if ((ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!this.refactoringEvents.hasClassRefactoringsInCommit(refactoring.getCommitId(), refactoring.getFileNameBefore(), refactoring.getClassName(), targetTefactoringTypes))) {
				throw new Exception("Existing refactoring not found.");
			} 
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setCondition(true);
			CommitData previousCommit = this.commitRange.getPreviousCommit(refactoring.getCommitId());
			ClassDataSmelly classSmell = getSmellCommitForClass(previousCommit.getId(), refactoring.getFileNameBefore(), refactoring.getClassName(), smellType); 
			if (classSmell != null) {
				predictionRound.setTrue(classSmell.getListaTecnicas());
				predictionRound.setFalseAllExcept(classSmell.getListaTecnicas());
				classOutputFiles.writeTruePositiveToCsvFiles(refactoring, classSmell);
				if (predictionRound.isAnyoneOutOfRound()) {
					classOutputFiles.writeFalseNegativeToCsvFiles(refactoring, classSmell);
				}
			} else {
				predictionRound.setFalseForAllOutOfRound();
				classOutputFiles.writeFalsePositiveToCsvFiles(refactoring, new ClassDataSmelly());
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}
	
	private HashSet<ClassDataSmelly> getSmellingClassesBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				if (classSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(classSmelly);
				}
			}
		}
		return (result);
	}

	private int countPositivePredictionForTechnique(FilterSmellResult commitInitial, String smellType, String technique) {
		HashSet<ClassDataSmelly> smellyClasses = getSmellingClassesBySmellAndTechnique(commitInitial, smellType, technique);
		return (smellyClasses.size());
	}
	
	public ClassDataSmelly getSmellCommitForClass(String commitId, String filePath, String className, String smellType) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellsCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsCommit != null) {
			for (ClassDataSmelly classSmell : smellsCommit.getClassesSmell()) {
				if (classSmell.getSmell().equals(smellType)) {
					if ( (classSmell.getDiretorioDaClasse().equals(filePath)) 
							&& classSmell.getNomeClasse().equals(className) ) {				
						result = classSmell;
					}
				}
			}
		}
		return result;
	}
	
	private HashSet<ClassDataSmelly> getNotSmellingClassesBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		result.addAll(commitInitial.getClassesNotSmelly());
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				if (!classSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(classSmelly);
				}
			}
		}
		return (result);
	}
	
	private boolean hasSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsInCommit != null) {
			for(ClassDataSmelly smellInCommit: smellsInCommit.getClassesSmell() ) {
				if (smellInCommit.getSmell().contains(smellType)) {
					if ( (smellInCommit.getDiretorioDaClasse().equals(filePath))
							&& (smellInCommit.getNomeClasse().equals(className)) ) {
						if (smellInCommit.getListaTecnicas().contains(technique)) {
							result = true;
						}
					}
				}
			}
		}
		return (result);
	}

}
