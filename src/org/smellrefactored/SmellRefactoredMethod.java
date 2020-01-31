package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

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
	
	final private boolean IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT = true; // Increases processing time.
	
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
	private PersistenceMechanism pmResultEvaluationMethods;
	private OutputFilesMethod methodOutputFiles;

	public SmellRefactoredMethod(RefactoringEvents refactoringEvents, ArrayList<String> smellCommitIds, CommitRange commitRange, CommitSmell commitSmell, String resultFileName) {
		this.refactoringEvents = refactoringEvents;
		this.smellCommitIds = smellCommitIds;
		this.commitRange = commitRange;
		this.commitMethodSmell = new CommitMethodSmell(commitSmell);
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
			pmResultEvaluationMethods.write("Numero de commits a analisar smells:", this.smellCommitIds.size());
			
			methodOutputFiles.writeHeaders();

			evaluateInDetailSmellChangeOperation(MethodDataSmelly.LONG_METHOD           , this.getLongMethodRefactoringTypes(), this.smellCommitIds, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.COMPLEX_METHOD        , this.getComplexMethodRefactoringTypes(), this.smellCommitIds, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.HIGH_EFFERENT_COUPLING, this.getHighEfferentCouplingRefactoringTypes(), this.smellCommitIds, listRefactoringMergedIntoMaster);
			evaluateInDetailSmellChangeOperation(MethodDataSmelly.MANY_PARAMETERS       , this.getManyParametersRefactoringTypes(), this.smellCommitIds, listRefactoringMergedIntoMaster);
			
			methodOutputFiles.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void evaluateInDetailSmellChangeOperation(String smellType, HashSet<String> targetTefactoringTypes, ArrayList<String> smellCommitIds,
			ArrayList<RefactoringEvent> listRefactoringMergedIntoMaster) throws Exception {
		evaluateSmellChangeOperation(smellCommitIds, listRefactoringMergedIntoMaster, smellType, targetTefactoringTypes);
		if ( (ANALYZE_EACH_REFACTORING_TYPE_BY_SMELL) && (targetTefactoringTypes.size() > 1) ) {
			for (String targetTefactoringType : targetTefactoringTypes) {
				evaluateSmellChangeOperation(smellCommitIds, listRefactoringMergedIntoMaster, smellType, new HashSet<String>(Arrays.asList(targetTefactoringType)));
			}
		}
	}
	
	private void evaluateSmellChangeOperation(ArrayList<String> smellCommitIds,
			ArrayList<RefactoringEvent> listRefactoring, String typeSmell, HashSet<String> targetTefactoringTypes) throws Exception {

		ConfusionMatrixPredictors confusionMatrices = new ConfusionMatrixPredictors(typeSmell + " " + targetTefactoringTypes.toString(), this.commitMethodSmell.getTechniquesThresholds().keySet());
		confusionMatrices.enableValidations(!IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT);
		
		// TP and FN
		computeTruePositiveAndFalseNegative(listRefactoring, typeSmell, targetTefactoringTypes, confusionMatrices);
		for (String smellCommitId: smellCommitIds) {
			FilterSmellResult smellResult = this.commitMethodSmell.obterSmellsCommit(smellCommitId);
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
		 * 	Integer positivePredictionExpected = this.commitSmell.countMethodSmellPredictionForTechniqueInCommit(smellResult, typeSmell, technique);
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
			PredictionRound predictionRound = confusionMatrices.newRound();
			predictionRound.setCondition(true);
			CommitData previousCommit = this.commitRange.getPreviousCommit(refactoring.getCommitId());
			MethodDataSmelly methodSmell = this.commitMethodSmell.getSmellCommitForMethod(previousCommit.getId(), refactoring.getFileNameBefore(), refactoring.getClassName(), refactoring.getMethodName(), smellType); 
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
	
	private void computeFalsePositiveAndTrueNegativeForAllTechniques(FilterSmellResult smellResult,
			ArrayList<RefactoringEvent> listRefactoring, String smellType, HashSet<String> targetTefactoringTypes,
			ConfusionMatrixPredictors confusionMatrices) throws Exception {
		for (String technique : confusionMatrices.getPredictores()) {
			computeFalsePositiveBySmellAndTechnique(smellResult, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			computeTrueNegativeBySmellAndTechnique(smellResult, technique, smellType, targetTefactoringTypes, confusionMatrices.get(technique));
			}
	}
	
	private void computeFalsePositiveBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<MethodDataSmelly> smellyMethodsForSelectedTechniqueSmell = this.commitMethodSmell.getSmellingMethodsBySmellAndTechnique(smellResult, smellType, technique);
		for (MethodDataSmelly methodSmelly : smellyMethodsForSelectedTechniqueSmell) {
			CommitData nextCommit = this.commitRange.getNextCommit(methodSmelly.getCommit());
			if (nextCommit != null) {
 				if (!this.refactoringEvents.hasMethodRefactoringsInCommit(nextCommit.getId(), methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), targetTefactoringTypes)) {
 					boolean ignoreCurrentPrediction = false;
 					if (IGNORE_REPEATED_PREDICION_ON_NEXT_COMMIT) {
 						ignoreCurrentPrediction = this.commitMethodSmell.hasMethodSmellPredictionForTechniqueInCommit(nextCommit.getId(), smellType, technique, methodSmelly.getDiretorioDaClasse(), methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo()); 
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
	
	private void computeTrueNegativeBySmellAndTechnique(FilterSmellResult smellResult, String technique, String smellType, HashSet<String> targetTefactoringTypes, ConfusionMatrix confusionMtrix) throws Exception {
		HashSet<MethodDataSmelly> notSmellyMethodsForSelectedTechniqueSmell = this.commitMethodSmell.getNotSmellingMethodsBySmellAndTechnique(smellResult, smellType, technique); 
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
	
	
}