package org.smellrefactored.classes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.refactoringminer.api.RefactoringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.CommitRange;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.SmellRefactoredManager;

public class OutputClassFileManager {

	private CommitRange commitRange;
	private String typeSmell;
	private HashSet<String> targetTefactoringTypes;
	private Set<String> techniques;
	private String baseFileName;
	private String suffixFileName;
	
	// private OutputClassFileClassCsv csvClasses;
	// private OutputClassFileClassMessageCsv csvClassMessage;
	// private OutputClassFileMachineLearningCsv csvMachineLearning;
	private OutputClassFilePlotCsv csvPlotAllTechniques;
	/// private OutputClassFilePlotCsv csvPlotTechnique;
	
	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputClassFileManager(CommitRange commitRange, String typeSmell, Set<String> techniques, HashSet<String> targetTefactoringTypes, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.typeSmell = typeSmell;
		this.targetTefactoringTypes = targetTefactoringTypes;
		this.techniques = techniques;
		this.baseFileName = baseFileName + "-" + typeSmell.replace(" ", "_");
		
		// csvClasses = new OutputClassFileClassCsv(this.commitRange, this.techniques, this.baseFileName);
		// csvClassMessage = new OutputClassFileClassMessageCsv(this.commitRange, this.techniques, this.baseFileName);
		// csvMachineLearning = new OutputClassFileMachineLearningCsv(this.commitRange, this.techniques, this.baseFileName);
		
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
		this.suffixFileName = fileNamePart;
		
		List<String> techniquelist = new ArrayList<String>(this.techniques); 
        Collections.sort(techniquelist); 
		
		String baseOutputFileName = this.baseFileName  + "-" + techniquelist.toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", "") + "-" + this.suffixFileName;
		csvPlotAllTechniques = new OutputClassFilePlotCsv(this.commitRange, baseOutputFileName);
		csvPlotAllTechniques.setRecordRefactorings(true);
		
		// csvClassMessage.writeHeader();
		// csvClasses.writeHeader();
		// csvMachineLearning.writeHeader();
		csvPlotAllTechniques.writeHeader();
	}
	
	public void beginTechnique(String technique) throws IOException {
		csvPlotAllTechniques.setTechnique(technique);
		String baseOutputFileName = this.baseFileName  + "-" + technique + "-" + this.suffixFileName;
		/// csvPlotTechnique = new OutputClassFilePlotCsv(this.commitRange, baseOutputFileName);
		/// csvPlotTechnique.setTechnique(technique);
		/// csvPlotTechnique.setRecordRefactorings(true);
		/// csvPlotTechnique.writeHeader();
	}

	public void writeTruePositive(RefactoringEvent refactoring, ClassDataSmelly classSmell) {
		// csvClassMessage.writeTruePositive(refactoring, classSmell);
		// csvClasses.writeTruePositive(refactoring, classSmell);
		// csvMachineLearning.writeTruePositive(refactoring, classSmell);
		/// csvPlotTechnique.writeTruePositive(refactoring, classSmell);
		csvPlotAllTechniques.writeTruePositive(refactoring, classSmell);
	}

	public void writeFalseNegative(RefactoringEvent refactoring, ClassDataSmelly classNotSmell) {
		if (classNotSmell == null) {
			logger.warn("Null response for querying non-smelling classes.");
			csvPlotAllTechniques.writeFalseNegative(refactoring, new ClassDataSmelly());
		} else {
			// csvClassMessage.writeFalseNegative(refactoring, classNotSmell);
			// csvClasses.writeFalseNegative(refactoring, classNotSmell);
			// csvMachineLearning.writeFalseNegative(refactoring, classNotSmell);
			/// csvPlotTechnique.writeFalseNegative(refactoring, classNotSmell);
			csvPlotAllTechniques.writeFalseNegative(refactoring, classNotSmell);
		}
	}	

	public void writeFalsePositive(ClassDataSmelly classSmell) {
		if (classSmell == null) {
			logger.warn("Null response for querying non-smelling classes.");
		} else {
			// csvClassMessage.writeFalsePositive(classSmell);
			// csvClasses.writeFalsePositive(classSmell);
			// csvMachineLearning.writeFalsePositive(classSmell);
			/// csvPlotTechnique.writeFalsePositive(classSmell);
			csvPlotAllTechniques.writeFalsePositive(classSmell);
		}
	}
	
	public void writeIgnoredFalsePositive(ClassDataSmelly classSmell) {
		if (classSmell == null) {
			logger.warn("Null response for querying non-smelling classes.");
		} else {
			/// csvPlotTechnique.writeIgnoredFalsePositive(classSmell);
			csvPlotAllTechniques.writeIgnoredFalsePositive(classSmell);
		}
	}
		
	public void writeTrueNegative(ClassDataSmelly classNotSmell) {
		// csvClassMessage.writeTrueNegative(classNotSmell);
		// csvClasses.writeTrueNegative(classNotSmell);
		// csvMachineLearning.writeTrueNegative(classNotSmell);
		/// csvPlotTechnique.writeTrueNegative(classNotSmell);
		csvPlotAllTechniques.writeTrueNegative(classNotSmell);
	}

	public void endTechnique() throws IOException {
		/// csvPlotTechnique.close();
		csvPlotAllTechniques.setRecordRefactorings(false);
	}
	
	public void close() throws IOException {
		// csvClassMessage.close();
		// csvClasses.close();
		// csvMachineLearning.close();
		csvPlotAllTechniques.close();
	}

}
