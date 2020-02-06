package org.smellrefactored.classes;

import java.io.IOException;
import java.util.Set;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.CommitRange;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.SmellRefactoredManager;

public class OutputClassFileManager {

	private CommitRange commitRange;
	private Set<String> techniques;
	private String baseFileName;
	
	private OutputClassFileClassCsv csvClasses;
	private OutputClassFileClassMessageCsv csvClassMessage;
	private OutputClassFileMachineLearningCsv csvMachineLearning;
	private OutputClassFilePlotCsv csvPlot;

	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputClassFileManager(CommitRange commitRange, Set<String> techniques, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.techniques = techniques;
		this.baseFileName = baseFileName;
		
		csvClasses = new OutputClassFileClassCsv(this.commitRange, this.techniques, this.baseFileName);
		csvClassMessage = new OutputClassFileClassMessageCsv(this.commitRange, this.techniques, this.baseFileName);
		csvMachineLearning = new OutputClassFileMachineLearningCsv(this.commitRange, this.techniques, this.baseFileName);
		csvPlot = new OutputClassFilePlotCsv(this.commitRange, this.techniques, this.baseFileName);
	}
	
	public void writeHeaders() {
		csvClassMessage.writeHeader();
		csvClasses.writeHeader();
		csvMachineLearning.writeHeader();
		csvPlot.writeHeader();
		}
	

	public void writeTruePositive(RefactoringEvent refactoring, ClassDataSmelly classSmell) {
		csvClassMessage.writeTruePositive(refactoring, classSmell);
		csvClasses.writeTruePositive(refactoring, classSmell);
		csvMachineLearning.writeTruePositive(refactoring, classSmell);
		csvPlot.writeTruePositive(refactoring, classSmell);
	}

	public void writeFalseNegative(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) {
		if (classNotSmell == null) {
			logger.warn("Null response for querying non-smelling classes.");
		} else {
			csvClassMessage.writeFalseNegative(refactoring, classNotSmell);
			csvClasses.writeFalseNegative(refactoring, classNotSmell);
			csvMachineLearning.writeFalseNegative(refactoring, classNotSmell);
			csvPlot.writeFalseNegative(refactoring, classNotSmell);
		}
	}	

	public void writeFalsePositive(ClassDataSmelly classSmell) {
		if (classSmell == null) {
			logger.warn("Null response for querying non-smelling classes.");
		} else {
			csvClassMessage.writeFalsePositive(classSmell);
			csvClasses.writeFalsePositive(classSmell);
			csvMachineLearning.writeFalsePositive(classSmell);
			csvPlot.writeFalsePositive(classSmell);
		}
	}
	
	public void writeTrueNegative(ClassDataSmelly classNotSmell) {
		csvClassMessage.writeTrueNegative(classNotSmell);
		csvClasses.writeTrueNegative(classNotSmell);
		csvMachineLearning.writeTrueNegative(classNotSmell);
		csvPlot.writeTrueNegative(classNotSmell);
	}
	
	public void close() throws IOException {
		csvClassMessage.close();
		csvClasses.close();
		csvMachineLearning.close();
		csvPlot.close();
	}

}
