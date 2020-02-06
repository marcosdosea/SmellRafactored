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

public class OutputClassFileClassMessageCsv {

	private CommitRange commitRange;
	// private Set<String> techniques;
	private String baseFileName;
	
	private CSVWriter csvSmellRefactoredClassesMessage;

	// static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputClassFileClassMessageCsv(CommitRange commitRange, Set<String> techniques, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		// this.techniques = techniques;
		this.baseFileName = baseFileName;
		
		csvSmellRefactoredClassesMessage = new CSVWriter(new FileWriter(this.baseFileName + "-smellRefactored-classes-message.csv"));
	}
	
	public void writeHeader() {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("Class");
		fields.add("Smell");
		fields.add("CLOC");
		fields.add("Tecnicas");
		fields.add("Commit");
		fields.add("Refactoring");
		fields.add("Left Side");
		fields.add("Right Side");
		fields.add("Full Message");
		csvSmellRefactoredClassesMessage.writeNext(fields.toArray(new String[0]));
	};

	public void writeTruePositive(RefactoringEvent refactoring, ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(refactoring.getClassName());
		fields.add(classSmell.getSmell());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add(classSmell.getListaTecnicas() != null ? classSmell.getListaTecnicas().toString() : null);
		fields.add(refactoring.getCommitId());
		fields.add(refactoring.getRefactoringType());
		fields.add(refactoring.getLeftSide());
		fields.add(refactoring.getRightSide());
		fields.add(refactoring.getCommitData().getFullMessage());
		csvSmellRefactoredClassesMessage.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalseNegative(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(refactoring.getClassName());
		fields.add((classNotSmell.getSmell() != null ? classNotSmell.getSmell() : ""));
		fields.add(String.valueOf(classNotSmell.getLinesOfCode()));
		fields.add(classNotSmell.getListaTecnicas() != null ? classNotSmell.getListaTecnicas().toString() : null);
		fields.add(refactoring.getCommitId());
		fields.add(refactoring.getRefactoringType());
		fields.add(refactoring.getLeftSide());
		fields.add(refactoring.getRightSide());
		fields.add(refactoring.getCommitData().getFullMessage());
		csvSmellRefactoredClassesMessage.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalsePositive(ClassDataSmelly classSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classSmell.getNomeClasse());
		fields.add(classSmell.getSmell());
		fields.add(String.valueOf(classSmell.getLinesOfCode()));
		fields.add(classSmell.getListaTecnicas() != null ? classSmell.getListaTecnicas().toString() : null);
		fields.add(classSmell.getCommit());
		fields.add("");
		fields.add("");
		fields.add("");
		fields.add("");
		csvSmellRefactoredClassesMessage.writeNext(fields.toArray(new String[0]));
	}

	public void writeTrueNegative(ClassDataSmelly classNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(classNotSmell.getNomeClasse());
		fields.add(classNotSmell.getSmell());
		fields.add(String.valueOf(classNotSmell.getLinesOfCode()));
		fields.add(classNotSmell.getListaTecnicas() != null ? classNotSmell.getListaTecnicas().toString() : null);
		fields.add(classNotSmell.getCommit());
		fields.add("");
		fields.add("");
		fields.add("");
		fields.add("");
		csvSmellRefactoredClassesMessage.writeNext(fields.toArray(new String[0]));
	}
	
	public void close() throws IOException {
		csvSmellRefactoredClassesMessage.close();
	}

}
