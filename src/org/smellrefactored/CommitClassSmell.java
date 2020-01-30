package org.smellrefactored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;

public class CommitClassSmell {

	private CommitSmell commitSmell;
	
	public CommitClassSmell(CommitSmell commitSmell) {
		this.commitSmell = commitSmell;
	}

	public ArrayList<FilterSmellResult> obterSmellsCommits(ArrayList<String> commitIds) throws Exception {
		return (this.commitSmell.obterSmellsCommits(commitIds));
	}	
	
	public FilterSmellResult obterSmellsCommit(String commitId) throws Exception {
		return (this.commitSmell.obterSmellsCommit(commitId));
	}	
	
	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
		return (this.commitSmell.getTechniquesThresholds());
	}
	
	public boolean hasClassSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.obterSmellsCommit(commitId);
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
	
	public HashSet<ClassDataSmelly> getNotSmellingClassesBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		result.addAll(commitInitial.getClassesNotSmelly());
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {
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
		FilterSmellResult smellsCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsCommit != null) {
			for (ClassDataSmelly classSmell : smellsCommit.getClassesSmell()) {
				if (classSmell.getSmell().equals(smellType)) {
					if ( (classSmell.getDiretorioDaClasse().equals(filePath)) 
							&& classSmell.getNomeClasse().equals(className) ) {				
						result = classSmell;
					}
				}
			}
		}
		return result;
	}
	
	public int countClassSmellPredictionForTechniqueInCommit(FilterSmellResult commitInitial, String smellType, String technique) {
		HashSet<ClassDataSmelly> smellyClasses = getSmellingClassesBySmellAndTechnique(commitInitial, smellType, technique);
		return (smellyClasses.size());
	}

	public HashSet<ClassDataSmelly> getSmellingClassesBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				if (classSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(classSmelly);
				}
			}
		}
		return (result);
	}
	
	static public void consistClassNotSmelly(FilterSmellResult smellsCommitInitial) throws Exception {
		for (ClassDataSmelly classSmelly : smellsCommitInitial.getClassesSmell()) {
			for (ClassDataSmelly classNotSmelly : smellsCommitInitial.getClassesNotSmelly()) {
				if (classSmelly.getDiretorioDaClasse().equals(classSmelly.getDiretorioDaClasse())
			        && classSmelly.getNomeClasse().equals(classNotSmelly.getNomeClasse())
					&& classSmelly.getCommit().equals(classNotSmelly.getCommit())) {
					throw new Exception("Class found in the list of smells and non-smells.:" + classSmelly.toString());
				}
			}
		}
	}

	
}
