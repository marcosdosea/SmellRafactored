package org.smellrefactored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmellRefactoredMethod {

	private String[] TECHNIQUES = {"A", "V", "X", "R", "D"};

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
		pmResultSmellRefactoredMethods = new CSVFile(resultFileName + "-smellRefactored.csv", false);
		pmResultSmellRefactoredMethodsMessage = new CSVFile(resultFileName + "-smellRefactored-message.csv", false);
		pmResultSmellRefactoredMethodsMachineLearning = new CSVFile(resultFileName + "-smellRefactored-methods-machineLearning.csv", false);
	}
	
	public void getSmellRefactoredMethods() {
		try {
			int countRefactoringRelatedOperations = 0;
			int countRefactoringRelatedRenaming = 0;

			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (refactoring.getRefactoringType().contains("OPERATION")) {
							countRefactoringRelatedOperations++;
						}
						if (refactoring.getRefactoringType().equals("RENAME_METHOD")) {
							countRefactoringRelatedRenaming++;
						}
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);
			
			pmResultEvaluationMethods.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluationMethods.write("Numero Refatoracoes em Metodos e Nao Metodos:",
					listRefactoringMergedIntoMaster.size());
			pmResultEvaluationMethods.write("Numero Refatoracoes relacionadas a operacoes em Metodos:",
					countRefactoringRelatedOperations);
			pmResultEvaluationMethods.write("Numero Refatoracoes relacionadas a rename em Metodos:",
					countRefactoringRelatedRenaming);

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

			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster,
					MethodDataSmelly.LONG_METHOD);
			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster,
					MethodDataSmelly.COMPLEX_METHOD);
			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster,
					MethodDataSmelly.HIGH_EFFERENT_COUPLING);
			evaluateSmellChangeParameters(smellsCommitInitial, listRefactoringMergedIntoMaster,
					MethodDataSmelly.MANY_PARAMETERS);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void evaluateSmellChangeOperation(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String typeSmell) throws Exception {

		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell, TECHNIQUES);

		for (MethodDataSmelly methodNotSmelly : commitInitial.getMetodosNotSmelly()) {
			MethodDataSmelly methodBuscar = methodNotSmelly;
			boolean renamedMethod = false;
			boolean refactoredMethod = false;
			Date dateCommitRenamed = null;
			do {
				renamedMethod = false;
				String methodRenamedName = null;
				for (RefactoringData methodRefactored : listRefactoring) {
					boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
							.contains(methodBuscar.getNomeClasse())
							|| methodRefactored.getInvolvedClassesAfter().contains(methodBuscar.getNomeClasse());

					boolean isMethodRefactored = methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
							|| methodRefactored.getRightSide().contains(methodBuscar.getNomeMetodo());

					if (isClassInvolved && isMethodRefactored) {
						if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
								&& methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
								&& methodRefactored.getInvolvedClassesBefore().contains(methodBuscar.getNomeClasse())) {

							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
								renamedMethod = true;
								dateCommitRenamed = methodRefactored.getCommitDate();
								methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
							}
						} else if (methodRefactored.getRefactoringType().contains("OPERATION")) {
							FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(
									methodRefactored.getCommit());
							for (MethodDataSmelly methodNotSmell : smellsPreviousCommit.getMetodosNotSmelly()) {
								boolean isSameClassMethod = methodNotSmell.getNomeClasse()
										.equals(methodBuscar.getNomeClasse())
										&& methodNotSmell.getNomeMetodo().equals(methodBuscar.getNomeMetodo());
								if (isSameClassMethod) {
									confusionMatrices.incFalseNegativeForAllTechniques();
									refactoredMethod = true;
									pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
											methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
											methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
											methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
											methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
											methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
											methodRefactored.getRightSide(), methodRefactored.getFullMessage());
									pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
											methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
											methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
											methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
											methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
											methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
											methodRefactored.getRightSide());
									pmResultSmellRefactoredMethodsMachineLearning.write(methodRefactored.getClassDesignRole(),
											methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
											methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
											"true", methodRefactored.getRefactoringType());
									break;
								}
							}
						}
					}
				}
				if (renamedMethod) {
					methodBuscar.setNomeMetodo(methodRenamedName);
				} else {
					dateCommitRenamed = null;
				}
			} while (renamedMethod && !refactoredMethod);
			
			if (!refactoredMethod) {
				confusionMatrices.incTrueNegativeForAllTechniques();
				pmResultSmellRefactoredMethodsMessage.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "", "");
				pmResultSmellRefactoredMethods.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "");
				pmResultSmellRefactoredMethodsMachineLearning.write(methodBuscar.getClassDesignRole(),
						methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						"false", "");
			}
		}

		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(typeSmell)) {
				MethodDataSmelly methodSmellyBuscar = methodSmelly;
				boolean renamedMethod = false;
				confusionMatrices.resetSensibleTechniques();
				Date dateCommitRenamed = null;
				do {
					renamedMethod = false;
					String methodRenamedName = null;
					for (RefactoringData methodRefactored : listRefactoring) {
						boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
								.contains(methodSmellyBuscar.getNomeClasse())
								|| methodRefactored.getInvolvedClassesAfter()
										.contains(methodSmellyBuscar.getNomeClasse());
						boolean isMethodRefactored = methodRefactored.getLeftSide()
								.contains(methodSmellyBuscar.getNomeMetodo())
								|| methodRefactored.getRightSide().contains(methodSmellyBuscar.getNomeMetodo());

						if (isClassInvolved && isMethodRefactored) {
							if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
									&& methodRefactored.getLeftSide().contains(methodSmellyBuscar.getNomeMetodo())
									&& methodRefactored.getInvolvedClassesBefore()
											.contains(methodSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
									renamedMethod = true;
									dateCommitRenamed = methodRefactored.getCommitDate();
									methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
								}
							} else if (methodRefactored.getRefactoringType().contains("OPERATION")) {
								FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(
										methodRefactored.getCommit());
								for (MethodDataSmelly methodSmell : smellsPreviousCommit.getMetodosSmell()) {
									boolean isSameClassMethod = methodSmell.getNomeClasse()
											.equals(methodSmellyBuscar.getNomeClasse())
											&& methodSmell.getNomeMetodo().equals(methodSmellyBuscar.getNomeMetodo());
									if (isSameClassMethod) {
										if ((isSameClassMethod) && methodSmell.getSmell().equals(typeSmell)) {
											confusionMatrices.incTruePositiveForSensibleTechniques(methodSmellyBuscar.getListaTecnicas());
											confusionMatrices.incFalseNegativeForInsensibleTechniquesExcept(methodSmellyBuscar.getListaTecnicas());
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
										}
										pmResultSmellRefactoredMethodsMachineLearning.write(methodRefactored.getClassDesignRole(),
												methodRefactored.getLinesOfCode(), methodRefactored.getComplexity(),
												methodRefactored.getEfferent(), methodRefactored.getNumberOfParameters(),
												"true", methodRefactored.getRefactoringType());
									}
								}
							}
						}
					}
					if (renamedMethod) {
						methodSmellyBuscar.setNomeMetodo(methodRenamedName);
					} else {
						dateCommitRenamed = null;
					}
				} while (renamedMethod && confusionMatrices.hasInsensibleTechniques());
				
				confusionMatrices.incFalsePositiveForInsensibleTechniques(methodSmellyBuscar.getListaTecnicas());
				confusionMatrices.incTrueNegativeForInsensibleTechniquesExcept(methodSmellyBuscar.getListaTecnicas());
				if (confusionMatrices.hasInsensibleTechniques()) {
					pmResultSmellRefactoredMethodsMessage.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");
					pmResultSmellRefactoredMethods.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		pmResultEvaluationMethods.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationMethods);
	
	}

	private void evaluateSmellChangeParameters(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, String typeSmell) throws Exception {

		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell, TECHNIQUES);

		for (MethodDataSmelly methodNotSmelly : commitInitial.getMetodosNotSmelly()) {
			MethodDataSmelly methodBuscar = methodNotSmelly;
			boolean renamedMethod = false;
			boolean refactoredMethod = false;
			Date dateCommitRenamed = null;
			do {
				renamedMethod = false;
				String methodRenamedName = null;
				for (RefactoringData methodRefactored : listRefactoring) {
					boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
							.contains(methodBuscar.getNomeClasse())
							|| methodRefactored.getInvolvedClassesAfter().contains(methodBuscar.getNomeClasse());

					boolean isMethodRefactored = methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
							|| methodRefactored.getRightSide().contains(methodBuscar.getNomeMetodo());

					if (isClassInvolved && isMethodRefactored) {
						if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
								&& methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
								&& methodRefactored.getInvolvedClassesBefore().contains(methodBuscar.getNomeClasse())) {

							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
								renamedMethod = true;
								dateCommitRenamed = methodRefactored.getCommitDate();
								methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
								if (countParameters(methodRefactored.getRightSide()) < countParameters(
										methodRefactored.getLeftSide())) {
									FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(
											methodRefactored.getCommit());
									for (MethodDataSmelly methodNotSmell : smellsPreviousCommit.getMetodosNotSmelly()) {
										boolean isSameClassMethod = methodNotSmell.getNomeClasse()
												.equals(methodBuscar.getNomeClasse())
												&& methodNotSmell.getNomeMetodo().equals(methodBuscar.getNomeMetodo());
										if (isSameClassMethod) {
											confusionMatrices.incFalseNegativeForAllTechniques();
											refactoredMethod = true;
											pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
													methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
													methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
													methodNotSmell.getEfferent(),
													methodNotSmell.getNumberOfParameters(),
													methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
													methodRefactored.getRefactoringType(),
													methodRefactored.getLeftSide(), methodRefactored.getRightSide(),
													methodRefactored.getFullMessage());
											pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
													methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
													methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
													methodNotSmell.getEfferent(),
													methodNotSmell.getNumberOfParameters(),
													methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
													methodRefactored.getRefactoringType(),
													methodRefactored.getLeftSide(), methodRefactored.getRightSide());
											pmResultSmellRefactoredMethodsMachineLearning.write(methodRefactored.getClassDesignRole(),
													methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
													methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
													"true", methodRefactored.getRefactoringType());
											break;
										}
									}
								}
							}
						}
					}
				}
				if (renamedMethod) {
					methodBuscar.setNomeMetodo(methodRenamedName);
				} else {
					dateCommitRenamed = null;
				}
			} while (renamedMethod && !refactoredMethod);
			
			if (!refactoredMethod) {
				confusionMatrices.incTrueNegativeForAllTechniques();
				pmResultSmellRefactoredMethodsMessage.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "", "");
				pmResultSmellRefactoredMethods.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "");
				pmResultSmellRefactoredMethodsMachineLearning.write(methodBuscar.getClassDesignRole(),
						methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						"false", "");
			}
		}

		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(typeSmell)) {
				MethodDataSmelly methodSmellyBuscar = methodSmelly;
				boolean renamedMethod = false;
				confusionMatrices.resetSensibleTechniques();
				Date dateCommitRenamed = null;
				do {
					renamedMethod = false;
					String methodRenamedName = null;
					for (RefactoringData methodRefactored : listRefactoring) {
						boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
								.contains(methodSmellyBuscar.getNomeClasse())
								|| methodRefactored.getInvolvedClassesAfter()
										.contains(methodSmellyBuscar.getNomeClasse());
						boolean isMethodRefactored = methodRefactored.getLeftSide()
								.contains(methodSmellyBuscar.getNomeMetodo())
								|| methodRefactored.getRightSide().contains(methodSmellyBuscar.getNomeMetodo());

						if (isClassInvolved && isMethodRefactored) {
							if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
									&& methodRefactored.getLeftSide().contains(methodSmellyBuscar.getNomeMetodo())
									&& methodRefactored.getInvolvedClassesBefore()
											.contains(methodSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
									renamedMethod = true;
									dateCommitRenamed = methodRefactored.getCommitDate();
									methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());

									if (countParameters(methodRefactored.getRightSide()) < countParameters(
											methodRefactored.getLeftSide())) {
										FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(
												methodRefactored.getCommit());
										for (MethodDataSmelly methodSmell : smellsPreviousCommit.getMetodosSmell()) {
											boolean isSameClassMethod = methodSmell.getNomeClasse()
													.equals(methodSmellyBuscar.getNomeClasse())
													&& methodSmell.getNomeMetodo()
															.equals(methodSmellyBuscar.getNomeMetodo());
											if (isSameClassMethod) {
												if (methodSmell.getSmell().equals(typeSmell)) {
													confusionMatrices.incTruePositiveForSensibleTechniques(methodSmellyBuscar.getListaTecnicas());
													confusionMatrices.incFalseNegativeForInsensibleTechniquesExcept(methodSmellyBuscar.getListaTecnicas());
													pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
														methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
														methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
														methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
														methodRefactored.getListaTecnicas(),
														methodRefactored.getCommit(),
														methodRefactored.getRefactoringType(),
														methodRefactored.getLeftSide(), methodRefactored.getRightSide(),
														methodRefactored.getFullMessage());
													pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
														methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
														methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
														methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
														methodRefactored.getListaTecnicas(),
														methodRefactored.getCommit(),
														methodRefactored.getRefactoringType(),
														methodRefactored.getLeftSide(),
														methodRefactored.getRightSide());
												}
												pmResultSmellRefactoredMethodsMachineLearning.write(methodRefactored.getClassDesignRole(),
														methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
														methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
														"true", methodRefactored.getRefactoringType());
											}
										}
									}
								}
							}
						}
					}
					if (renamedMethod) {
						methodSmellyBuscar.setNomeMetodo(methodRenamedName);
					} else {
						dateCommitRenamed = null;
					}
				} while (renamedMethod && confusionMatrices.hasInsensibleTechniques());

				confusionMatrices.incFalsePositiveForInsensibleTechniques(methodSmellyBuscar.getListaTecnicas());
				confusionMatrices.incTrueNegativeForInsensibleTechniquesExcept(methodSmellyBuscar.getListaTecnicas());
				if (confusionMatrices.hasInsensibleTechniques()) {
					pmResultSmellRefactoredMethodsMessage.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");

					pmResultSmellRefactoredMethods.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		pmResultEvaluationMethods.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationMethods);
		
	}
	
	private static String extrairNomeMetodo(String rightSide) {
		String nomeMetodo = rightSide.substring(0, rightSide.indexOf(")") + 1);
		if (nomeMetodo.contains("public ")) {
			nomeMetodo = nomeMetodo.substring("public ".length());
		}
		if (nomeMetodo.contains("private ")) {
			nomeMetodo = nomeMetodo.substring("private ".length());
		}
		if (nomeMetodo.contains("protected ")) {
			nomeMetodo = nomeMetodo.substring("protected ".length());
		}
		return nomeMetodo;
	}

	private int countParameters(String metodo) {
		int countParams = 0;
		if (metodo.contains(",")) {
			countParams = 1;
			for (int i = 0; i < metodo.length(); i++) {
				if (metodo.charAt(i) == ',') {
					countParams++;
				}
			}
		} else {
			int posPrimeiroParenteses = metodo.indexOf("(");
			int posUltimoParenteses = metodo.indexOf(")");
			for (int i = posPrimeiroParenteses + 1; i < posUltimoParenteses - 1; i++) {
				if (metodo.charAt(i) != ' ' && metodo.charAt(i) != ')') {
					countParams++;
					break;
				}
			}
		}
		return countParams;
	}

	
}