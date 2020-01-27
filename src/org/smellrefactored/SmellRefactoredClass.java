package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
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

	private HashSet<String> getClassRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		/// refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
		return refactoringTypes;
	}

	private HashSet<String> getClassRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.addAll(this.getLongClassRefactoringTypes());
		refactoringTypes.addAll(this.getClassRenameRefactoringTypes());
		return refactoringTypes;
	}


	LinkedHashMap<String, Integer> refactoringsCounter = new LinkedHashMap<String, Integer>();  
	
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	ArrayList<RefactoringData> listRefactoring;
	private String initialCommit;
	ArrayList<CommitData> commitsMergedIntoMaster;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluationClass;
	PersistenceMechanism pmResultSmellRefactoredClasses;
	PersistenceMechanism pmResultSmellRefactoredClassesMessage;
	PersistenceMechanism pmResultSmellRefactoredClassesMachineLearning;
	
	CommitSmell commitSmell;

	
	public SmellRefactoredClass(ArrayList<RefactoringData> listRefactoring, String initialCommit, ArrayList<CommitData> commitsMergedIntoMaster, CommitSmell commitSmell, String resultFileName) {
		this.listRefactoring = listRefactoring;
		this.initialCommit = initialCommit;
		this.commitsMergedIntoMaster = commitsMergedIntoMaster;
		this.commitSmell = commitSmell;
		this.resultFileName = resultFileName;
		
		pmResultEvaluationClass = new CSVFile(resultFileName + "-evaluation-classes.csv", false);
		pmResultSmellRefactoredClasses = new CSVFile(resultFileName + "-smellRefactored-classes.csv", false);
		pmResultSmellRefactoredClassesMessage = new CSVFile(resultFileName + "-smellRefactored-classes-message.csv", false);
		pmResultSmellRefactoredClassesMachineLearning = new CSVFile(resultFileName + "-smellRefactored-classes-machineLearning.csv", false);
	}
	
	
	public void getSmellRefactoredClasses() {
		try {
			int countRefactoringRelatedClasses = 0;
			int countRefactoringRelatedClassesRenaming = 0;
			int countRefactoringRelatedLongClass = 0;

			
			for (String refactoringType: getClassRefactoringTypes()) {
				refactoringsCounter.put(refactoringType, 0);
			}
			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (this.getClassRefactoringTypes().contains(refactoring.getRefactoringType())) {
							if (refactoring.getNomeClasse() == null) {
								logger.error("NULL class name for " + refactoring.getRefactoringType() + " refactoring type");
							}
							if (refactoring.getNomeClasse().contains("[") || refactoring.getNomeClasse().contains("]") || refactoring.getNomeClasse().contains(",") || refactoring.getNomeClasse().contains(" ")) {
								logger.error("DIRTY class name for " + refactoring.getRefactoringType() + " refactoring type");
							}
							countRefactoringRelatedClasses++;
							refactoringsCounter.put(refactoring.getRefactoringType(), refactoringsCounter.getOrDefault(refactoring.getRefactoringType(), 0) +1);
						}
						if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedClassesRenaming++;
						}
						
						if (this.getLongClassRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedLongClass++;
						}
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);

		
			
			pmResultEvaluationClass.write("RELATORIO COMPLETO SISTEMA");
			
			pmResultEvaluationClass.write("Numero total de refatoracoes detectadas:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationClass.write("Numero de refatoracoes relacionadas a classes:", countRefactoringRelatedClasses, getClassRefactoringTypes());
			pmResultEvaluationClass.write("Numero de refatoracoes relacionadas a rename em classes:", countRefactoringRelatedClassesRenaming, getClassRenameRefactoringTypes());
			pmResultEvaluationClass.write("Numero de refatoracoes relacionadas a " + ClassDataSmelly.LONG_CLASS + ":", countRefactoringRelatedLongClass, getLongClassRefactoringTypes());

			for (String refactoringType: refactoringsCounter.keySet()) {
				pmResultEvaluationClass.write("Numero de refatoracoes do tipo " + refactoringType + ":", refactoringsCounter.getOrDefault(refactoringType, 0));
			}

			

			

			FilterSmellResult smellsCommitInitial = this.commitSmell.obterSmellsCommit(initialCommit);
			pmResultEvaluationClass.write("Numero Classes Smell Commit Inicial:",
					smellsCommitInitial.getClassesSmell().size());
			pmResultEvaluationClass.write("Numero Classes NOT Smell Commit Inicial:",
					smellsCommitInitial.getClassesNotSmelly().size());

			
			pmResultSmellRefactoredClassesMessage.write("Class", "Smell", "CLOC", "Tecnicas", "Commit", "Refactoring",
					"Left Side", "Right Side", "Full Message");
			pmResultSmellRefactoredClasses.write("Class", "Smell", "CLOC", "Tecnicas", "Commit", "Refactoring",
					"Left Side", "Right Side");
			pmResultSmellRefactoredClassesMachineLearning.write("DesignRole", "CLOC", "isRefactoring", "Refactoring");

			
			evaluateInDetailSmellChangeOperation(ClassDataSmelly.LONG_CLASS, this.getLongClassRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, FilterSmellResult smellsCommitInitial,
			ArrayList<RefactoringData> listRefactoringMergedIntoMaster) throws Exception {
		evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}

	
	
	private void evaluateSmellChangeClass(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {

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
			ArrayList<RefactoringData> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
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
			CommitData nextCommit = SmellRefactoredManager.getNextCommit(classSmelly.getCommit(), commitsMergedIntoMaster);
			if (nextCommit != null) {
 				if (!hasRefactoringsInCommit(nextCommit.getId(), classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = hasSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse()); 
 					}
					if (!ignoreCurrentPrediction) {
						confusionMtrix.incFalsePositive();
						writeNegativeToCsvFiles(classSmelly);
					}
				}
			} else {
				confusionMtrix.incFalsePositive();
				writeNegativeToCsvFiles(classSmelly);
			}
		}
	}

	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult commitInitial, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<ClassDataSmelly> notSmellyClassesForSelectedTechniqueSmell = getNotSmellingClassesBySmellAndTechnique(commitInitial, smellType, technique); 
		for (ClassDataSmelly classNotSmelly : notSmellyClassesForSelectedTechniqueSmell) {
			CommitData nextCommit = SmellRefactoredManager.getNextCommit(classNotSmelly.getCommit(), commitsMergedIntoMaster);
			if (nextCommit != null) {
				if (!hasRefactoringsInCommit(nextCommit.getId(), classNotSmelly.getDiretorioDaClasse(), classNotSmelly.getNomeClasse(), targetTefactoringTypes)) {
					confusionMtrix.incTrueNegative();
					writeNegativeToCsvFiles(classNotSmelly);
				}
			} else {
				confusionMtrix.incTrueNegative();
				writeNegativeToCsvFiles(classNotSmelly);
			}	
		}
	}
	
	
	
	private void computeTruePositiveAndFalseNegative(ArrayList<RefactoringData> listRefactoring, String typeSmell,
			HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (RefactoringData refactoring : listRefactoring) {
			if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
				continue;
			}
			if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				if ((ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!hasRefactoringsInCommit(refactoring.getCommit(), refactoring.getFileNameBefore(), refactoring.getNomeClasse(), targetTefactoringTypes))) {
					throw new Exception("Existing refactoring not found.");
				} 
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setCondition(true);
				ClassDataSmelly classSmell = getPreviousSmellCommitForClassByRefactoring(refactoring, typeSmell);
				if (classSmell != null) {
					predictionRound.setTrue(classSmell.getListaTecnicas());
					predictionRound.setFalseAllExcept(classSmell.getListaTecnicas());
					writeTruePositiveToCsvFiles(refactoring, classSmell);
					if (predictionRound.isAnyoneOutOfRound()) {
						writeFalseNegativeToCsvFiles(refactoring, classSmell);
					}
				} else {
					predictionRound.setFalseForAllOutOfRound();
					writeFalsePositiveToCsvFiles(refactoring);
				}
				predictionRound.setNullForAllOutOfRound();
				confusionMatrices.processPredictionRound(predictionRound);
			}
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
	
	public ClassDataSmelly getPreviousSmellCommitForClassByRefactoring(RefactoringData refactoring, String smellType) throws Exception {
		ClassDataSmelly result = null;
		CommitData commit = SmellRefactoredManager.getCommitById(refactoring.getCommit(), commitsMergedIntoMaster);
		if (commit == null ) {
			throw new Exception("Refactor Commit does not exist in the list of commits.");
		}
		CommitData previousCommit = commit.getPrevious();
		if (previousCommit != null) {
			FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsCommit(previousCommit.getId());
			if (smellsPreviousCommit != null) {
				for (ClassDataSmelly classSmell : smellsPreviousCommit.getClassesSmell()) {
					if (classSmell.getSmell().equals(smellType)) {
						if ( (classSmell.getDiretorioDaClasse().equals(refactoring.getFileNameBefore())) 
								&& classSmell.getNomeClasse().equals(refactoring.getNomeClasse()) ) {				
							result = classSmell;
						}
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
	
	private boolean hasRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringData> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}
	
	
	private ArrayList<RefactoringData> getRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringData> result = new ArrayList<RefactoringData>(); 
		String filePath = originalFilePath;
		String className = originalClassName;
		boolean renamedClass;
		Date dateCommitRenamed = null;
		do {
			renamedClass = false;
			String pathRenamedName = null;
			String classRenamedName = null;
			for (RefactoringData refactoring : listRefactoring) {
				if (!refactoring.getCommit().equals(commitId)) {
					continue;
				}
				if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
					continue;
				}
				if (!refactoring.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (!isSameClassRefactored(className, refactoring))  {
					continue;
				}
				if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
					result.add(refactoring);
				}
				if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) ) {
						renamedClass = true;
						dateCommitRenamed = refactoring.getCommitDate();
						pathRenamedName = refactoring.getFileNameAfter();
						classRenamedName = refactoring.getRightSide();
					}
				}
			}
			if (renamedClass) {
				filePath = pathRenamedName;
				className = classRenamedName;
			} else {
				dateCommitRenamed = null;
			}
		} while (renamedClass);
		return (result);
	}

	private boolean isSameClassRefactored(String className, RefactoringData refactoring) {
		// boolean isClassInvolvementBefore = refactoring.getInvolvedClassesBefore().contains(className);
		// boolean isClassInvolvementAfter  = refactoring.getInvolvedClassesAfter().contains(className);
		// boolean isClassInvolvement  = (isClassInvolvementBefore || isClassInvolvementAfter);
		boolean isClassSameName = ( (refactoring.getNomeClasse()!=null) && (refactoring.getNomeClasse().equals(className)) );
		return (isClassSameName);
	}
	

	private void writeTruePositiveToCsvFiles(RefactoringData refactoring, ClassDataSmelly classSmell) {
		pmResultSmellRefactoredClassesMessage.write(refactoring.getNomeClasse(),
			refactoring.getSmell(), classSmell.getLinesOfCode(),
			refactoring.getListaTecnicas(), refactoring.getCommit(),
			refactoring.getRefactoringType(), refactoring.getLeftSide(),
			refactoring.getRightSide(), refactoring.getFullMessage());
		pmResultSmellRefactoredClasses.write(refactoring.getNomeClasse(),
			refactoring.getSmell(), classSmell.getLinesOfCode(),
			refactoring.getListaTecnicas(), refactoring.getCommit(),
			refactoring.getRefactoringType(), refactoring.getLeftSide(),
			refactoring.getRightSide());
		pmResultSmellRefactoredClassesMachineLearning.write(refactoring.getClassDesignRole(),
			classSmell.getLinesOfCode(), "true", refactoring.getRefactoringType());
	}

	private void writeFalseNegativeToCsvFiles(RefactoringData refactoring, ClassDataSmelly classNotSmell) {
		pmResultSmellRefactoredClassesMessage.write(refactoring.getNomeClasse(),
			refactoring.getSmell() != null ? refactoring.getSmell() : "", classNotSmell.getLinesOfCode(),
			refactoring.getListaTecnicas(), refactoring.getCommit(),
			refactoring.getRefactoringType(), refactoring.getLeftSide(),
			refactoring.getRightSide(), refactoring.getFullMessage());
		pmResultSmellRefactoredClasses.write(refactoring.getNomeClasse(),
			refactoring.getSmell() != null ? refactoring.getSmell() : "", classNotSmell.getLinesOfCode(),
			refactoring.getListaTecnicas(), refactoring.getCommit(),
			refactoring.getRefactoringType(), refactoring.getLeftSide(),
			refactoring.getRightSide());
		pmResultSmellRefactoredClassesMachineLearning.write(refactoring.getClassDesignRole(),
			classNotSmell.getLinesOfCode(),
			"true", refactoring.getRefactoringType());
	}	

	private void writeNegativeToCsvFiles(ClassDataSmelly classBuscar) {
		pmResultSmellRefactoredClassesMessage.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
			classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
			"", "");
		pmResultSmellRefactoredClasses.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
			classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
			"");
		pmResultSmellRefactoredClassesMachineLearning.write(classBuscar.getClassDesignRole(),
			classBuscar.getLinesOfCode(), "false", "");
	}
	private void writeFalsePositiveToCsvFiles(RefactoringData refactoring) {
		pmResultSmellRefactoredClassesMessage.write(refactoring.getNomeClasse(), refactoring.getSmell(),
				refactoring.getLinesOfCode(), refactoring.getListaTecnicas(), refactoring.getCommit(), "", "",
			"", "");
		pmResultSmellRefactoredClasses.write(refactoring.getNomeClasse(), refactoring.getSmell(),
				refactoring.getLinesOfCode(), refactoring.getListaTecnicas(), refactoring.getCommit(), "", "",
			"");
		pmResultSmellRefactoredClassesMachineLearning.write(refactoring.getClassDesignRole(),
				refactoring.getLinesOfCode(), "false", "");
	}
	
	
}
