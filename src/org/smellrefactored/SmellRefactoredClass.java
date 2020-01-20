package org.smellrefactored;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmellRefactoredClass {

	private String[] TECHNIQUES = {"A", "V", "X", "R", "D"};

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
	
	private HashSet<String> getLongClassRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.EXTRACT_CLASS.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_SUBCLASS.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_SUPERCLASS.toString());
		refactoringTypes.add(RefactoringType.CONVERT_ANONYMOUS_CLASS_TO_TYPE.toString());
		// refactoringTypes.add(RefactoringType.INTRODUCE_POLYMORPHISM.toString());
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
		// refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		// refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		// refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		// refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
		return refactoringTypes;
	}

	private HashSet<String> getClassRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.addAll(this.getLongClassRefactoringTypes());
		refactoringTypes.addAll(this.getClassRenameRefactoringTypes());
		return refactoringTypes;
	}
	
	
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

			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (this.getClassRefactoringTypes().contains(refactoring.getRefactoringType())) {
							countRefactoringRelatedClasses++;
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
			pmResultEvaluationClass.write("Total de refatoracoes:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationClass.write("Total de refatoracoes em classes:", countRefactoringRelatedClasses);
			pmResultEvaluationClass.write("Total de trocas de nome de classes:", countRefactoringRelatedClassesRenaming);
			pmResultEvaluationClass.write("Total de refatoracoes para classes longas:", countRefactoringRelatedLongClass);

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

			
			boolean ignorePredictionForDelayedRefactorings = false;
			
			evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, ClassDataSmelly.LONG_CLASS, this.getLongClassRefactoringTypes(), ignorePredictionForDelayedRefactorings);
			
			for (String longClassRefactoringType : this.getLongClassRefactoringTypes()) {
				evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, ClassDataSmelly.LONG_CLASS, new HashSet<String>(Arrays.asList(longClassRefactoringType)), ignorePredictionForDelayedRefactorings);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void evaluateSmellChangeClass(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring, String typeSmell, HashSet<String> targetTefactorings, boolean ignorePredictionForDelayedRefactorings) throws Exception {


		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell + " " + targetTefactorings.toString(), TECHNIQUES);
		if (ignorePredictionForDelayedRefactorings) {
			confusionMatrices.setSubtitle("(ignoring the prediction for delayed refactorings)");
		}

		
		for (ClassDataSmelly classNotSmelly : commitInitial.getClassesNotSmelly()) {
			ClassDataSmelly classBuscar = classNotSmelly;
			boolean renamedClass = false;
			boolean refactoredClass = false;
			Date dateCommitRenamed = null;
			do {
				renamedClass = false;
				String classRenamedName = null;
				for (RefactoringData refactoring : listRefactoring) {
					if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactorings.contains(refactoring.getRefactoringType())) ) {
						continue;
					}
					
					boolean isClassInvolvedBefore = refactoring.getInvolvedClassesBefore()
							.contains(classBuscar.getNomeClasse());
					boolean isClassInvolvedAfter = refactoring.getInvolvedClassesAfter()
							.contains(classBuscar.getNomeClasse());
					
					boolean isClassInvolved = isClassInvolvedBefore || isClassInvolvedAfter 
							|| classBuscar.getNomeClasse().contains(classBuscar.getNomeClasse());

					boolean isClassRefactored = this.wasClassRefactored(classBuscar.getNomeClasse(), refactoring);

					if (isClassInvolved && isClassRefactored) {
						
						if ( (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType()))
								&& refactoring.getLeftSide().contains(classBuscar.getNomeClasse())
								&& refactoring.getInvolvedClassesBefore().contains(classBuscar.getNomeClasse())) {
							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) {
								renamedClass = true;
								dateCommitRenamed = refactoring.getCommitDate();
								classRenamedName = refactoring.getRightSide();
							}
						} else if (isClassInvolvedBefore) { // && !isClassInvolvedAfter
							if (targetTefactorings.contains(refactoring.getRefactoringType())) {
								FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(refactoring.getCommit());
								for (ClassDataSmelly classNotSmell : smellsPreviousCommit.getClassesNotSmelly()) {
									boolean isSameClass = classNotSmell.getNomeClasse()
											.equals(classBuscar.getNomeClasse());
									if (isSameClass) {
										confusionMatrices.incFalseNegativeForAllTechniques();
										refactoredClass = true;
										pmResultSmellRefactoredClassesMessage.write(refactoring.getNomeClasse(),
												refactoring.getSmell(), classNotSmell.getLinesOfCode(),
												refactoring.getListaTecnicas(), refactoring.getCommit(),
												refactoring.getRefactoringType(), refactoring.getLeftSide(),
												refactoring.getRightSide(), refactoring.getFullMessage());
										pmResultSmellRefactoredClasses.write(refactoring.getNomeClasse(),
												refactoring.getSmell(), classNotSmell.getLinesOfCode(),
												refactoring.getListaTecnicas(), refactoring.getCommit(),
												refactoring.getRefactoringType(), refactoring.getLeftSide(),
												refactoring.getRightSide());
										pmResultSmellRefactoredClassesMachineLearning.write(refactoring.getClassDesignRole(),
												classNotSmell.getLinesOfCode(),
												"true", refactoring.getRefactoringType());
										break;
									}
								}
							}
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
				boolean ignorePrediction = false;
				if (ignorePredictionForDelayedRefactorings) {
					if (!confusionMatrices.hasSensibleTechniques()) {
						if (hasDelayedRefactoring(commitInitial.getCommitId(), classBuscar.getNomeClasse(), this.getLongClassRefactoringTypes())) {
							ignorePrediction = true;
						}
					}
				}
						
				if (!ignorePrediction) {
					confusionMatrices.incTrueNegativeForAllTechniques();
					pmResultSmellRefactoredClassesMessage.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
						classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
						"", "");
					pmResultSmellRefactoredClasses.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
						classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
						"");
					pmResultSmellRefactoredClassesMachineLearning.write(classBuscar.getClassDesignRole(),
						classBuscar.getLinesOfCode(), "false", "");
				}
			}
		}

		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {

			if (classSmelly.getSmell().equals(typeSmell)) {
				ClassDataSmelly classSmellyBuscar = classSmelly;
				boolean renamedClass = false;
				confusionMatrices.resetSensibleTechniques();
				Date dateCommitRenamed = null;
				do {
					renamedClass = false;
					String classRenamedName = null;
					for (RefactoringData refactoring : listRefactoring) {
						if ( (!this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType())) && (!targetTefactorings.contains(refactoring.getRefactoringType())) ) {
							continue;
						}
						boolean isClassInvolvedBefore = refactoring.getInvolvedClassesBefore()
								.contains(classSmellyBuscar.getNomeClasse());
						boolean isClassInvolvedAfter = refactoring.getInvolvedClassesAfter()
								.contains(classSmellyBuscar.getNomeClasse());
						boolean isClassInvolved = isClassInvolvedBefore || isClassInvolvedAfter;
						boolean isClassRefactored = this.wasClassRefactored(classSmellyBuscar.getNomeClasse(), refactoring);
						
						
						/*
						 * logger.info( "DEBUG: classSmellyBuscar: " + classSmellyBuscar.getNomeClasse()
						 * + " " + refactoring.getNomeClasse() + " " +
						 * refactoring.getInvolvedClassesBefore() + " " +
						 * refactoring.getInvolvedClassesAfter() + " " + classSmellyBuscar.getSmell() +
						 * " " + refactoring.getRefactoringType() + " " + isClassInvolvedBefore + " " +
						 * isClassInvolvedAfter + " " + isClassInvolved + " " + isClassRefactored );
						 */						
						if (isClassInvolved && isClassRefactored) {
							if ( (this.getClassRenameRefactoringTypes().contains(refactoring.getRefactoringType()))
									&& refactoring.getLeftSide().contains(classSmellyBuscar.getNomeClasse())
									&& refactoring.getInvolvedClassesBefore().contains(classSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) {
									renamedClass = true;
									dateCommitRenamed = refactoring.getCommitDate();
									classRenamedName = refactoring.getRightSide();
								}
							} else if (isClassInvolvedBefore) {
								if (targetTefactorings.contains(refactoring.getRefactoringType())) {
									FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(refactoring.getCommit());
									// logger.info("DEBUG: isLongClassRefactoring: " + refactoring.getCommit());
									for (ClassDataSmelly classSmell : smellsPreviousCommit.getClassesSmell()) {
										boolean isSameClass = classSmell.getNomeClasse()
												.equals(classSmellyBuscar.getNomeClasse());
										// logger.info("DEBUG: Class smell: " + classSmell.getSmell());
										if (isSameClass) {
											if (classSmell.getSmell().equals(typeSmell)) {
												confusionMatrices.incTruePositiveForSensibleTechniques(classSmellyBuscar.getListaTecnicas());
												confusionMatrices.incFalseNegativeForInsensibleTechniquesExcept(classSmellyBuscar.getListaTecnicas());
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
											}
											pmResultSmellRefactoredClassesMachineLearning.write(refactoring.getClassDesignRole(),
													classSmell.getLinesOfCode(), "true", refactoring.getRefactoringType());
										}
									}
								}

							}
						}
					}
					if (renamedClass) {
						classSmellyBuscar.setNomeClasse(classRenamedName);
					} else {
						dateCommitRenamed = null;
					}
				} while (renamedClass && confusionMatrices.hasInsensibleTechniques());
				
				boolean ignorePrediction = false;
				if (ignorePredictionForDelayedRefactorings) {
					if (!confusionMatrices.hasSensibleTechniques()) {
						if (hasDelayedRefactoring(commitInitial.getCommitId(), classSmellyBuscar.getNomeClasse(), this.getLongClassRefactoringTypes())) {
							ignorePrediction = true;
						}
					}
				}
					
				if (!ignorePrediction) {
					confusionMatrices.incFalsePositiveForInsensibleTechniques(classSmellyBuscar.getListaTecnicas());
					confusionMatrices.incTrueNegativeForInsensibleTechniquesExcept(classSmellyBuscar.getListaTecnicas());
					if (confusionMatrices.hasInsensibleTechniques()) {
						pmResultSmellRefactoredClassesMessage.write(classSmellyBuscar.getNomeClasse(),
							classSmellyBuscar.getSmell(), classSmellyBuscar.getLinesOfCode(),
							classSmellyBuscar.getListaTecnicas(), classSmellyBuscar.getCommit(), "", "", "", "");
						pmResultSmellRefactoredClasses.write(classSmellyBuscar.getNomeClasse(),
							classSmellyBuscar.getSmell(), classSmellyBuscar.getLinesOfCode(),
							classSmellyBuscar.getListaTecnicas(), classSmellyBuscar.getCommit(), "", "", "");
					}
				}
				
			}
		}

		pmResultEvaluationClass.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationClass);
	}
	
	
	private boolean hasDelayedRefactoring(String initialCommitId, String className, HashSet<String> refactoringTypes) {
		boolean found = false;
		CommitData nextCommit = getNextCommit(initialCommitId);
		while (nextCommit != null) {
			for (RefactoringData refactoring : listRefactoring) {
				if (refactoring.getCommit().equals(nextCommit.getId())) {
					if (this.getLongClassRefactoringTypes().contains(refactoring.getRefactoringType())) {
						boolean isClassRefactored = this.wasClassRefactored(className, refactoring);
						if (isClassRefactored) {
							logger.info("Refactoring delays found: " + refactoring.getRefactoringType() + " for " + className);
							found = true;
							break;
						}
					}
				}
				if (found) {
					break;
				}
			}
			if (found) {
				break;
			}
			nextCommit = getNextCommit(nextCommit.getId());
		}
		return found;
	}
	
	private CommitData getNextCommit(String commitId) {
		CommitData nextCommit = null;
		for (CommitData commit : commitsMergedIntoMaster) {
			if (commit.getPrevious() != null) {
				if (commit.getPrevious().getId().equals(commitId)) {
					nextCommit = commit;
					break;
				}
			}
		}
		return nextCommit;
	}
	
	private boolean wasClassRefactored(String className, RefactoringData refactoring) {
		return refactoring.getLeftSide().contains(className)
				|| refactoring.getRightSide().contains(className)
				|| ( (refactoring.getNomeClasse()!=null) && (refactoring.getNomeClasse().equals(className)) );
	}

	
}
