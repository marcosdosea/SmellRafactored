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
import org.smellrefactored.statistics.ConfusionMatrixPredictors;
import org.smellrefactored.statistics.PredictionRound;

public class SmellRefactoredClass {

	final private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true;
	
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
	
		// TP e FN
		computeTruePositiveAndFalseNegative(listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		// FP
/// @TODO: Bug esse método computa alguns falsos positivos a mais
computeFalsePositive(commitInitial, listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		// TN
		computeTrueNegativeCommon(commitInitial, listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		computeTrueNegativeIndividual(commitInitial, listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		
		
		int realPositive = SmellRefactoredManager.countRealPositive(refactoringsCounter, targetTefactoringTypes);
		confusionMatrices.setValidationRealPositive(realPositive);

		pmResultEvaluationClass.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationClass);
	}

	
	private void computeFalsePositive(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring,
			String typeSmell, HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices)
			throws Exception {
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {
			if (classSmelly.getSmell().equals(typeSmell)) {
				PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setDefaultCondition(false);
				if (classSmelly.getSmell().equals(typeSmell)) {
					if (hasRefactoringsInNextCommit(classSmelly.getCommit(), classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse(), targetTefactoringTypes)) {
						predictionRound.setCondition(true);
						predictionRound.setNull(classSmelly.getListaTecnicas());
					} else {
						predictionRound.setCondition(false);
						predictionRound.setTrueIfOutOfRound(classSmelly.getListaTecnicas());
					}
				}
				predictionRound.setNullForAllOutOfRound();
				confusionMatrices.processPredictionRound(predictionRound);
			}
		}
	}
				
	
	private void computeTrueNegativeCommon(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring,
			String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (ClassDataSmelly classNotSmelly : commitInitial.getClassesNotSmelly()) {
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setDefaultCondition(false);
			for (RefactoringData refactoring : getRefactoringsForNextCommit(classNotSmelly.getCommit(), classNotSmelly.getDiretorioDaClasse(), classNotSmelly.getNomeClasse(), targetTefactoringTypes, true)) {
				predictionRound.setCondition(true);
				ClassDataSmelly classSmell = getPreviousSmellCommitForRefactoring(refactoring, smellType);
				if (classSmell != null) {
					predictionRound.setNullIfOutOfRound(classSmell.getListaTecnicas());
					break;
				}
			}
			if (!predictionRound.getCondition()) {
				predictionRound.setFalseForAllOutOfRound();
				writeNegativeToCsvFiles(classNotSmelly);
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}

	private void computeTrueNegativeIndividual(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setDefaultCondition(false);
				for (RefactoringData refactoring : getRefactoringsForNextCommit(classSmelly.getCommit(), classSmelly.getDiretorioDaClasse(), classSmelly.getNomeClasse(), targetTefactoringTypes, true)) {
					ClassDataSmelly classSmell = getPreviousSmellCommitForRefactoring(refactoring, smellType);
					if (classSmell != null) {
						predictionRound.setCondition(true);
						predictionRound.setNull(classSmell.getListaTecnicas());
						predictionRound.setNullAllExcept(classSmell.getListaTecnicas());
					// writeTruePositiveToCsvFiles(methodRefactored, methodSmell);
					}
				}
				predictionRound.setNullIfOutOfRound(classSmelly.getListaTecnicas());
				if (predictionRound.isAnyoneOutOfRound()) {
					writeNegativeToCsvFiles(classSmelly);
				}
				predictionRound.setFalseForAllOutOfRound();
				confusionMatrices.processPredictionRound(predictionRound);
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
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setCondition(true);
				ClassDataSmelly classSmell = getPreviousSmellCommitForRefactoring(refactoring, typeSmell);
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
				confusionMatrices.processPredictionRound(predictionRound);
			}
		}
	}
	
	
	public ClassDataSmelly getPreviousSmellCommitForRefactoring(RefactoringData refactoring, String smellType) {
		ClassDataSmelly result = null;
		FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(refactoring.getCommit());
		if (smellsPreviousCommit != null) {
			for (ClassDataSmelly classSmell : smellsPreviousCommit.getClassesSmell()) {
				if (classSmell.getSmell().equals(smellType)) {
					if ( (classSmell.getDiretorioDaClasse().equals(refactoring.getFileNameAfter())) 
							&& (classSmell.getNomeClasse().equals(refactoring.getNomeClasse())) ) {
						result = classSmell;
					}
				}
			}
		}
		return result;
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
	
	private CommitData getCommitById(String commitId) {
		CommitData result = null;
		for (CommitData commit : commitsMergedIntoMaster) {
			if (commit.getId().equals(commitId)) {
				result = commit;
			}
		}
		return result;
	}
	
	private CommitData getNextCommit(String commitId) {
		CommitData result = null;
		CommitData commit = getCommitById(commitId);
		if (commit!=null) {
			result = commit.getNext();
		}
		return result;
	}


	private boolean hasRefactoringsInNextCommit(String beforeCommitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringData> refactoringsForNextCommit = getRefactoringsForNextCommit(beforeCommitId, originalFilePath, originalClassName, targetTefactoringTypes, true);
		return (refactoringsForNextCommit.size()>0);		 
	}
	
	private ArrayList<RefactoringData> getRefactoringsForNextCommit(String baseCommitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes, boolean justNextCommit) {
		ArrayList<RefactoringData> result = new ArrayList<RefactoringData>(); 
		CommitData baseCommit = getCommitById(baseCommitId);
		CommitData nextCommit = baseCommit.getNext();
		String filePath = originalFilePath;
		String className = originalClassName;
		boolean renamedClass;
		Date dateCommitRenamed = null;
		do {
			renamedClass = false;
			String pathRenamedName = null;
			String classRenamedName = null;
			for (RefactoringData refactoring : listRefactoring) {
				if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
					continue;
				}
				if (refactoring.getCommitDate().before(baseCommit.getDate())) {
					continue;
				}
				if ( (justNextCommit) && (!refactoring.getCommit().equals(nextCommit.getId())) ) {
					continue;
				}
				if (!refactoring.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (!refactoring.getNomeClasse().equals(className)) {
					continue;
				}
				if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
					CommitData nextCommitForSmell = getNextCommit(baseCommitId);
					if ( (nextCommitForSmell != null) &&  (refactoring.getCommit().equals(nextCommitForSmell.getId())) ) {
						result.add(refactoring);
					}
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

	
	
}
