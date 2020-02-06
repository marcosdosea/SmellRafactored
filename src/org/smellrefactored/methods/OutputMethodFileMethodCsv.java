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

public class OutputMethodFileMethodCsv {

	private CommitRange commitRange;
	private Set<String> techniques;
	private String baseFileName;
	
	private CSVWriter csvSmellRefactoredMethods;

	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputMethodFileMethodCsv(CommitRange commitRange, Set<String> techniques, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.techniques = techniques;
		this.baseFileName = baseFileName;
		
		csvSmellRefactoredMethods = new CSVWriter(new FileWriter(this.baseFileName + "-smellRefactored-methods.csv"));
	}
	
	public void writeHeader() {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("Class");
		fields.add("Method");
		fields.add("Smell");
		fields.add("LOC");
		fields.add("CC");
		fields.add("EC");
		fields.add("NOP");
		fields.add("Tecnicas");
		fields.add("Commit");
		fields.add("Refactoring");
		fields.add("Left Side");
		fields.add("Right Side");
		csvSmellRefactoredMethods.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeTruePositive(RefactoringEvent refactoring, MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(refactoring.getClassName());
		fields.add(refactoring.getMethodName());
		fields.add(methodSmell.getSmell());
		fields.add(String.valueOf(methodSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodSmell.getComplexity()));
		fields.add(String.valueOf(methodSmell.getEfferent()));
		fields.add(String.valueOf(methodSmell.getNumberOfParameters()));
		fields.add(methodSmell.getListaTecnicas() != null ? methodSmell.getListaTecnicas().toString() : null);
		fields.add(refactoring.getCommitId());
		fields.add(refactoring.getRefactoringType());
		fields.add(refactoring.getLeftSide());
		fields.add(refactoring.getRightSide());
		csvSmellRefactoredMethods.writeNext(fields.toArray(new String[0]));
	}
	
	public void writeFalseNegative(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(refactoring.getClassName());
		fields.add(refactoring.getMethodName());
		fields.add((methodNotSmell.getSmell() != null ? methodNotSmell.getSmell() : ""));
		fields.add(String.valueOf(methodNotSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodNotSmell.getComplexity()));
		fields.add(String.valueOf(methodNotSmell.getEfferent()));
		fields.add(String.valueOf(methodNotSmell.getNumberOfParameters()));
		fields.add(methodNotSmell.getListaTecnicas() != null ? methodNotSmell.getListaTecnicas().toString() : null);
		fields.add(refactoring.getCommitId());
		fields.add(refactoring.getRefactoringType());
		fields.add(refactoring.getLeftSide());
		fields.add(refactoring.getRightSide());
		csvSmellRefactoredMethods.writeNext(fields.toArray(new String[0]));
	}

	public void writeFalsePositive(MethodDataSmelly methodSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(methodSmell.getNomeClasse());
		fields.add(methodSmell.getNomeMetodo());
		fields.add(methodSmell.getSmell());
		fields.add(String.valueOf(methodSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodSmell.getComplexity()));
		fields.add(String.valueOf(methodSmell.getEfferent()));
		fields.add(String.valueOf(methodSmell.getNumberOfParameters()));
		fields.add(methodSmell.getListaTecnicas() != null ? methodSmell.getListaTecnicas().toString() : null);
		fields.add(methodSmell.getCommit());
		fields.add("");
		fields.add("");
		fields.add("");
		csvSmellRefactoredMethods.writeNext(fields.toArray(new String[0]));
	}

	public void writeTrueNegative(MethodDataSmelly methodNotSmell) {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add(methodNotSmell.getNomeClasse());
		fields.add(methodNotSmell.getNomeMetodo());
		fields.add(methodNotSmell.getSmell());
		fields.add(String.valueOf(methodNotSmell.getLinesOfCode()));
		fields.add(String.valueOf(methodNotSmell.getComplexity()));
		fields.add(String.valueOf(methodNotSmell.getEfferent()));
		fields.add(String.valueOf(methodNotSmell.getNumberOfParameters()));
		fields.add(methodNotSmell.getListaTecnicas() != null ? methodNotSmell.getListaTecnicas().toString() : null);
		fields.add(methodNotSmell.getCommit());
		fields.add("");
		fields.add("");
		fields.add("");
		csvSmellRefactoredMethods.writeNext(fields.toArray(new String[0]));
	}

	public void close() throws IOException {
		csvSmellRefactoredMethods.close();
	}

}
