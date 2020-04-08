package org.smellrefactored.methods;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.refactoringminer.api.RefactoringType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.CommitRange;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.SmellRefactoredManager;

public class OutputMethodFileManager {

	private CommitRange commitRange;
	private String typeSmell;
	private HashSet<String> targetTefactoringTypes;
	private Set<String> techniques;
	private String baseFileName;
	private String suffixFileName;

	
	// private OutputMethodFileMethodCsv csvMethod;
	// private OutputMethodFileMethodMessageCsv csvMethodMessage;
	// private OutputMethodFileMachineLearningCsv csvMachineLearning;
	private OutputMethodFilePlotCsv csvPlotAllTechniques;
	/// private OutputMethodFilePlotCsv csvPlotTechnique;

	static private Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	public OutputMethodFileManager(CommitRange commitRange, String typeSmell, Set<String> techniques, HashSet<String> targetTefactoringTypes, String baseFileName) throws IOException {
		this.commitRange = commitRange;
		this.typeSmell = typeSmell;
		this.targetTefactoringTypes = targetTefactoringTypes;
		this.techniques = techniques;
		this.baseFileName = baseFileName + "-" + typeSmell.replace(" ", "_");
		
		
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
		this.suffixFileName = fileNamePart;
		
		List<String> techniquelist = new ArrayList<String>(this.techniques); 
        Collections.sort(techniquelist); 
		
		String baseOutputFileName = this.baseFileName  + "-" + techniquelist.toString().replace("[", "").replace("]", "").replace(",", "").replace(" ", "") + "-" + this.suffixFileName;
		csvPlotAllTechniques = new OutputMethodFilePlotCsv(this.commitRange, baseOutputFileName);
		csvPlotAllTechniques.setRecordRefactorings(true);

		// csvMethodMessage.writeHeader();
		// csvMethod.writeHeader();
		// csvMachineLearning.writeHeader();
		csvPlotAllTechniques.writeHeader();
	}
	
	public void beginTechnique(String technique) throws IOException {
		csvPlotAllTechniques.setTechnique(technique);
		String baseOutputFileName = this.baseFileName  + "-" + technique + "-" + this.suffixFileName;
		/// csvPlotTechnique = new OutputMethodFilePlotCsv(this.commitRange, baseOutputFileName);
		/// csvPlotTechnique.setTechnique(technique);
		/// csvPlotTechnique.setRecordRefactorings(true);
		/// csvPlotTechnique.writeHeader();
	}

	public void writeTruePositive(RefactoringEvent refactoring, MethodDataSmelly methodSmell) {
		// csvMethodMessage.writeTruePositive(refactoring, methodSmell);
		// csvMethod.writeTruePositive(refactoring, methodSmell);
		// csvMachineLearning.writeTruePositive(refactoring, methodSmell);
		/// csvPlotTechnique.writeTruePositive(refactoring, methodSmell);
		csvPlotAllTechniques.writeTruePositive(refactoring, methodSmell);
	}

	public void writeFalseNegative(RefactoringEvent refactoring, MethodDataSmelly methodNotSmell) {
		if (methodNotSmell == null) {
			logger.warn("Null response for querying non-smelling methods.");
		} else {
			// csvMethodMessage.writeFalseNegative(refactoring, methodNotSmell);
			// csvMethod.writeFalseNegative(refactoring, methodNotSmell);
			// csvMachineLearning.writeFalseNegative(refactoring, methodNotSmell);
			/// csvPlotTechnique.writeFalseNegative(refactoring, methodNotSmell);
			csvPlotAllTechniques.writeFalseNegative(refactoring, methodNotSmell);
		}
	}	

	public void writeFalsePositive(MethodDataSmelly methodSmell) {
		if (methodSmell == null) {
			logger.warn("Null response for querying non-smelling methods.");
		} else {
			// csvMethodMessage.writeFalsePositive(methodSmell);
			// csvMethod.writeFalsePositive(methodSmell);
			// csvMachineLearning.writeFalsePositive(methodSmell);
			/// csvPlotTechnique.writeFalsePositive(methodSmell);
			csvPlotAllTechniques.writeFalsePositive(methodSmell);
		}
	}
	
	public void writeIgnoredFalsePositive(MethodDataSmelly methodSmell) {
		if (methodSmell == null) {
			logger.warn("Null response for querying non-smelling methods.");
		} else {
			/// csvPlotTechnique.writeIgnoredFalsePositive(methodSmell);
			csvPlotAllTechniques.writeIgnoredFalsePositive(methodSmell);
		}
	}	
	
	public void writeTrueNegative(MethodDataSmelly methodNotSmell) {
		// csvMethodMessage.writeTrueNegative(methodNotSmell);
		// csvMethod.writeTrueNegative(methodNotSmell);
		// csvMachineLearning.writeTrueNegative(methodNotSmell);
		/// csvPlotTechnique.writeTrueNegative(methodNotSmell);
		csvPlotAllTechniques.writeTrueNegative(methodNotSmell);
	}
	
	public void endTechnique() throws IOException {
		/// csvPlotTechnique.close();
		csvPlotAllTechniques.setRecordRefactorings(false);
	}
	
	public void close() throws IOException {
		// csvMethodMessage.close();
		// csvMethod.close();
		// csvMachineLearning.close();
		csvPlotAllTechniques.close();
	}
	
}
