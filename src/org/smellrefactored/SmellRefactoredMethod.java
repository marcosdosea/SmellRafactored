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
import org.smellrefactored.statistics.ConfusionMatrix;
import org.smellrefactored.statistics.ConfusionMatrixPredictors;
import org.smellrefactored.statistics.PredictionRound;

public class SmellRefactoredMethod {

	final private boolean ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL = true; // Increases processing time.
	
	final private boolean ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG = false; // Increases processing time.
	
	final private boolean IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT = true; // Increases processing time.
	
	
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
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
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
	CommitRange commitRange;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluationMethods;
	PersistenceMechanism pmResultSmellRefactoredMethods;
	PersistenceMechanism pmResultSmellRefactoredMethodsMessage;
	PersistenceMechanism pmResultSmellRefactoredMethodsMachineLearning;
	
	CommitSmell commitSmell;

	public SmellRefactoredMethod(ArrayList<RefactoringData> listRefactoring, String initialCommit, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) {
		this.listRefactoring = listRefactoring;
		this.initialCommit = initialCommit;
		this.commitRange = commitRange;
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
				for (CommitData commit : this.commitRange.getCommitsMergedIntoMaster()) {
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
			
			pmResultEvaluationMethods.write("Numero total de refatoracoes detectadas:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a operacoes em Metodos:", countRefactoringRelatedMethods, getMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a rename em Classes:", countRefactoringRelatedClassRenaming, getClassRenameRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a rename em Metodos:", countRefactoringRelatedMethodRenaming, getMethodRenameRefactoringTypes());
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
			pmResultSmellRefactoredMethodsMachineLearning.write(
					"commitId"
					, "filePath"
					, "className"
					, "methodName"
					, "DesignRole"
					, "LOC"
					, "CC"
					, "EC"
					, "NOP"
					, "isRefactoring"
					, "Refactoring"
					);

			evaluateInDetailSmellChangeOperation(MethodDataSmelly.LONG_METHOD           , this.getLongMethodRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.COMPLEX_METHOD        , this.getComplexMethodRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.HIGH_EFFERENT_COUPLING, this.getHighEfferentCouplingRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.MANY_PARAMETERS       , this.getManyParametersRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			
		} catch (Exception e) {
			e.printStackTrace();
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
		confusionMatrices.enableValidations(!IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT);
		
		// TP and FN
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
		
		pmResultEvaluationMethods.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationMethods);
	
	}
	
	
	private void computeTruePositiveAndFalseNegative(ArrayList<RefactoringData> listRefactoring, String typeSmell,
			HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (RefactoringData refactoring : listRefactoring) {
			if ( (!this.getMethodRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
				continue;
			}
			if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				if ((ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!hasRefactoringsInCommit(refactoring.getCommit(), refactoring.getFileNameBefore(), refactoring.getNomeClasse(), refactoring.getNomeMetodo(), targetTefactoringTypes))) {
					throw new Exception("Existing refactoring not found.");
				} 
				PredictionRound predictionRound = confusionMatrices.newRound();
				predictionRound.setCondition(true);
				MethodDataSmelly methodSmell = getPreviousSmellCommitForMathodByRefactoring(refactoring, typeSmell);
				if (methodSmell != null) {
					predictionRound.setTrue(methodSmell.getListaTecnicas());
					predictionRound.setFalseAllExcept(methodSmell.getListaTecnicas());
					writeTruePositiveToCsvFiles(refactoring, methodSmell);
					if (predictionRound.isAnyoneOutOfRound()) {
						writeFalseNegativeToCsvFiles(refactoring, methodSmell);
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
	
	private void computeFalsePositiveAndTrueNegativeForAllTechniques(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (String technique : confusionMatrices.getPredictores()) {
			computeFalsePositiveBySmellAndTechnique(commitInitial, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			computeTrueNegativeBySmellAndTechnique(commitInitial, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			}
	}
	
	private void computeFalsePositiveBySmellAndTechnique(FilterSmellResult commitInitial, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<MethodDataSmelly> smellyMethodsForSelectedTechniqueSmell = getSmellingMethodsBySmellAndTechnique(commitInitial, smellType, technique);
		for (MethodDataSmelly methodSmelly : smellyMethodsForSelectedTechniqueSmell) {
			if ( (ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!hasSmellPredictionForTechniqueInCommit(methodSmelly.getCommit(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo())) ) {
				throw new Exception("Existing method smelly not found (" + methodSmelly.getCommit() + " | " + smellType + " | " + technique + " | " + methodSmelly.getDiretorioDaClasse() + " | " + methodSmelly.getNomeClasse() + " | " + methodSmelly.getNomeMetodo() + ".");
			} 
			CommitData nextCommit = this.commitRange.getNextCommit(methodSmelly.getCommit());
			if (nextCommit != null) {
 				if (!hasRefactoringsInCommit(nextCommit.getId(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = hasSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo()); 
 					}
					if (!ignoreCurrentPrediction) {
						confusionMtrix.incFalsePositive();
						writeNegativeToCsvFiles(methodSmelly);
					}
				}
			} else {
				confusionMtrix.incFalsePositive();
				writeNegativeToCsvFiles(methodSmelly);
			}
		}
	}
	
	private boolean hasSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className, String methodName) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsInCommit != null) {
			for(MethodDataSmelly smellInCommit: smellsInCommit.getMetodosSmell() ) {
				if (smellInCommit.getSmell().contains(smellType)) {
					if ( (smellInCommit.getDiretorioDaClasse().equals(filePath))
							&& (smellInCommit.getNomeClasse().equals(className))
							&& (smellInCommit.getNomeMetodo().equals(methodName)) ) {
						if (smellInCommit.getListaTecnicas().contains(technique)) {
							result = true;
						}
					}
				}
			}
		}
		return (result);
	}
	
	
	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult commitInitial, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<MethodDataSmelly> notSmellyMethodsForSelectedTechniqueSmell = getNotSmellingMethodsBySmellAndTechnique(commitInitial, smellType, technique); 
		for (MethodDataSmelly methodNotSmelly : notSmellyMethodsForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(methodNotSmelly.getCommit());
			if (nextCommit != null) {
				if (!hasRefactoringsInCommit(nextCommit.getId(), methodNotSmelly.getDiretorioDaClasse(), methodNotSmelly.getNomeClasse(), methodNotSmelly.getNomeMetodo(), targetTefactoringTypes)) {
					confusionMtrix.incTrueNegative();
					writeNegativeToCsvFiles(methodNotSmelly);
				}
			} else {
				confusionMtrix.incTrueNegative();
				writeNegativeToCsvFiles(methodNotSmelly);
			}	
		}
	}
	
	private HashSet<MethodDataSmelly> getSmellingMethodsBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<MethodDataSmelly> result =  new HashSet<MethodDataSmelly>();
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				if (methodSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(methodSmelly);
				}
			}
		}
		return (result);
	}

	private HashSet<MethodDataSmelly> getNotSmellingMethodsBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<MethodDataSmelly> result =  new HashSet<MethodDataSmelly>();
		result.addAll(commitInitial.getMetodosNotSmelly());
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				if (!methodSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(methodSmelly);
				}
			}
		}
		return (result);
	}

	
	
	
	public MethodDataSmelly getPreviousSmellCommitForMathodByRefactoring(RefactoringData refactoring, String smellType) throws Exception {
		MethodDataSmelly result = null;
		CommitData commit = this.commitRange.getCommitById(refactoring.getCommit());
		if (commit == null ) {
			throw new Exception("Refactor Commit does not exist in the list of commits.");
		}
		CommitData previousCommit = commit.getPrevious();
		if (previousCommit != null) {
			FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsCommit(previousCommit.getId());
			if (smellsPreviousCommit != null) {
				for (MethodDataSmelly methodSmell : smellsPreviousCommit.getMetodosSmell()) {
					if (methodSmell.getSmell().equals(smellType)) {
						if ( (methodSmell.getDiretorioDaClasse().equals(refactoring.getFileNameBefore())) 
								&& methodSmell.getNomeClasse().equals(refactoring.getNomeClasse()) 
								&& methodSmell.getNomeMetodo().equals(refactoring.getNomeMetodo()) ) {				
							result = methodSmell;
						}
					}
				}
			}
		}
		return result;
	}

	private int countPositivePredictionForTechnique(FilterSmellResult commitInitial, String smellType, String technique) {
		HashSet<MethodDataSmelly> smellyMethods = getSmellingMethodsBySmellAndTechnique(commitInitial, smellType, technique);
		return (smellyMethods.size());
	}
	
	
	
		
	
	public static String extrairNomeMetodo(String rightSide) {
		int methodNameEnd = rightSide.indexOf("(");
		String partialMethodName = rightSide.substring(0, methodNameEnd);
		int methodNameBegin = partialMethodName.lastIndexOf(" ") + 1;
		return partialMethodName.substring(methodNameBegin);
	}

	
	private boolean isSameClassRefactored(String className, RefactoringData refactoring) {
		// boolean isClassInvolvementBefore = refactoring.getInvolvedClassesBefore().contains(className);
		// boolean isClassInvolvementAfter  = refactoring.getInvolvedClassesAfter().contains(className);
		// boolean isClassInvolvement  = (isClassInvolvementBefore || isClassInvolvementAfter);
		boolean isClassSameName = ( (refactoring.getNomeClasse()!=null) && (refactoring.getNomeClasse().equals(className)) );
		return (isClassSameName);
	}

	private boolean isSameMethodRefactored(String className, String methodName, RefactoringData refactoring) {
		boolean isClassSameName = isSameClassRefactored(className, refactoring);

		// boolean isMethodLeftSide = refactoring.getLeftSide().contains(methodName);
		// boolean isMethodRightSide = refactoring.getRightSide().contains(methodName);
		// boolean isMethodSide = (isMethodLeftSide || isMethodRightSide);
		boolean isMethodSameName = ( (refactoring.getNomeMetodo()!=null) && (refactoring.getNomeMetodo().equals(methodName)) );

		return (isClassSameName && isMethodSameName);
	}
	

	private boolean hasRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringData> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}

	private ArrayList<RefactoringData> getRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringData> result = new ArrayList<RefactoringData>(); 
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
				if (!refactoring.getCommit().equals(commitId)) {
					continue;
				}
				if ( (!this.getMethodRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) ) {
					continue;
				}
				if (!refactoring.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (!isSameClassRefactored(className, refactoring)) {
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
				if (!isSameMethodRefactored(className, methodName, refactoring)) {
					continue;
				}
				if (targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
					result.add(refactoring);
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
	
	
	private void writeTruePositiveToCsvFiles(RefactoringData refactoring, MethodDataSmelly methodSmell) throws Exception {
		pmResultSmellRefactoredMethodsMessage.write(refactoring.getNomeClasse(),
			refactoring.getNomeMetodo(), refactoring.getSmell(),
			methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
			methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
			refactoring.getListaTecnicas(), refactoring.getCommit(),
			refactoring.getRefactoringType(), refactoring.getLeftSide(),
			refactoring.getRightSide(), refactoring.getFullMessage());
		pmResultSmellRefactoredMethods.write(refactoring.getNomeClasse(),
			refactoring.getNomeMetodo(), refactoring.getSmell(),
			methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
			methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
			refactoring.getListaTecnicas(), refactoring.getCommit(),
			refactoring.getRefactoringType(), refactoring.getLeftSide(),
			refactoring.getRightSide());
		pmResultSmellRefactoredMethodsMachineLearning.write(
				this.commitRange.getPreviousCommit(refactoring.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getNomeClasse()
				, refactoring.getNomeMetodo()
				, refactoring.getClassDesignRole()
				, refactoring.getLinesOfCode()
				, refactoring.getComplexity()
				, refactoring.getEfferent()
				, refactoring.getNumberOfParameters()
				, "true"
				, refactoring.getRefactoringType()
				);
		
	}

	private void writeFalseNegativeToCsvFiles(RefactoringData refactoring, MethodDataSmelly methodNotSmell) throws Exception {
		pmResultSmellRefactoredMethodsMessage.write(refactoring.getNomeClasse(),
				refactoring.getNomeMetodo(), refactoring.getSmell() != null ? refactoring.getSmell() : "",
				methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
				methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
				refactoring.getListaTecnicas(), refactoring.getCommit(),
				refactoring.getRefactoringType(), refactoring.getLeftSide(),
				refactoring.getRightSide(), refactoring.getFullMessage());
		pmResultSmellRefactoredMethods.write(refactoring.getNomeClasse(),
				refactoring.getNomeMetodo(), refactoring.getSmell() != null ? refactoring.getSmell() : "",
				methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
				methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
				refactoring.getListaTecnicas(), refactoring.getCommit(),
				refactoring.getRefactoringType(), refactoring.getLeftSide(),
				refactoring.getRightSide());
		pmResultSmellRefactoredMethodsMachineLearning.write(
				this.commitRange.getPreviousCommit(refactoring.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getNomeClasse()
				, refactoring.getNomeMetodo()
				, refactoring.getClassDesignRole()
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, "true"
				, refactoring.getRefactoringType()
				);
	}

	private void writeFalsePositiveToCsvFiles(RefactoringData refactoring) throws Exception {
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
			pmResultSmellRefactoredMethodsMachineLearning.write(
					this.commitRange.getPreviousCommit(refactoring.getCommit())
					, refactoring.getFileNameAfter()
					, refactoring.getNomeClasse()
					, refactoring.getNomeMetodo()
					, refactoring.getClassDesignRole()
					, refactoring.getLinesOfCode()
					, refactoring.getComplexity()
					, refactoring.getEfferent()
					, refactoring.getNumberOfParameters()
					, "false"
					, ""
					);
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
		pmResultSmellRefactoredMethodsMachineLearning.write(
				methodSmellyBuscar.getCommit()
				, methodSmellyBuscar.getDiretorioDaClasse()
				, methodSmellyBuscar.getNomeClasse()
				, methodSmellyBuscar.getNomeMetodo()
				, methodSmellyBuscar.getClassDesignRole()
				, methodSmellyBuscar.getLinesOfCode()
				, methodSmellyBuscar.getComplexity()
				, methodSmellyBuscar.getEfferent()
				, methodSmellyBuscar.getNumberOfParameters()
				, "false"
				, ""
				);
	}

	
	
	
	
}