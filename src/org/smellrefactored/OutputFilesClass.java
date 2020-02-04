package org.smellrefactored;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputFilesClass {

	private CommitRange commitRange;
	private String baseFileName;
	
	private PersistenceMechanism pmResultSmellRefactoredClasses;
	private PersistenceMechanism pmResultSmellRefactoredClassesMessage;
	private PersistenceMechanism pmResultSmellRefactoredClassesMachineLearning;

	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputFilesClass(CommitRange commitRange, String baseFileName) {
		this.commitRange = commitRange;
		this.baseFileName = baseFileName;
		
		pmResultSmellRefactoredClasses = new CSVFile(this.baseFileName + "-smellRefactored-classes.csv", false);
		pmResultSmellRefactoredClassesMessage = new CSVFile(this.baseFileName + "-smellRefactored-classes-message.csv", false);
		pmResultSmellRefactoredClassesMachineLearning = new CSVFile(this.baseFileName + "-refactoredAndNotRefactored-classes-machineLearning.csv", false);
	}
	
	public void writeHeaders() {
		pmResultSmellRefactoredClassesMessage.write(
				"Class"
				, "Smell"
				, "CLOC"
				, "Tecnicas"
				, "Commit"
				, "Refactoring"
				, "Left Side"
				, "Right Side"
				, "Full Message"
				);
		pmResultSmellRefactoredClasses.write(
				"Class"
				, "Smell"
				, "CLOC"
				, "Tecnicas"
				, "Commit"
				, "Refactoring"
				, "Left Side"
				, "Right Side"
				);
		pmResultSmellRefactoredClassesMachineLearning.write(
				"commitId"
				, "commitDate"
				, "filePath"
				, "className"
				, "designRole"
				, "cloc"
				, "isRefactoring"
				, "refactoring"
				);
	}

	
	public void writeTruePositiveToCsvFiles(RefactoringEvent refactoring, ClassDataSmelly classSmell) throws Exception {
		pmResultSmellRefactoredClassesMessage.write(
				refactoring.getClassName()
				, classSmell.getSmell()
				, classSmell.getLinesOfCode()
				, classSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				, refactoring.getCommitData().getFullMessage()
				);
		pmResultSmellRefactoredClasses.write(
				refactoring.getClassName()
				, classSmell.getSmell()
				, classSmell.getLinesOfCode()
				, classSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				);
		pmResultSmellRefactoredClassesMachineLearning.write(
				classSmell.getCommit()
				, getCommitDateAsString(classSmell.getCommit())
				, classSmell
				, refactoring.getFileNameAfter()
				, refactoring.getClassName()
				, classSmell.getClassDesignRole()
				, classSmell.getLinesOfCode()
				, "true"
				, refactoring.getRefactoringType()
				);
	}

	public void writeFalseNegativeToCsvFiles(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) throws Exception {
		pmResultSmellRefactoredClassesMessage.write(
				refactoring.getClassName()
				, (classNotSmell.getSmell() != null ? classNotSmell.getSmell() : "")
				, classNotSmell.getLinesOfCode()
				, classNotSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				, refactoring.getCommitData().getFullMessage()
				);
		pmResultSmellRefactoredClasses.write(
				refactoring.getClassName()
				, (classNotSmell.getSmell() != null ? classNotSmell.getSmell() : "")
				, classNotSmell.getLinesOfCode()
				, classNotSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				);
		pmResultSmellRefactoredClassesMachineLearning.write(
				classNotSmell.getCommit()
				, getCommitDateAsString(classNotSmell.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getClassName()
				, classNotSmell.getClassDesignRole()
				, classNotSmell.getLinesOfCode()
				, "true"
				, refactoring.getRefactoringType()
				);
	}	

	public void writeFalsePositiveToCsvFiles(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) throws Exception {
		if (classNotSmell == null) {
			classNotSmell = new ClassDataSmelly();
			logger.warn("Null response for querying non-smelling classes.");
		}
		pmResultSmellRefactoredClassesMessage.write(
				refactoring.getClassName()
				, classNotSmell.getSmell()
				, classNotSmell.getLinesOfCode()
				, classNotSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, ""
				, ""
				, ""
				, ""
				);
		pmResultSmellRefactoredClasses.write(
				refactoring.getClassName()
				, classNotSmell.getSmell()
				, classNotSmell.getLinesOfCode()
				, classNotSmell.getListaTecnicas()
				, classNotSmell.getCommit()
				, ""
				, ""
				, ""
				);
		pmResultSmellRefactoredClassesMachineLearning.write(
				classNotSmell.getCommit()
				, getCommitDateAsString(classNotSmell.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getClassName()
				, classNotSmell.getClassDesignRole()
				, classNotSmell.getLinesOfCode()
				, "false"
				, ""
				);
	}
	
	public void writeNegativeToCsvFiles(ClassDataSmelly classBuscar) {
		pmResultSmellRefactoredClassesMessage.write(
				classBuscar.getNomeClasse()
				, classBuscar.getSmell()
				, classBuscar.getLinesOfCode()
				, classBuscar.getListaTecnicas()
				, classBuscar.getCommit()
				, ""
				, ""
				, ""
				, ""
				);
		pmResultSmellRefactoredClasses.write(
				classBuscar.getNomeClasse()
				, classBuscar.getSmell()
				, classBuscar.getLinesOfCode()
				, classBuscar.getListaTecnicas()
				, classBuscar.getCommit()
				, ""
				, ""
				, ""
				);
		pmResultSmellRefactoredClassesMachineLearning.write(
				classBuscar.getCommit() 
				, getCommitDateAsString(classBuscar.getCommit())
				, classBuscar.getDiretorioDaClasse()
				, classBuscar.getNomeClasse()
				, classBuscar.getClassDesignRole()
				, classBuscar.getLinesOfCode()
				, "false"
				, ""
				);
	}
	
	public void close() {
		pmResultSmellRefactoredClassesMessage.close();
		pmResultSmellRefactoredClasses.close();
		pmResultSmellRefactoredClassesMachineLearning.close();
		
	}

	private String getCommitDateAsString(String commitId) {
		CommitData commit = null;
		if (commitId != null) {
			try {
				commit = this.commitRange.getCommitById(commitId);
			} catch (Exception e) {
				// do nothing
			}
		}
		return (commit != null ? commit.getDate().toString() : null);
	}

}
