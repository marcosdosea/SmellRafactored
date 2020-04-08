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

import com.opencsv.CSVWriter;

public class OutputClassFilePlotCsv {

	final String RECORD_TYPE_REFACTORING   = "Refactoring";
	final String RECORD_TYPE_SMELL         = "Smell";
	final String RECORD_TYPE_IGNORED_SMELL = "Ignored Smell";
	
	private CommitRange commitRange;
	private String baseFileName;
	private Boolean recordRefactorings;
	private String technique;
	
	private CSVWriter csvFile;

	// static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputClassFilePlotCsv(CommitRange commitRange, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.baseFileName = baseFileName;
		this.recordRefactorings = true; 
				
		// csvFile = new CSVWriter(new FileWriter(this.baseFileName + "-" + this.commitRange.getInitialCommitId() + "-" + this.commitRange.getFinalCommitId() + "-classes-plot.csv"));
		csvFile = new CSVWriter(new FileWriter(this.baseFileName + "-classes-plot.csv"));
	}
	
	public void  setTechnique(String technique) {
		this.technique = technique;
	}
	
	public void  setRecordRefactorings(Boolean recordRefactorings) {
		this.recordRefactorings = recordRefactorings;
	}
	
	public void writeHeader() {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("commitId");
		fields.add("commitDateTime");
		fields.add("filePath");
		fields.add("className");
		fields.add("designRole");
		fields.add("cloc");
		fields.add("recordType");
		fields.add("entityName");
		fields.add("technique");
		fields.add("techniques");
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTruePositive(RefactoringEvent refactoring, ClassDataSmelly classSmell) {
		this.writeTruePositiveSmell(classSmell);
		this.writeTruePositiveRefactoring(refactoring, classSmell);
	}

	public void writeTruePositiveSmell(ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(classSmell.getCommit()));
		fields.add(classSmell.getDiretorioDaClasse());
		fields.add(classSmell.getNomeClasse());
		fields.add(classSmell.getClassDesignRole());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add(RECORD_TYPE_SMELL);
		fields.add(classSmell.getSmell());
		fields.add(this.technique);
		fields.add(classSmell.getListaTecnicas() != null ? classSmell.getListaTecnicas().toString() : null);
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTruePositiveRefactoring(RefactoringEvent refactoring, ClassDataSmelly classSmell) {
		if (this.recordRefactorings) {
			ArrayList<String> fields = new ArrayList<String>();
			fields.add(refactoring.getCommitId());
			fields.add(this.commitRange.getCommitDateAsString(refactoring.getCommitId()));
			fields.add(refactoring.getFileNameAfter());
			fields.add(refactoring.getClassName());
			fields.add(classSmell.getClassDesignRole());
			fields.add(String.valueOf(classSmell.getLinesOfCode()));
			fields.add(RECORD_TYPE_REFACTORING);
			fields.add(refactoring.getRefactoringType());
			fields.add("");
			fields.add("");
			csvFile.writeNext(fields.toArray(new String[0]));
		}
	}

	public void writeFalseNegative(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) {
		if (this.recordRefactorings) {
			ArrayList<String> fields = new ArrayList<String>();
			// Refactoring
			fields.add(refactoring.getCommitId());
			fields.add(this.commitRange.getCommitDateAsString(refactoring.getCommitId()));
			fields.add(refactoring.getFileNameAfter());
			fields.add(refactoring.getClassName());
			fields.add(classNotSmell.getClassDesignRole());
			fields.add(String.valueOf(classNotSmell.getLinesOfCode()));
			fields.add(RECORD_TYPE_REFACTORING);
			fields.add(refactoring.getRefactoringType());
			fields.add("");
			fields.add("");
			csvFile.writeNext(fields.toArray(new String[0]));
		}
	}
	
	public void writeFalsePositive(ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		// Smell
		fields.add(classSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(classSmell.getCommit()));
		fields.add(classSmell.getDiretorioDaClasse());
		fields.add(classSmell.getNomeClasse());
		fields.add(classSmell.getClassDesignRole());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add(RECORD_TYPE_SMELL);
		fields.add(classSmell.getSmell());
		fields.add(this.technique);
		fields.add(classSmell.getListaTecnicas() != null ? classSmell.getListaTecnicas().toString() : null);
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeIgnoredFalsePositive(ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		// Smell
		fields.add(classSmell.getCommit());
		fields.add(this.commitRange.getCommitDateAsString(classSmell.getCommit()));
		fields.add(classSmell.getDiretorioDaClasse());
		fields.add(classSmell.getNomeClasse());
		fields.add(classSmell.getClassDesignRole());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add(RECORD_TYPE_IGNORED_SMELL);
		fields.add(classSmell.getSmell());
		fields.add(this.technique);
		fields.add(classSmell.getListaTecnicas() != null ? classSmell.getListaTecnicas().toString() : null);
		csvFile.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTrueNegative(ClassDataSmelly classNotSmell) {
		// Smell - do nothing... 
		// pmResultSmellRefactoredClassesPlot.write();
	}

	public void close() throws IOException {
		csvFile.close();
	}

}
