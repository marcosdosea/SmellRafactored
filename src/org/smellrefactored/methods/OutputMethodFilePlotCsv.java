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

import com.opencsv.CSVWriter;

public class OutputMethodFilePlotCsv {

	final String RECORD_TYPE_REFACTORING   = "Refactoring";
	final String RECORD_TYPE_SMELL         = "Smell";
	final String RECORD_TYPE_IGNORED_SMELL = "Ignored Smell";

	private CommitRange commitRange;
	private String baseFileName;
	
	private CSVWriter csvFile;

	// static private Logger logger = LoggerFactory.getLogger(OutputMethodFilePlotCsv.class);
	
	public OutputMethodFilePlotCsv(CommitRange commitRange, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.baseFileName = baseFileName;
		
		// csvFile = new CSVWriter(new FileWriter(this.baseFileName + "-" + this.commitRange.getInitialCommitId() + "-" + this.commitRange.getFinalCommitId() + "-methods-plot.csv"));
		csvFile = new CSVWriter(new FileWriter(this.baseFileName + "-methods-plot.csv"));
	}
	
	public void writeHeader() {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("commitId");
		fields.add("commitDateTime");
		fields.add("filePath");
		fields.add("designRole");
		fields.add("className");
		fields.add("methodName");
		fields.add("loc");
		fields.add("cc");
		fields.add("ec");
		fields.add("nop");
		fields.add("recordType");
		fields.add("entityName");
		fields.add("techniques");
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTruePositive(RefactoringEvent refactoring, MethodDataSmelly methodSmell) {
		this.writeTruePositiveSmell(methodSmell);
		this.writeTruePositiveRefactoring(refactoring, methodSmell);
	}

	private void writeTruePositiveSmell(MethodDataSmelly methodSmell) {
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
		fields.add(RECORD_TYPE_SMELL);
		fields.add(methodSmell.getSmell());
		fields.add(methodSmell.getListaTecnicas() != null ? methodSmell.getListaTecnicas().toString() : null);
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	private void writeTruePositiveRefactoring(RefactoringEvent refactoring, MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(refactoring.getCommitId());
		fields.add(this.commitRange.getCommitDateAsString(refactoring.getCommitId()));
		fields.add(refactoring.getFileNameAfter());
		fields.add(methodSmell.getClassDesignRole());
		fields.add(refactoring.getClassName());
		fields.add(refactoring.getMethodName());
		fields.add(String.valueOf(methodSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodSmell.getComplexity()));
		fields.add(String.valueOf(methodSmell.getEfferent()));
		fields.add(String.valueOf(methodSmell.getNumberOfParameters()));
		fields.add(RECORD_TYPE_REFACTORING);
		fields.add(refactoring.getRefactoringType());
		fields.add("");
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalseNegative(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		// Refactoring
		fields.add(refactoring.getCommitId());
		fields.add(this.commitRange.getCommitDateAsString(refactoring.getCommitId()));
		fields.add(refactoring.getFileNameAfter());
		fields.add(methodNotSmell.getClassDesignRole());
		fields.add(refactoring.getClassName());
		fields.add(refactoring.getMethodName());
		fields.add(String.valueOf(methodNotSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodNotSmell.getComplexity()));
		fields.add(String.valueOf(methodNotSmell.getEfferent()));
		fields.add(String.valueOf(methodNotSmell.getNumberOfParameters()));
		fields.add(RECORD_TYPE_REFACTORING);
		fields.add(refactoring.getRefactoringType());
		fields.add("");
		csvFile.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalsePositive(MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		// Smell
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
		fields.add(RECORD_TYPE_SMELL);
		fields.add(methodSmell.getSmell());
		fields.add(methodSmell.getListaTecnicas() != null ? methodSmell.getListaTecnicas().toString() : null);
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeIgnoredFalsePositive(MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		// Smell
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
		fields.add(RECORD_TYPE_IGNORED_SMELL);
		fields.add(methodSmell.getSmell());
		fields.add(methodSmell.getListaTecnicas() != null ? methodSmell.getListaTecnicas().toString() : null);
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTrueNegative(MethodDataSmelly methodNotSmell) {
		// Smell - do nothing... 
		// pmResultSmellRefactoredMethodsPlot.write();
	}

	public void close() throws IOException {
		csvFile.close();
	}

}
