package org.smellrefactored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmellRefactoredClass {

	private String[] TECHNIQUES = {"A", "V", "X", "R", "D"};
	
	public static final String[] LONG_CLASS_REFACTORIES = {
			RefactoringType.EXTRACT_CLASS.toString()
			, RefactoringType.EXTRACT_SUBCLASS.toString()
			, RefactoringType.EXTRACT_SUPERCLASS.toString()
			, RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString()
			, RefactoringType.MOVE_OPERATION.toString()
			, RefactoringType.PULL_UP_OPERATION.toString()
			, RefactoringType.PUSH_DOWN_OPERATION.toString()
			};
	public static final String[] CLASS_REFACTORIES = {
			RefactoringType.EXTRACT_CLASS.toString()
			, RefactoringType.EXTRACT_SUBCLASS.toString()
			, RefactoringType.EXTRACT_SUPERCLASS.toString()
			, RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString()
			, RefactoringType.MOVE_OPERATION.toString()
			, RefactoringType.PULL_UP_OPERATION.toString()
			, RefactoringType.PUSH_DOWN_OPERATION.toString()
			, RefactoringType.RENAME_CLASS.toString()
			};

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	ArrayList<RefactoringData> listRefactoring;
	private String initialCommit;
	ArrayList<CommitData> commitsMergedIntoMaster;
	String resultFileName;
	
	PersistenceMechanism pmResultEvaluationClass;
	PersistenceMechanism pmResultSmellRefactoredClasses;
	PersistenceMechanism pmResultSmellRefactoredClassesMessage;
	
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
	}
	
	
	public void getSmellRefactoredClasses() {
		try {
			int countRefactoringRelatedClasses = 0;

			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (refactoring.getRefactoringType().equals(RefactoringType.RENAME_CLASS.toString())) {
							// countRefactoringRelatedRenaming++;
						} else {
							for (String longClassRefactoring : CLASS_REFACTORIES) {
								if (refactoring.getRefactoringType().equals(longClassRefactoring)) {
									countRefactoringRelatedClasses++;
									/// break;
								}
							}
						}
					}
				}
			}
			
			Collections.sort(listRefactoringMergedIntoMaster);
			
			pmResultEvaluationClass.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluationClass.write("Numero Refatoracoes em Metodos e Nao Metodos:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationClass.write("Numero Refatoracoes relacionadas a Classes:", countRefactoringRelatedClasses);

			FilterSmellResult smellsCommitInitial = this.commitSmell.obterSmellsCommit(initialCommit);

			pmResultEvaluationClass.write("Numero Classes Smell Commit Inicial:",
					smellsCommitInitial.getClassesSmell().size());
			pmResultEvaluationClass.write("Numero Classes NOT Smell Commit Inicial:",
					smellsCommitInitial.getClassesNotSmelly().size());

			pmResultSmellRefactoredClassesMessage.write("Class", "Smell", "CLOC", "Tecnicas", "Commit", "Refactoring",
					"Left Side", "Right Side", "Full Message");
			pmResultSmellRefactoredClasses.write("Class", "Smell", "CLOC", "Tecnicas", "Commit", "Refactoring",
					"Left Side", "Right Side");

			evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, ClassDataSmelly.LONG_CLASS);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void evaluateSmellChangeClass(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring, String typeSmell) throws Exception {

		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell, TECHNIQUES);
		
		for (ClassDataSmelly classNotSmelly : commitInitial.getClassesNotSmelly()) {
			ClassDataSmelly classBuscar = classNotSmelly;
			boolean renamedClass = false;
			boolean refactoredClass = false;
			Date dateCommitRenamed = null;
			do {
				renamedClass = false;
				String classRenamedName = null;
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassInvolvedBefore = refactoring.getInvolvedClassesBefore()
							.contains(classBuscar.getNomeClasse());
					boolean isClassInvolvedAfter = refactoring.getInvolvedClassesAfter()
							.contains(classBuscar.getNomeClasse());
					boolean isClassInvolved = isClassInvolvedBefore || isClassInvolvedAfter;

					boolean isClassRefactored = refactoring.getLeftSide().contains(classBuscar.getNomeClasse())
							|| refactoring.getRightSide().contains(classBuscar.getNomeClasse());

					if (isClassInvolved && isClassRefactored) {
						if (refactoring.getRefactoringType().equals(RefactoringType.RENAME_CLASS.toString())
								&& refactoring.getLeftSide().contains(classBuscar.getNomeClasse())
								&& refactoring.getInvolvedClassesBefore().contains(classBuscar.getNomeClasse())) {

							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) {
								renamedClass = true;
								dateCommitRenamed = refactoring.getCommitDate();
								classRenamedName = refactoring.getRightSide();
							}
						} else if (isClassInvolvedBefore) { // && !isClassInvolvedAfter
							Boolean isClassRefactoring = false; 
							for (String longClassRefactoring : CLASS_REFACTORIES) {
								if (refactoring.getRefactoringType().equals(longClassRefactoring)) {
									isClassRefactoring = true;
									break;
								}
							}
							if (isClassRefactoring) {
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
				confusionMatrices.incTrueNegativeForAllTechniques();
				pmResultSmellRefactoredClassesMessage.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
						classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
						"", "");

				pmResultSmellRefactoredClasses.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
						classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
						"");
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
						boolean isClassInvolvedBefore = refactoring.getInvolvedClassesBefore()
								.contains(classSmellyBuscar.getNomeClasse());
						boolean isClassInvolvedAfter = refactoring.getInvolvedClassesAfter()
								.contains(classSmellyBuscar.getNomeClasse());
						boolean isClassInvolved = isClassInvolvedBefore || isClassInvolvedAfter;
						boolean isClassRefactored = refactoring.getLeftSide()
								.contains(classSmellyBuscar.getNomeClasse())
								|| refactoring.getRightSide().contains(classSmellyBuscar.getNomeClasse());
						
						
						/*
						 * logger.info( "DEBUG: classSmellyBuscar: " + classSmellyBuscar.getNomeClasse()
						 * + " " + refactoring.getNomeClasse() + " " +
						 * refactoring.getInvolvedClassesBefore() + " " +
						 * refactoring.getInvolvedClassesAfter() + " " + classSmellyBuscar.getSmell() +
						 * " " + refactoring.getRefactoringType() + " " + isClassInvolvedBefore + " " +
						 * isClassInvolvedAfter + " " + isClassInvolved + " " + isClassRefactored );
						 */						
						if (isClassInvolved && isClassRefactored) {
							if (refactoring.getRefactoringType().equals(RefactoringType.RENAME_CLASS.toString())
									&& refactoring.getLeftSide().contains(classSmellyBuscar.getNomeClasse())
									&& refactoring.getInvolvedClassesBefore()
											.contains(classSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) {
									renamedClass = true;
									dateCommitRenamed = refactoring.getCommitDate();
									classRenamedName = refactoring.getRightSide();
								}
							} else if (isClassInvolvedBefore) {
								Boolean isLongClassRefactoring = false; 
								for (String longClassRefactoring : LONG_CLASS_REFACTORIES) {
									if (refactoring.getRefactoringType().equals(longClassRefactoring)) {
										// logger.info("DEBUG: isLongClassRefactoring: " + longClassRefactoring + " " + classSmellyBuscar.getNomeClasse());
										isLongClassRefactoring = true;
										break;
									}
								}
								if (isLongClassRefactoring) {
									FilterSmellResult smellsPreviousCommit = this.commitSmell.obterSmellsPreviousCommit(refactoring.getCommit());
									// logger.info("DEBUG: isLongClassRefactoring: " + refactoring.getCommit());
									for (ClassDataSmelly classSmell : smellsPreviousCommit.getClassesSmell()) {
										boolean isSameClass = classSmell.getNomeClasse()
												.equals(classSmellyBuscar.getNomeClasse());
										// logger.info("DEBUG: Class smell: " + classSmell.getSmell());
										if ((isSameClass) && classSmell.getSmell().equals(typeSmell)) {
											confusionMatrices.incTruePositiveForSensibleTechniques(classSmellyBuscar.getListaTecnicas());
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
				
				confusionMatrices.incFalsePositiveForInsensibleTechniques(classSmellyBuscar.getListaTecnicas());
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

		pmResultEvaluationClass.write("");
		confusionMatrices.writeToCsvFile(pmResultEvaluationClass);
	}
	
}
