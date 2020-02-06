package org.smellrefactored.classes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.smellrefactored.CommitSmell;

public class CommitClassSmell {

	private CommitSmell commitSmell;
	
	public CommitClassSmell(CommitSmell commitSmell) {
		this.commitSmell = commitSmell;
	}

	public ArrayList<FilterSmellResult> getSmellsFromCommits(ArrayList<String> commitIds) throws Exception {
		return (this.commitSmell.getSmellsFromCommits(commitIds));
	}	
	
	public FilterSmellResult getSmellsFromCommit(String commitId) throws Exception {
		return (this.commitSmell.getSmellsFromCommit(commitId));
	}	
	
	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
		return (this.commitSmell.getTechniquesThresholds());
	}
	
	public boolean hasClassSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellsInCommit != null) {
			for(ClassDataSmelly smellInCommit: smellsInCommit.getClassesSmell() ) {
				if (smellInCommit.getSmell().contains(smellType)) {
					if ( (smellInCommit.getDiretorioDaClasse().equals(filePath))
							&& (smellInCommit.getNomeClasse().equals(className)) ) {
						if (smellInCommit.getListaTecnicas().contains(technique)) {
							result = true;
						}
					}
				}
			}
		}
		return (result);
	}
	
	public HashSet<ClassDataSmelly> getNotSmellingClassesBySmellAndTechnique(FilterSmellResult smellResult, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		result.addAll(smellResult.getClassesNotSmelly());
		for (ClassDataSmelly classSmelly : smellResult.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				if (!classSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(classSmelly);
				}
			}
		}
		return (result);
	}

	public ClassDataSmelly getSmellCommitForClass(String commitId, String filePath, String className, String smellType) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType);
		}
		return result;
	}
	
	public ClassDataSmelly getSmellCommitForClassFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String smellType) throws Exception {
		ClassDataSmelly result = null;
		for (ClassDataSmelly classSmell : smellResult.getClassesSmell()) {
			if (classSmell.getSmell().equals(smellType)) {
				if ( (classSmell.getDiretorioDaClasse().equals(filePath)) 
						&& classSmell.getNomeClasse().equals(className) ) {				
					result = classSmell;
					break;
				}
			}
		}
		return result;
	}

	public ClassDataSmelly getNotSmellCommitForClass(String commitId, String filePath, String className) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getNotSmellCommitForClassFromSmellResult(smellResult, filePath, className);
		}
		return result;
	}
	
	public ClassDataSmelly getNotSmellCommitForClassFromSmellResult(FilterSmellResult smellResult, String filePath, String className) throws Exception {
		ClassDataSmelly result = null;
		for (ClassDataSmelly classNotSmell : smellResult.getClassesNotSmelly()) {
			if ( (classNotSmell.getDiretorioDaClasse().equals(filePath)) 
					&& classNotSmell.getNomeClasse().equals(className) ) {				
				result = classNotSmell;
				break;
			}
		}
		return result;
	}

	public ClassDataSmelly getSmellOrNotSmellCommitForClass(String commitId, String filePath, String className, String smellType) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getSmellOrNotSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType);
		}
		return result;
	}

	public ClassDataSmelly getSmellOrNotSmellCommitForClassFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String smellType) throws Exception {
		ClassDataSmelly result = getSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType);
		if (result == null) { 
			result = getNotSmellCommitForClassFromSmellResult(smellResult, filePath, className);
		}
		return result;
	}
	
	public int countClassSmellPredictionForTechniqueInCommit(FilterSmellResult smellResult, String smellType, String technique) {
		HashSet<ClassDataSmelly> smellyClasses = getSmellingClassesBySmellAndTechnique(smellResult, smellType, technique);
		return (smellyClasses.size());
	}

	public HashSet<ClassDataSmelly> getSmellingClassesBySmellAndTechnique(FilterSmellResult smellResult, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		for (ClassDataSmelly classSmelly : smellResult.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				if (classSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(classSmelly);
				}
			}
		}
		return (result);
	}
	
	static public void consistClassNotSmelly(FilterSmellResult smellResult) throws Exception {
		for (ClassDataSmelly classSmelly : smellResult.getClassesSmell()) {
			for (ClassDataSmelly classNotSmelly : smellResult.getClassesNotSmelly()) {
				if (classSmelly.getCommit().equals(classNotSmelly.getCommit())
				    && classSmelly.getDiretorioDaClasse().equals(classNotSmelly.getDiretorioDaClasse())
			        && classSmelly.getNomeClasse().equals(classNotSmelly.getNomeClasse()) ) {
					throw new Exception("Class found in the list of smells and non-smells.:" + classSmelly.toString());
				}
			}
		}
	}

}
