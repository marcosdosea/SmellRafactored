package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
	
	private HashSet<String> getMethodRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		// Add all smell refactor groups here.
		refactoringTypes.addAll(this.getLongMethodRefactoringTypes());
		refactoringTypes.addAll(this.getComplexMethodRefactoringTypes());
		refactoringTypes.addAll(this.getHighEfferentCouplingRefactoringTypes());
		refactoringTypes.addAll(this.getManyParametersRefactoringTypes());
		return refactoringTypes;
	}

	
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private RefactoringEvents refactoringEvents;
	CommitSmell commitSmell;

	private String initialCommit;
	CommitRange commitRange;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluationMethods;
	OutputFilesMethod methodOutputFiles;

	public SmellRefactoredMethod(RefactoringEvents refactoringEvents, String initialCommit, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) {
		this.refactoringEvents = refactoringEvents;
		this.initialCommit = initialCommit;
		this.commitRange = commitRange;
		this.commitSmell = commitSmell;
		this.resultFileName = resultFileName;
		pmResultEvaluationMethods = new CSVFile(resultFileName + "-evaluation-methods.csv", false);
		methodOutputFiles = new OutputFilesMethod(this.resultFileName);
	}
	
	public void getSmellRefactoredMethods() {
		try {
			ArrayList<RefactoringEvent> listRefactoringMergedIntoMaster = new ArrayList<RefactoringEvent>();
			for (RefactoringEvent refactoring : refactoringEvents.getAll()) {
				for (CommitData commit : this.commitRange.getCommitsMergedIntoMaster()) {
					if (refactoring.getCommitId().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (this.getMethodRefactoringTypes().contains(refactoring.getRefactoringType())) {
							if (refactoring.getClassName() == null) {
								logger.error("NULL class name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getClassName());
							}
							if (refactoring.getClassName().contains("[") || refactoring.getClassName().contains("]") || refactoring.getClassName().contains(",") || refactoring.getClassName().contains(" ")) {
								logger.error("DIRTY class name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getClassName());
							}
							if (refactoring.getMethodName() == null) {
								logger.error("NULL method name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getMethodName());
							}
							if (refactoring.getMethodName().contains("[") || refactoring.getMethodName().contains("]") || refactoring.getMethodName().contains(",") || refactoring.getMethodName().contains(" ")) {
								logger.error("DIRTY method name for " + refactoring.getRefactoringType() + " refactoring type: " + refactoring.getMethodName());
							}
						}
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);
			
			pmResultEvaluationMethods.write("RELATORIO COMPLETO SISTEMA");
			
			pmResultEvaluationMethods.write("Numero total de refatoracoes detectadas:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a operacoes em Metodos:", this.refactoringEvents.countTypes(getMethodRefactoringTypes()), getMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.LONG_METHOD + ":", this.refactoringEvents.countTypes(getLongMethodRefactoringTypes()), getLongMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.COMPLEX_METHOD + ":", this.refactoringEvents.countTypes(getComplexMethodRefactoringTypes()), getComplexMethodRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.HIGH_EFFERENT_COUPLING + ":", this.refactoringEvents.countTypes(getHighEfferentCouplingRefactoringTypes()), getHighEfferentCouplingRefactoringTypes());
			pmResultEvaluationMethods.write("Numero de refatoracoes relacionadas a " + MethodDataSmelly.MANY_PARAMETERS + ":", this.refactoringEvents.countTypes(getManyParametersRefactoringTypes()), getManyParametersRefactoringTypes());
			for (String refactoringType: this.getMethodRefactoringTypes()) {
				pmResultEvaluationMethods.write("Numero de refatoracoes do tipo " + refactoringType + ":", this.refactoringEvents.countType(refactoringType));
			}
			
			FilterSmellResult smellsCommitInitial = this.commitSmell.obterSmellsCommit(initialCommit);
			pmResultEvaluationMethods.write("Numero Metodos Smell Commit Inicial:",
					smellsCommitInitial.getMetodosSmell().size());
			
			pmResultEvaluationMethods.write("Numero Metodos NOT Smell Commit Inicial:",
					smellsCommitInitial.getMetodosNotSmelly().size());
			
			methodOutputFiles.writeHeaders();

			evaluateInDetailSmellChangeOperation(MethodDataSmelly.LONG_METHOD           , this.getLongMethodRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.COMPLEX_METHOD        , this.getComplexMethodRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.HIGH_EFFERENT_COUPLING, this.getHighEfferentCouplingRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.MANY_PARAMETERS       , this.getManyParametersRefactoringTypes(), smellsCommitInitial, listRefactoringMergedIntoMaster);
			
			methodOutputFiles.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, FilterSmellResult smellsCommitInitial,
			ArrayList<RefactoringEvent> listRefactoringMergedIntoMaster) throws Exception {
		evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}

	
	private void evaluateSmellChangeOperation(FilterSmellResult commitInitial,
			ArrayList<RefactoringEvent> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {

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
	
	private void computeTruePositiveAndFalseNegative(ArrayList<RefactoringEvent> listRefactoring, String smellType,
			HashSet<String> targetTefactoringTypes, ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (RefactoringEvent refactoring : listRefactoring) {
			if (!targetTefactoringTypes.contains(refactoring.getRefactoringType())) {
				continue;
			}
			if ((ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!this.refactoringEvents.hasMethodRefactoringsInCommit(refactoring.getCommitId(), refactoring.getFileNameBefore(), refactoring.getClassName(), refactoring.getMethodName(), targetTefactoringTypes))) {
				throw new Exception("Existing refactoring not found.");
			} 
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setCondition(true);
			CommitData previousCommit = this.commitRange.getPreviousCommit(refactoring.getCommitId());
			MethodDataSmelly methodSmell = getSmellCommitForMethod(previousCommit.getId(), refactoring.getFileNameBefore(), refactoring.getClassName(), refactoring.getMethodName(), smellType); 
			if (methodSmell != null) {
				predictionRound.setTrue(methodSmell.getListaTecnicas());
				predictionRound.setFalseAllExcept(methodSmell.getListaTecnicas());
				this.methodOutputFiles.writeTruePositiveToCsvFiles(refactoring, methodSmell);
				if (predictionRound.isAnyoneOutOfRound()) {
					this.methodOutputFiles.writeFalseNegativeToCsvFiles(refactoring, methodSmell);
				}
			} else {
				predictionRound.setFalseForAllOutOfRound();
				this.methodOutputFiles.writeFalsePositiveToCsvFiles(refactoring, new MethodDataSmelly());
			}
			predictionRound.setNullForAllOutOfRound();
			confusionMatrices.processPredictionRound(predictionRound);
		}
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
		HashSet<MethodDataSmelly> smellyMethodsForSelectedTechniqueSmell = getSmellingMethodsBySmellAndTechnique(commitInitial, smellType, technique);
		for (MethodDataSmelly methodSmelly : smellyMethodsForSelectedTechniqueSmell) {
			if ( (ENABLE_REDUNDANT_VERIFICATION_FOR_DEBUG) && (!hasSmellPredictionForTechniqueInCommit(methodSmelly.getCommit(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo())) ) {
				throw new Exception("Existing method smelly not found (" + methodSmelly.getCommit() + " | " + smellType + " | " + technique + " | " + methodSmelly.getDiretorioDaClasse() + " | " + methodSmelly.getNomeClasse() + " | " + methodSmelly.getNomeMetodo() + ".");
			} 
			CommitData nextCommit = this.commitRange.getNextCommit(methodSmelly.getCommit());
			if (nextCommit != null) {
 				if (!this.refactoringEvents.hasMethodRefactoringsInCommit(nextCommit.getId(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = hasSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo()); 
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

	
	public MethodDataSmelly getSmellCommitForMethod(String commitId, String filePath, String className, String methodName, String smellType) throws Exception {
		MethodDataSmelly result = null;
		FilterSmellResult smellsCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsCommit != null) {
			for (MethodDataSmelly methodSmell : smellsCommit.getMetodosSmell()) {
				if (methodSmell.getSmell().equals(smellType)) {
					if ( (methodSmell.getDiretorioDaClasse().equals(filePath)) 
							&& methodSmell.getNomeClasse().equals(className) 
							&& methodSmell.getNomeMetodo().equals(methodName) ) {				
						result = methodSmell;
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
	
}