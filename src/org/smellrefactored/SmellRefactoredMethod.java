package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.statistics.ConfusionMatrixPredictors;
import org.smellrefactored.statistics.PredictionRound;

public class SmellRefactoredMethod {

	final private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true;
	
	private HashSet<String> getLongMethodRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		// refactoringTypes.add(RefactoringType.INLINE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MERGE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
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
		// refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
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
		// refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
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
		// refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		// refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.CHANGE_METHOD_SIGNATURE.toString());
		// // refactoringTypes.add(RefactoringType.CHANGE_RETURN_TYPE.toString());
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

	private HashSet<String> getMethodRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		// Original: refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		/// refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		/// refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
		return refactoringTypes;
	}
	
	
	private HashSet<String> getMethodRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		// smells refactoring...
		refactoringTypes.addAll(this.getLongMethodRefactoringTypes());
		refactoringTypes.addAll(this.getComplexMethodRefactoringTypes());
		refactoringTypes.addAll(this.getHighEfferentCouplingRefactoringTypes());
		refactoringTypes.addAll(this.getManyParametersRefactoringTypes());
		// and rename...
		refactoringTypes.addAll(this.getClassRenameRefactoringTypes());
		refactoringTypes.addAll(this.getMethodRenameRefactoringTypes());
		return refactoringTypes;
	}

	
	LinkedHashMap<String, Integer> refactoringsCounter = new LinkedHashMap<String, Integer>();  
	
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	ArrayList<RefactoringData> listRefactoring;
	private String initialCommit;
	ArrayList<CommitData> commitsMergedIntoMaster;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluationMethods;
	PersistenceMechanism pmResultSmellRefactoredMethods;
	PersistenceMechanism pmResultSmellRefactoredMethodsMessage;
	PersistenceMechanism pmResultSmellRefactoredMethodsMachineLearning;
	
	CommitSmell commitSmell;

	public SmellRefactoredMethod(ArrayList<RefactoringData> listRefactoring, String initialCommit, ArrayList<CommitData> commitsMergedIntoMaster, CommitSmell commitSmell, String resultFileName) {
		this.listRefactoring = listRefactoring;
		this.initialCommit = initialCommit;
		this.commitsMergedIntoMaster = commitsMergedIntoMaster;
		this.commitSmell = commitSmell;
		this.resultFileName = resultFileName;
		
		pmResultEvaluationMethods = new CSVFile(resultFileName + "-evaluation-methods.csv", false);
		pmResultSmellRefactoredMethods = new CSVFile(resultFileName + "-smellRefactored-methods.csv", false);
		pmResultSmellRefactoredMethodsMessage = new CSVFile(resultFileName + "-smellRefactored-methods-message.csv", false);
		pmResultSmellRefactoredMethodsMachineLearning = new CSVFile(resultFileName + "-smellRefactored-methods-machineLearning.csv", false);
	}
	
	public void getSmellRefactoredMethods() {
		try {
			int countRefactoringRelatedMethods = 0;
			int countRefactoringRelatedClassRenaming = 0;
			int countRefactoringRelatedMethodRenaming = 0;
			int countRefactoringRelatedLongMethod = 0;
			int countRefactoringRelatedComplexMethod = 0;
			int countRefactoringRelatedHighEfferentCoupling = 0;
			int countRefactoringRelatedManyParameters = 0;

			for (String refactoringType: getMethodRefactoringTypes()) {
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
						if (this.getMethodRefactoringTypes().contains(refactoring.getRefactoringType())) {
							if (refactoring.getNomeClasse() == null) {
								logger.error("NULL class name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getNomeClasse());
							}
							if (refactoring.getNomeClasse().contains("[") || refactoring.getNomeClasse().contains("]") || refactoring.getNomeClasse().contains(",") || refactoring.getNomeClasse().contains(" ")) {
								logger.error("DIRTY class name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getNomeClasse());
							}
							if (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
								if (refactoring.getNomeMetodo() == null) {
									logger.error("NULL method name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getNomeMetodo());
								}
								if (refactoring.getNomeMetodo().contains("[") || refactoring.getNomeMetodo().contains("]") || refactoring.getNomeMetodo().contains(",") || refactoring.getNomeMetodo().contains(" ")) {
									logger.error("DIRTY method name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getNomeMetodo());
								}
							}
							countRefactoringRelatedMethods++;
							refactoringsCounter.put(refactoring.getRefactoringType(), refactoringsCounter.getOrDefault(refactoring.getRefactoringType(), 0) +1);
						}
						if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedClassRenaming++;
						}
						if (this.getMethodRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedMethodRenaming++;
						}

						if (this.getLongMethodRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedLongMethod++;
						}
						if (this.getComplexMethodRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedComplexMethod++;
						}
						if (this.getHighEfferentCouplingRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedHighEfferentCoupling++;
						}
						if (this.getManyParametersRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedManyParameters++;
						}
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);
			
			pmResultEvaluationMethods.write("RELATORIO COMPLETO SISTEMA");
			
			pmResultEvaluationMethods.write("Numero de Refatoracoes em Metodos e Nao Metodos:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationMethods.write("Numero de Refatoracoes relacionadas a operacoes em Metodos:", countRefactoringRelatedMethods, getMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de Refatoracoes relacionadas a rename em Classes:", countRefactoringRelatedClassRenaming, getClassRenameRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de Refatoracoes relacionadas a rename em Metodos:", countRefactoringRelatedMethodRenaming, getMethodRenameRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.LONG_METHOD + ":", countRefactoringRelatedLongMethod, getLongMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.COMPLEX_METHOD + ":", countRefactoringRelatedComplexMethod, getComplexMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.HIGH_EFFERENT_COUPLING + ":", countRefactoringRelatedHighEfferentCoupling, getHighEfferentCouplingRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.MANY_PARAMETERS + ":", countRefactoringRelatedManyParameters, getManyParametersRefactoringTypes());

			for (String refactoringType: refactoringsCounter.keySet()) {
				pmResultEvaluationMethods.write("Numero de refatoracoes do tipo " + refactoringType + ":", refactoringsCounter.getOrDefault(refactoringType, 0));
			}
			

			FilterSmellResult smellsCommitInitial = this.commitSmell.obterSmellsCommit(initialCommit);
			pmResultEvaluationMethods.write("Numero Metodos Smell Commit Inicial:",
					smellsCommitInitial.getMetodosSmell().size());
			
			pmResultEvaluationMethods.write("Numero Metodos NOT Smell Commit Inicial:",
					smellsCommitInitial.getMetodosNotSmelly().size());

			
			pmResultSmellRefactoredMethodsMessage.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP",
					"Tecnicas", "Commit", "Refactoring", "Left Side", "Right Side", "Full Message");
			pmResultSmellRefactoredMethods.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas",
					"Commit", "Refactoring", "Left Side", "Right Side");
			pmResultSmellRefactoredMethodsMachineLearning.write("DesignRole", "LOC", "CC", "EC", "NOP", "isRefactoring", "Refactoring");

			evaluateInDetailSmellChangeOperation(MethodDataSmelly.LONG_METHOD           , this.getLongMethodRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.COMPLEX_METHOD        , this.getComplexMethodRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.HIGH_EFFERENT_COUPLING, this.getHighEfferentCouplingRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.MANY_PARAMETERS       , this.getManyParametersRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, FilterSmellResult smellsCommitInitial,
			ArrayList<RefactoringData> listRefactoringMergedIntoMaster) throws Exception {
		evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}

	
	private void evaluateSmellChangeOperation(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {

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
		for (String technique : this.commitSmell.getTechniquesThresholds().keySet()) {
			Integer positivePredictionExpected = countPositivePredictionForTechnique(commitInitial, typeSmell, technique);
			confusionMatrices.setValidationPositivePrediction(technique, positivePredictionExpected);
		}
		
		pmResultEvaluationMethods.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationMethods);
	
	}
	
	
	private void computeTrueNegativeIndividual(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setDefaultCondition(false);
				for (RefactoringData refactoring : getRefactoringsForNextCommit(methodSmelly.getCommit(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes, true)) {
					MethodDataSmelly methodSmell = getPreviousSmellCommitForMathodByRefactoring(refactoring, smellType);
					if (methodSmell != null) {
						predictionRound.setCondition(true);
						predictionRound.setNull(methodSmell.getListaTecnicas());
						predictionRound.setNullAllExcept(methodSmell.getListaTecnicas());
					// writeTruePositiveToCsvFiles(methodRefactored, methodSmell);
					}
				}
				predictionRound.setNullIfOutOfRound(methodSmelly.getListaTecnicas());
				if (predictionRound.isAnyoneOutOfRound()) {
					writeNegativeToCsvFiles(methodSmelly);
				}
				predictionRound.setFalseForAllOutOfRound();
				confusionMatrices.processPredictionRound(predictionRound);
			}
		}
	}
	

	
	private void computeTrueNegativeCommon(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring,
			String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (MethodDataSmelly methodNotSmelly : commitInitial.getMetodosNotSmelly()) {
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setDefaultCondition(false);
			for (RefactoringData refactoring : getRefactoringsForNextCommit(methodNotSmelly.getCommit(), methodNotSmelly.getDiretorioDaClasse(), methodNotSmelly.getNomeClasse(), methodNotSmelly.getNomeMetodo(), targetTefactoringTypes, true)) {
				predictionRound.setCondition(true);
				MethodDataSmelly methodSmell = getPreviousSmellCommitForMathodByRefactoring(refactoring, smellType);
				if (methodSmell != null) {
					predictionRound.setNullIfOutOfRound(methodSmell.getListaTecnicas());
					break;
				}
			}
			if (!predictionRound.getCondition()) {
				predictionRound.setFalseForAllOutOfRound();
				writeNegativeToCsvFiles(methodNotSmelly);
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
	}

	
	
	private void computeTruePositiveAndFalseNegative(ArrayList<RefactoringData> listRefactoring, String typeSmell,
			HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (RefactoringData refactoring : listRefactoring) {
			if ( (!this.getMethodRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
				continue;
			}
			if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setCondition(true);
				MethodDataSmelly classSmell = getPreviousSmellCommitForMathodByRefactoring(refactoring, typeSmell);
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
	
	private void computeFalsePositive(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(typeSmell)) {
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setDefaultCondition(false);
				if (methodSmelly.getSmell().equals(typeSmell)) {
					if (hasRefactoringsInNextCommit(methodSmelly.getCommit(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes)) {
						predictionRound.setCondition(true);
						predictionRound.setNull(methodSmelly.getListaTecnicas());
					} else {
						predictionRound.setCondition(false);
						predictionRound.setTrueIfOutOfRound(methodSmelly.getListaTecnicas());
					}
				}
				predictionRound.setNullForAllOutOfRound();
				confusionMatrices.processPredictionRound(predictionRound);
			}
		}
	}
				
	
	
	
	public MethodDataSmelly getPreviousSmellCommitForMathodByRefactoring(RefactoringData refactoring, String smellType) {
		MethodDataSmelly result = null;
		FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(refactoring.getCommit());
		if (smellsPreviousCommit != null) {
			for (MethodDataSmelly methodSmell : smellsPreviousCommit.getMetodosSmell()) {
				if (methodSmell.getSmell().equals(smellType)) {
					if ( (methodSmell.getDiretorioDaClasse().equals(refactoring.getFileNameAfter())) 
					        && methodSmell.getNomeClasse().equals(refactoring.getNomeClasse()) 
							&& methodSmell.getNomeMetodo().equals(refactoring.getNomeMetodo()) ) {				
						result = methodSmell;
					}
				}
			}
		}
		return result;
	}

	private void writeNegativeToCsvFiles(MethodDataSmelly methodSmellyBuscar) {
		pmResultSmellRefactoredMethodsMessage.write(methodSmellyBuscar.getNomeClasse(),
			methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell() != null ? methodSmellyBuscar.getSmell() : "",
			methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
			methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
			methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");
		pmResultSmellRefactoredMethods.write(methodSmellyBuscar.getNomeClasse(),
			methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell() != null ? methodSmellyBuscar.getSmell() : "",
			methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
			methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
			methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
		pmResultSmellRefactoredMethodsMachineLearning.write(methodSmellyBuscar.getClassDesignRole(),
			methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
			methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
			"false", "");
	}

	private void writeTruePositiveToCsvFiles(RefactoringData methodRefactored, MethodDataSmelly methodSmell) {
		pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
			methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
			methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
			methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
			methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
			methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
			methodRefactored.getRightSide(), methodRefactored.getFullMessage());
		pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
			methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
			methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
			methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
			methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
			methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
			methodRefactored.getRightSide());
		pmResultSmellRefactoredMethodsMachineLearning.write(methodRefactored.getClassDesignRole(),
			methodRefactored.getLinesOfCode(), methodRefactored.getComplexity(),
			methodRefactored.getEfferent(), methodRefactored.getNumberOfParameters(),
			"true", methodRefactored.getRefactoringType());
		
	}

	private void writeFalseNegativeToCsvFiles(RefactoringData methodRefactored, MethodDataSmelly methodNotSmell) {
		pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
				methodRefactored.getNomeMetodo(), methodRefactored.getSmell() != null ? methodRefactored.getSmell() : "",
				methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
				methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
				methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
				methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
				methodRefactored.getRightSide(), methodRefactored.getFullMessage());
		pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
				methodRefactored.getNomeMetodo(), methodRefactored.getSmell() != null ? methodRefactored.getSmell() : "",
				methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
				methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
				methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
				methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
				methodRefactored.getRightSide());
		pmResultSmellRefactoredMethodsMachineLearning.write(methodRefactored.getClassDesignRole(),
				methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
				methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
				"true", methodRefactored.getRefactoringType());
	}

	private void writeFalsePositiveToCsvFiles(RefactoringData refactoring) {
		pmResultSmellRefactoredMethodsMessage.write(refactoring.getNomeClasse(),
				refactoring.getNomeMetodo(), refactoring.getSmell() != null ? refactoring.getSmell() : "",
				refactoring.getLinesOfCode(), refactoring.getComplexity(),
				refactoring.getEfferent(), refactoring.getNumberOfParameters(),
				refactoring.getListaTecnicas(), refactoring.getCommit(), "", "", "", "");
			pmResultSmellRefactoredMethods.write(refactoring.getNomeClasse(),
				refactoring.getNomeMetodo(), refactoring.getSmell() != null ? refactoring.getSmell() : "",
				refactoring.getLinesOfCode(), refactoring.getComplexity(),
				refactoring.getEfferent(), refactoring.getNumberOfParameters(),
				refactoring.getListaTecnicas(), refactoring.getCommit(), "", "", "");
			pmResultSmellRefactoredMethodsMachineLearning.write(refactoring.getClassDesignRole(),
				refactoring.getLinesOfCode(), refactoring.getComplexity(),
				refactoring.getEfferent(), refactoring.getNumberOfParameters(),
				"false", "");
	}
	
	
	private static int countPositivePredictionForTechnique(FilterSmellResult commitInitial, String smellType, String technique) {
		int positivePrediction = 0; 
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().contains(smellType)) {
				if (methodSmelly.getListaTecnicas().contains(technique)) {
					positivePrediction++;
				}
			}
		}
		return positivePrediction;
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
		
	
	public static String extrairNomeMetodo(String rightSide) {
		int methodNameEnd = rightSide.indexOf("(");
		String partialMethodName = rightSide.substring(0, methodNameEnd);
		int methodNameBegin = partialMethodName.lastIndexOf(" ") + 1;
		return partialMethodName.substring(methodNameBegin);
	}

	
	

	private boolean wasMethodRefactored(String className, String methodName, RefactoringData refactoring) {
		// boolean isClassInvolvementBefore = refactoring.getInvolvedClassesBefore().contains(className);
		// boolean isClassInvolvementAfter  = refactoring.getInvolvedClassesAfter().contains(className);
		// boolean isClassInvolvement  = (isClassInvolvementBefore || isClassInvolvementAfter);
		boolean isClassSameName = refactoring.getNomeClasse().equals(className);

		// boolean isMethodLeftSide = refactoring.getLeftSide().contains(methodName);
		// boolean isMethodRightSide = refactoring.getRightSide().contains(methodName);
		// boolean isMethodSide = (isMethodLeftSide || isMethodRightSide);
		boolean isMethodSameName = refactoring.getNomeMetodo().equals(methodName);

		boolean isSameClass = (isClassSameName);
		boolean isSameMethod = (isMethodSameName);
		
		return (isSameClass && isSameMethod);
	}
	

	private boolean hasRefactoringsInNextCommit(String beforeCommitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringData> refactoringsForNextCommit = getRefactoringsForNextCommit(beforeCommitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes, true);
		return (refactoringsForNextCommit.size()>0);		 
	}
	
	private ArrayList<RefactoringData> getRefactoringsForNextCommit(String baseCommitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes, boolean justNextCommit) {
		ArrayList<RefactoringData> result = new ArrayList<RefactoringData>(); 
		CommitData baseCommit = getCommitById(baseCommitId);
		CommitData nextCommit = baseCommit.getNext();
		String filePath = originalFilePath;
		String className = originalClassName;
		String methodName = originalMethodName;
		boolean renamedMethod;
		Date dateCommitRenamed = null;
		do {
			renamedMethod = false;
			String pathRenamedName = null;
			String classRenamedName = null;
			String methodRenamedName = null;
			for (RefactoringData refactoring : listRefactoring) {
				if ( (!this.getMethodRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
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
				if (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) ) {
						renamedMethod = true;
						// Change it
						dateCommitRenamed = refactoring.getCommitDate();
						pathRenamedName = refactoring.getFileNameBefore();
						classRenamedName = refactoring.getRightSide();
						// Do not change
						methodRenamedName = methodName;
					}
				}
				if (!wasMethodRefactored(className, methodName, refactoring)) {
					continue;
				}
				if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
					CommitData nextCommitForSmell = getNextCommit(baseCommitId);
					if ( (nextCommitForSmell != null) &&  (refactoring.getCommit().equals(nextCommitForSmell.getId())) ) {
						result.add(refactoring);
					}
				}
				if (this.getMethodRenameRefactoringTypes().contains(refactoring.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) ) {
						renamedMethod = true;
						// Do not change
						pathRenamedName = filePath;
						classRenamedName = className;
						// Change it
						dateCommitRenamed = refactoring.getCommitDate();
						methodRenamedName = extrairNomeMetodo(refactoring.getRightSide());
					}
				}
			}
			if (renamedMethod) {
				filePath = pathRenamedName;
				className = classRenamedName;
				methodName = methodRenamedName;
			} else {
				dateCommitRenamed = null;
			}
		} while (renamedMethod);
		return (result);
	}
	
	
	
	
}