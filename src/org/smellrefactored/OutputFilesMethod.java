package org.smellrefactored;

import java.util.Date;

import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutputFilesMethod {

	private CommitRange commitRange;
	String baseFileName;
	
	PersistenceMechanism pmResultSmellRefactoredMethods;
	PersistenceMechanism pmResultSmellRefactoredMethodsMessage;
	PersistenceMechanism pmResultSmellRefactoredMethodsMachineLearning;

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputFilesMethod(CommitRange commitRange, String baseFileName) {
		this.commitRange = commitRange;
		this.baseFileName = baseFileName;
		
		pmResultSmellRefactoredMethods = new CSVFile(this.baseFileName + "-smellRefactored-methods.csv", false);
		pmResultSmellRefactoredMethodsMessage = new CSVFile(this.baseFileName + "-smellRefactored-methods-message.csv", false);
		pmResultSmellRefactoredMethodsMachineLearning = new CSVFile(this.baseFileName + "-refactoredAndNotRefactored-methods-machineLearning.csv", false);
	}
	
	public void writeHeaders() {
		pmResultSmellRefactoredMethodsMessage.write(
				"Class"
				, "Method"
				, "Smell"
				, "LOC"
				, "CC"
				, "EC"
				, "NOP"
				, "Tecnicas"
				, "Commit"
				, "Refactoring"
				, "Left Side"
				, "Right Side"
				, "Full Message"
				);
		pmResultSmellRefactoredMethods.write(
				"Class"
				, "Method"
				, "Smell"
				, "LOC"
				, "CC"
				, "EC"
				, "NOP"
				, "Tecnicas"
				, "Commit"
				, "Refactoring"
				, "Left Side"
				, "Right Side"
				);
		pmResultSmellRefactoredMethodsMachineLearning.write(
				"commitId"
				, "commitDate"
				, "filePath"
				, "className"
				, "methodName"
				, "designRole"
				, "loc"
				, "cc"
				, "ec"
				, "nop"
				, "isRefactoring"
				, "refactoring"
				);
	}
	
	public void writeTruePositiveToCsvFiles(RefactoringEvent refactoring, MethodDataSmelly methodSmell) throws Exception {
		pmResultSmellRefactoredMethodsMessage.write(
				refactoring.getClassName()
				, refactoring.getMethodName()
				, methodSmell.getSmell()
				, methodSmell.getLinesOfCode()
				, methodSmell.getComplexity()
				, methodSmell.getEfferent()
				, methodSmell.getNumberOfParameters()
				, methodSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				, refactoring.getCommitData().getFullMessage()
			);
		pmResultSmellRefactoredMethods.write(
				refactoring.getClassName()
				, refactoring.getMethodName()
				, methodSmell.getSmell()
				, methodSmell.getLinesOfCode()
				, methodSmell.getComplexity()
				, methodSmell.getEfferent()
				, methodSmell.getNumberOfParameters()
				, methodSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
			);
		pmResultSmellRefactoredMethodsMachineLearning.write(
				methodSmell.getCommit()
				, getCommitDateAsString(methodSmell.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getClassName()
				, refactoring.getMethodName()
				, methodSmell.getClassDesignRole()
				, methodSmell.getLinesOfCode()
				, methodSmell.getComplexity()
				, methodSmell.getEfferent()
				, methodSmell.getNumberOfParameters()
				, "true"
				, refactoring.getRefactoringType()
				);
		
	}

	public void writeFalseNegativeToCsvFiles(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) throws Exception {
		pmResultSmellRefactoredMethodsMessage.write(
				refactoring.getClassName()
				, refactoring.getMethodName()
				, (methodNotSmell.getSmell() != null ? methodNotSmell.getSmell() : "")
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, methodNotSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				, refactoring.getCommitData().getFullMessage()
				);
		pmResultSmellRefactoredMethods.write(
				refactoring.getClassName()
				, refactoring.getMethodName()
				, (methodNotSmell.getSmell() != null ? methodNotSmell.getSmell() : "")
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, methodNotSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, refactoring.getRefactoringType()
				, refactoring.getLeftSide()
				, refactoring.getRightSide()
				);
		pmResultSmellRefactoredMethodsMachineLearning.write(
				methodNotSmell.getCommit()
				, getCommitDateAsString(methodNotSmell.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getClassName()
				, refactoring.getMethodName()
				, methodNotSmell.getClassDesignRole()
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, "true"
				, refactoring.getRefactoringType()
				);
	}

	public void writeFalsePositiveToCsvFiles(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) throws Exception {
		if (methodNotSmell == null) {
			methodNotSmell = new MethodDataSmelly();
			logger.warn("Null response for querying non-smelling method.");
		}
		pmResultSmellRefactoredMethodsMessage.write(
				refactoring.getClassName()
				, refactoring.getMethodName()
				, (methodNotSmell.getSmell() != null ? methodNotSmell.getSmell() : "")
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, methodNotSmell.getListaTecnicas()
				, refactoring.getCommitId()
				, ""
				, ""
				, ""
				, ""
				);
		pmResultSmellRefactoredMethods.write(
				refactoring.getClassName()
				, refactoring.getMethodName()
				, (methodNotSmell.getSmell() != null ? methodNotSmell.getSmell() : "")
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, methodNotSmell.getListaTecnicas()
				, methodNotSmell.getCommit()
				, ""
				, ""
				, ""
				);
		pmResultSmellRefactoredMethodsMachineLearning.write(
				methodNotSmell.getCommit()
				, getCommitDateAsString(methodNotSmell.getCommit())
				, refactoring.getFileNameAfter()
				, refactoring.getClassName()
				, refactoring.getMethodName()
				, methodNotSmell.getClassDesignRole()
				, methodNotSmell.getLinesOfCode()
				, methodNotSmell.getComplexity()
				, methodNotSmell.getEfferent()
				, methodNotSmell.getNumberOfParameters()
				, "false"
				, ""
				);
	}

	public void writeNegativeToCsvFiles(MethodDataSmelly methodSmellyBuscar) {
		pmResultSmellRefactoredMethodsMessage.write(
				methodSmellyBuscar.getNomeClasse()
				, methodSmellyBuscar.getNomeMetodo()
				, (methodSmellyBuscar.getSmell() != null ? methodSmellyBuscar.getSmell() : "")
				, methodSmellyBuscar.getLinesOfCode()
				, methodSmellyBuscar.getComplexity()
				, methodSmellyBuscar.getEfferent()
				, methodSmellyBuscar.getNumberOfParameters()
				, methodSmellyBuscar.getListaTecnicas()
				, methodSmellyBuscar.getCommit()
				, ""
				, ""
				, ""
				, ""
			);
		pmResultSmellRefactoredMethods.write(
				methodSmellyBuscar.getNomeClasse()
				, methodSmellyBuscar.getNomeMetodo()
				, (methodSmellyBuscar.getSmell() != null ? methodSmellyBuscar.getSmell() : "")
				, methodSmellyBuscar.getLinesOfCode()
				, methodSmellyBuscar.getComplexity()
				, methodSmellyBuscar.getEfferent()
				, methodSmellyBuscar.getNumberOfParameters()
				, methodSmellyBuscar.getListaTecnicas()
				, methodSmellyBuscar.getCommit()
				, ""
				, ""
				, ""
			);
		pmResultSmellRefactoredMethodsMachineLearning.write(
				methodSmellyBuscar.getCommit()
				, getCommitDateAsString(methodSmellyBuscar.getCommit())
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

	public void close() {
		pmResultSmellRefactoredMethodsMessage.close();
		pmResultSmellRefactoredMethods.close();
		pmResultSmellRefactoredMethodsMachineLearning.close();
		
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
