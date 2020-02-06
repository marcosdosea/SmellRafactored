package org.smellrefactored.methods;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.CommitRange;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.SmellRefactoredManager;

import com.opencsv.CSVWriter;

public class OutputMethodFileMachineLearningCsv {

	private CommitRange commitRange;
	private Set<String> techniques;
	private String baseFileName;
	
	private CSVWriter csvFile;
	
	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputMethodFileMachineLearningCsv(CommitRange commitRange, Set<String> techniques, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.techniques = techniques;
		this.baseFileName = baseFileName;
		
		csvFile = new CSVWriter(new FileWriter(this.baseFileName + "-refactoredAndNotRefactored-methods-machineLearning.csv"));
	}
	
	public void writeHeader() {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("commitId");
		fields.add("commitDate");
		fields.add("filePath");
		fields.add("designRole");
		fields.add("className");
		fields.add("methodName");
		fields.add("loc");
		fields.add("cc");
		fields.add("ec");
		fields.add("nop");
		fields.add("isRefactoring");
		fields.add("refactoring");
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTruePositive(RefactoringEvent refactoring, MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(methodSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(methodSmell.getCommit()));
		fields.add(refactoring.getFileNameAfter());
		fields.add(methodSmell.getClassDesignRole());
		fields.add(refactoring.getClassName());
		fields.add(refactoring.getMethodName());
		fields.add(String.valueOf(methodSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodSmell.getComplexity()));
		fields.add(String.valueOf(methodSmell.getEfferent()));
		fields.add(String.valueOf(methodSmell.getNumberOfParameters()));
		fields.add("true");
		fields.add(refactoring.getRefactoringType());
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalseNegative(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(methodNotSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(methodNotSmell.getCommit()));
		fields.add(refactoring.getFileNameAfter());
		fields.add(methodNotSmell.getClassDesignRole());
		fields.add(refactoring.getClassName());
		fields.add(refactoring.getMethodName());
		fields.add(String.valueOf(methodNotSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodNotSmell.getComplexity()));
		fields.add(String.valueOf(methodNotSmell.getEfferent()));
		fields.add(String.valueOf(methodNotSmell.getNumberOfParameters()));
		fields.add("true");
		fields.add(refactoring.getRefactoringType());
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalsePositive(MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(methodSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(methodSmell.getCommit()));
		fields.add(methodSmell.getDiretorioDaClasse());
		fields.add(methodSmell.getClassDesignRole());
		fields.add(methodSmell.getNomeClasse());
		fields.add(methodSmell.getNomeMetodo());
		fields.add(String.valueOf(methodSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodSmell.getComplexity()));
		fields.add(String.valueOf(methodSmell.getEfferent()));
		fields.add(String.valueOf(methodSmell.getNumberOfParameters()));
		fields.add("false");
		fields.add("");
		csvFile.writeNext(fields.toArray(new String[0]));
	}


	public void writeTrueNegative(MethodDataSmelly methodNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(methodNotSmell.getCommit()); 
		fields.add(this.commitRange.getCommitDateAsString(methodNotSmell.getCommit()));
		fields.add(methodNotSmell.getDiretorioDaClasse());
		fields.add(methodNotSmell.getClassDesignRole());
		fields.add(methodNotSmell.getNomeClasse());
		fields.add(methodNotSmell.getNomeMetodo());
		fields.add(String.valueOf(methodNotSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodNotSmell.getComplexity()));
		fields.add(String.valueOf(methodNotSmell.getEfferent()));
		fields.add(String.valueOf(methodNotSmell.getNumberOfParameters()));
		fields.add("false");
		fields.add("");
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void close() throws IOException {
		csvFile.close();
	}

}
