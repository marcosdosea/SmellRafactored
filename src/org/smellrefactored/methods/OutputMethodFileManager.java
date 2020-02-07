package org.smellrefactored.methods;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.refactoringminer.api.RefactoringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.CommitRange;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.SmellRefactoredManager;
import org.smellrefactored.classes.OutputClassFilePlotCsv;

public class OutputMethodFileManager {

	private CommitRange commitRange;
	private String typeSmell;
	private String technique;
	private HashSet<String> targetTefactoringTypes;
	private Set<String> techniques;
	private String baseFileName;
	
	// private OutputMethodFileMethodCsv csvMethod;
	// private OutputMethodFileMethodMessageCsv csvMethodMessage;
	// private OutputMethodFileMachineLearningCsv csvMachineLearning;
	private OutputMethodFilePlotCsv csvPlot;

	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputMethodFileManager(CommitRange commitRange, String typeSmell, String technique, HashSet<String> targetTefactoringTypes, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.typeSmell = typeSmell;
		this.technique = technique;
		this.targetTefactoringTypes = targetTefactoringTypes;
		this.techniques = techniques;
		this.baseFileName = baseFileName;
		
		// csvMethod = new OutputMethodFileMethodCsv(this.commitRange, this.techniques, this.baseFileName);
		// csvMethodMessage = new OutputMethodFileMethodMessageCsv(this.commitRange, this.techniques, this.baseFileName);
		// csvMachineLearning = new OutputMethodFileMachineLearningCsv(this.commitRange, this.techniques, this.baseFileName);
		
		
		String fileNamePart = this.targetTefactoringTypes.toString().replace("[", "").replace("]", "").replace(",", "-").replace(" ", "");
		if (fileNamePart.length()>40) {
			fileNamePart = "";
			for (String refactoringType: targetTefactoringTypes) {
				if (fileNamePart.length()>0) {
					fileNamePart += "-"; 
				}
				fileNamePart += String.valueOf(RefactoringType.valueOf(refactoringType).ordinal());
			}
		}
		String baseOutputFileName =this.baseFileName + "-" + typeSmell.replace(" ", "_") + "-" + technique + "-" + fileNamePart;
		csvPlot = new OutputMethodFilePlotCsv(this.commitRange, baseOutputFileName);
	}
	
	public void writeHeaders() {
		// csvMethodMessage.writeHeader();
		// csvMethod.writeHeader();
		// csvMachineLearning.writeHeader();
		csvPlot.writeHeader();
		}
	

	public void writeTruePositive(RefactoringEvent refactoring, MethodDataSmelly methodSmell) {
		// csvMethodMessage.writeTruePositive(refactoring, methodSmell);
		// csvMethod.writeTruePositive(refactoring, methodSmell);
		// csvMachineLearning.writeTruePositive(refactoring, methodSmell);
		csvPlot.writeTruePositive(refactoring, methodSmell);
	}

	public void writeFalseNegative(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) {
		if (methodNotSmell == null) {
			logger.warn("Null response for querying non-smelling methods.");
		} else {
			// csvMethodMessage.writeFalseNegative(refactoring, methodNotSmell);
			// csvMethod.writeFalseNegative(refactoring, methodNotSmell);
			// csvMachineLearning.writeFalseNegative(refactoring, methodNotSmell);
			csvPlot.writeFalseNegative(refactoring, methodNotSmell);
		}
	}	

	public void writeFalsePositive(MethodDataSmelly methodSmell) {
		if (methodSmell == null) {
			logger.warn("Null response for querying non-smelling methods.");
		} else {
			// csvMethodMessage.writeFalsePositive(methodSmell);
			// csvMethod.writeFalsePositive(methodSmell);
			// csvMachineLearning.writeFalsePositive(methodSmell);
			csvPlot.writeFalsePositive(methodSmell);
		}
	}
	
	public void writeIgnoredFalsePositive(MethodDataSmelly methodSmell) {
		if (methodSmell == null) {
			logger.warn("Null response for querying non-smelling methods.");
		} else {
			csvPlot.writeIgnoredFalsePositive(methodSmell);
		}
	}	
	
	public void writeTrueNegative(MethodDataSmelly methodNotSmell) {
		// csvMethodMessage.writeTrueNegative(methodNotSmell);
		// csvMethod.writeTrueNegative(methodNotSmell);
		// csvMachineLearning.writeTrueNegative(methodNotSmell);
		csvPlot.writeTrueNegative(methodNotSmell);
	}
	
	public void close() throws IOException {
		// csvMethodMessage.close();
		// csvMethod.close();
		// csvMachineLearning.close();
		csvPlot.close();
	}
	
}
