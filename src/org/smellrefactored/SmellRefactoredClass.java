package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmellRefactoredClass {

	private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true;
	
	private HashSet<String> getLongClassRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_CLASS.toString());
		// refactoringTypes.add(RefactoringType.EXTRACT_SUBCLASS.toString());
		// refactoringTypes.add(RefactoringType.EXTRACT_SUPERCLASS.toString());
		// refactoringTypes.add(RefactoringType.CONVERT_ANONYMOUS_CLASS_TO_TYPE.toString());
		// // refactoringTypes.add(RefactoringType.INTRODUCE_POLYMORPHISM.toString());
		// refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_ATTRIBUTE.toString());
		// refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		return refactoringTypes;
	}

	private HashSet<String> getClassRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		// refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
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

			
			evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, ClassDataSmelly.LONG_CLASS, this.getLongClassRefactoringTypes());
			if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (this.getLongClassRefactoringTypes().size() > 1) ) {
				for (String smellRefactoringType : this.getLongClassRefactoringTypes()) {
					evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, ClassDataSmelly.LONG_CLASS, new HashSet<String>(Arrays.asList(smellRefactoringType)));
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	
	private void evaluateSmellChangeClass(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {


		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(typeSmell + " " + targetTefactoringTypes.toString(), this.commitSmell.getTechniquesThresholds().keySet());
	
		// TP e FN
		computeTruePositiveAndFalseNegative(listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		// FP
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
				ClassDataSmelly classSmellyBuscar = classSmelly;
				boolean renamedClass = false;
				Date dateCommitRenamed = null;
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setDefaultCondition(false);
				do {
					renamedClass = false;
					String classRenamedName = null;
					for (RefactoringData refactoring : listRefactoring) {
						if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
							continue;
						}
						if (!refactoring.getNomeClasse().equals(classSmellyBuscar.getNomeClasse())) {
							continue;
						}
						if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
							ClassDataSmelly classSmell = obterSmellPreviousCommit(refactoring.getCommit(), classSmellyBuscar.getNomeClasse(), typeSmell);
							if (classSmell != null) { // && classSmellyBuscar.getCommit()
								predictionRound.setCondition(true);
								predictionRound.setNullIfOutOfRound(classSmell.getListaTecnicas());
								// writeTruePositiveToCsvFiles(refactoring, classSmell);
								// break;
							}
						}
						if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
							if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
								&& (dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) ) {
								renamedClass = true;
								dateCommitRenamed = refactoring.getCommitDate();
								classRenamedName = refactoring.getRightSide();
							}
						}
					}
					if (renamedClass) {
						classSmellyBuscar.setNomeClasse(classRenamedName);
					} else {
						dateCommitRenamed = null;
					}
				} while (renamedClass && predictionRound.isAnyoneOutOfRound() );
				
				if (predictionRound.isAnyoneOutOfRound()) {
				 	writeNegativeToCsvFiles(classSmellyBuscar);
				}
				predictionRound.setTrueIfOutOfRound(classSmellyBuscar.getListaTecnicas());
				predictionRound.setNullForAllOutOfRound();
				confusionMatrices.processPredictionRound(predictionRound);
				
			}
		}
	}

	
	

	
	
	private void computeTrueNegativeCommon(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring,
			String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (ClassDataSmelly classNotSmelly : commitInitial.getClassesNotSmelly()) {
			ClassDataSmelly classBuscar = classNotSmelly;
			boolean renamedClass = false;
			boolean refactoredClass = false;
			Date dateCommitRenamed = null;
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setDefaultCondition(false);
			do {
				renamedClass = false;
				String classRenamedName = null;
				for (RefactoringData refactoring : listRefactoring) {
					if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
						continue;
					}
					if (!refactoring.getNomeClasse().equals(classBuscar.getNomeClasse())) { 
						continue;
					}
					if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
						ClassDataSmelly classSmell = obterSmellPreviousCommit(refactoring.getCommit(), classBuscar.getNomeClasse(), smellType);
						if (classSmell != null) {
							logger.info("False Negative" + refactoring.getRefactoringType() + ": " + classBuscar.getNomeClasse() + " to " + classRenamedName);
							predictionRound.setCondition(true);
							predictionRound.setNullIfOutOfRound(classSmell.getListaTecnicas());
							// writeFalseNegativeToCsvFiles(refactoring, classSmell);
							refactoredClass = true;
							break;
						}
					}
					if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
						if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
								&& (dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) ) {
							renamedClass = true;
							dateCommitRenamed = refactoring.getCommitDate();
							classRenamedName = refactoring.getRightSide();
						}
					}
				}
				if (renamedClass) {
					classBuscar.setNomeClasse(classRenamedName);
				} else {
					dateCommitRenamed = null;
				}
			} while (renamedClass && !refactoredClass);
		
			if (!refactoredClass) {
				predictionRound.setFalseForAllOutOfRound();
				writeNegativeToCsvFiles(classBuscar);
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}
	private void computeTrueNegativeIndividual(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring,
			String typeSmell, HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices)
			throws Exception {
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {

			if (classSmelly.getSmell().equals(typeSmell)) {
				ClassDataSmelly classSmellyBuscar = classSmelly;
				boolean renamedClass = false;
				Date dateCommitRenamed = null;
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setDefaultCondition(false);
				do {
					renamedClass = false;
					String classRenamedName = null;
					for (RefactoringData refactoring : listRefactoring) {
						if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
							continue;
						}
						if (!refactoring.getNomeClasse().equals(classSmellyBuscar.getNomeClasse())) {
							continue;
						}
						if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
							ClassDataSmelly classSmell = obterSmellPreviousCommit(refactoring.getCommit(), classSmellyBuscar.getNomeClasse(), typeSmell);
							if (classSmell != null) {
								predictionRound.setCondition(true);
								predictionRound.setNull(classSmell.getListaTecnicas());
								predictionRound.setNullAllExcept(classSmell.getListaTecnicas());
								// writeTruePositiveToCsvFiles(refactoring, classSmell);
							}
						}
						if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
							if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
								&& ( dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) ) {
								renamedClass = true;
								dateCommitRenamed = refactoring.getCommitDate();
								classRenamedName = refactoring.getRightSide();
							}
						}
					}
					if (renamedClass) {
						classSmellyBuscar.setNomeClasse(classRenamedName);
					} else {
						dateCommitRenamed = null;
					}
				} while (renamedClass && predictionRound.isAnyoneInRound());
				
				predictionRound.setNullIfOutOfRound(classSmellyBuscar.getListaTecnicas());
				if (predictionRound.isAnyoneOutOfRound()) {
					writeNegativeToCsvFiles(classSmellyBuscar);
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
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setCondition(true);
			if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				ClassDataSmelly classSmell = obterSmellPreviousCommit(refactoring.getCommit(), refactoring.getNomeClasse(), typeSmell);
				if (classSmell != null) {
					predictionRound.setTrue(classSmell.getListaTecnicas());
					predictionRound.setFalseAllExcept(classSmell.getListaTecnicas());
					writeTruePositiveToCsvFiles(refactoring, classSmell);
				} else {
					predictionRound.setFalseForAllOutOfRound();
					writeFalsePositiveToCsvFiles(refactoring);
				}
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}
	
	
	public ClassDataSmelly obterSmellPreviousCommit(String commitId, String className, String smellType) {
		ClassDataSmelly result = null;
		FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(commitId);
		if (smellsPreviousCommit != null) {
			for (ClassDataSmelly classSmell : smellsPreviousCommit.getClassesSmell()) {
				if (classSmell.getSmell().equals(smellType)) {
					if (classSmell.getNomeClasse().equals(className)) {
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
	
}
