package org.smellrefactored.classes;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.CommitRange;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.SmellRefactoredManager;

import com.opencsv.CSVWriter;

public class OutputClassFileMachineLearningCsv {

	private CommitRange commitRange;
	private Set<String> techniques;
	private String baseFileName;
	
	private CSVWriter csvFile;
	
	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputClassFileMachineLearningCsv(CommitRange commitRange, Set<String> techniques, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.techniques = techniques;
		this.baseFileName = baseFileName;
		
		csvFile = new CSVWriter(new FileWriter(this.baseFileName + "-refactoredAndNotRefactored-classes-machineLearning.csv"));
	}
	
	public void writeHeader() {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("commitId");
		fields.add("commitDate");
		fields.add("filePath");
		fields.add("className");
		fields.add("designRole");
		fields.add("cloc");
		fields.add("isRefactoring");
		fields.add("refactoring");
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTruePositive(RefactoringEvent refactoring, ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(classSmell.getCommit()));
		fields.add(refactoring.getFileNameAfter());
		fields.add(refactoring.getClassName());
		fields.add(classSmell.getClassDesignRole());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add("true");
		fields.add(refactoring.getRefactoringType());
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalseNegative(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classNotSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(classNotSmell.getCommit()));
		fields.add(refactoring.getFileNameAfter());
		fields.add(refactoring.getClassName());
		fields.add(classNotSmell.getClassDesignRole());
		fields.add(String.valueOf(classNotSmell.getLinesOfCode()));
		fields.add("true");
		fields.add(refactoring.getRefactoringType());
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalsePositive(ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(classSmell.getCommit()));
		fields.add(classSmell.getDiretorioDaClasse());
		fields.add(classSmell.getNomeClasse());
		fields.add(classSmell.getClassDesignRole());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add("false");
		fields.add("");
		csvFile.writeNext(fields.toArray(new String[0]));
	}


	public void writeTrueNegative(ClassDataSmelly classNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classNotSmell.getCommit()); 
		fields.add(this.commitRange.getCommitDateAsString(classNotSmell.getCommit()));
		fields.add(classNotSmell.getDiretorioDaClasse());
		fields.add(classNotSmell.getNomeClasse());
		fields.add(classNotSmell.getClassDesignRole());
		fields.add(String.valueOf(classNotSmell.getLinesOfCode()));
		fields.add("false");
		fields.add("");
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void close() throws IOException {
		csvFile.close();
	}

}
