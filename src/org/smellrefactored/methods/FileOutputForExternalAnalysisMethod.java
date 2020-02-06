package org.smellrefactored.methods;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.designroleminer.smelldetector.model.MethodDataSmelly;

import com.opencsv.CSVWriter;

public class FileOutputForExternalAnalysisMethod {
	
	private String[] refactoringTypes;
	private String[] techniques;
	private String resultBaseFileName;
	
	private HashSet<String> processedCommits = new HashSet<String>();
	
	private FileWriter fileHandler;
	private CSVWriter csv;
	
	public FileOutputForExternalAnalysisMethod(String[] refactoringTypes, String[] techniques, String resultBaseFileName) throws IOException {
		this.refactoringTypes = refactoringTypes;
		this.techniques = techniques;
		this.resultBaseFileName = resultBaseFileName;

		FileWriter fileHandler = new FileWriter(this.resultBaseFileName + "-method-forExternalAnalises.csv");
		csv = new CSVWriter(fileHandler);
	}

	public void writeCsvHeader() {
		ArrayList<String> header = new ArrayList<String>();
		header.add("commitId");
		header.add("designRole");
		header.add("filePath");
		header.add("className");
		header.add("methodName");
		header.add("loc");
		header.add("cc");
		header.add("ec");
		header.add("nop");
		for (String technique : techniques) {
			header.add(technique);
		}
		header.add("refactored");
		for (String refactoringType : refactoringTypes) {
			header.add(refactoringType);
		}
		csv.writeNext((String[]) header.toArray());
	}
	
	public void writeCsvLine(MethodDataSmelly methodDataSmelly, String[] refactorings) {
		if (!processedCommits.contains(methodDataSmelly.getCommit())) {
			processedCommits.add(methodDataSmelly.getCommit());
			ArrayList<String> line = new ArrayList<String>();
			line.add(methodDataSmelly.getCommit());
			line.add(methodDataSmelly.getClassDesignRole());
			line.add(methodDataSmelly.getDiretorioDaClasse());
			line.add(methodDataSmelly.getNomeClasse());
			line.add(methodDataSmelly.getNomeMetodo());
			line.add(Integer.toString(methodDataSmelly.getLinesOfCode()));
			for (String technique : techniques) {
				line.add( (methodDataSmelly.getListaTecnicas().contains(technique)) ? "T" :  "F");
			}
			line.add( ((refactorings != null) && (refactorings.length>0) )  ? "T" :  "F");
			for (String refactoringType : refactoringTypes) {
				line.add( Arrays.asList(refactorings).contains(refactoringType) ? "T" :  "F");
			}
			csv.writeNext((String[]) line.toArray());
		}
	}
	
	public void close() throws IOException {
		csv.close();
		fileHandler.close();
	}
	
}
