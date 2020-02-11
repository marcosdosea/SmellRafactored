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
	
	public FilterSmellResult getSmellsFromCommitSmellTypeTechnique(String commitId, String smellType, String technique) throws Exception {
		FilterSmellResult result = new FilterSmellResult(commitId);
		HashSet<ClassDataSmelly> smells = new HashSet<ClassDataSmelly>();
		HashSet<ClassDataSmelly> notSmells = new HashSet<ClassDataSmelly>();
		FilterSmellResult smellsFromCommit = this.getSmellsFromCommit(commitId);
		for (ClassDataSmelly classSmelly: smellsFromCommit.getClassesSmell()) {
			if (classSmelly.getSmell().equals(smellType)) {
				ClassDataSmelly localClassSmelly = getNewClassDataSmelly(classSmelly);
				if (classSmelly.getListaTecnicas().contains(technique)) {
					smells.add(localClassSmelly);
				} else {
					localClassSmelly.setSmell("");
					notSmells.add(localClassSmelly);
				}
			}
		}
		for (ClassDataSmelly classNotSmelly: smellsFromCommit.getClassesNotSmelly()) {
			ClassDataSmelly localClassNotSmelly = getNewClassDataSmelly(classNotSmelly);
			notSmells.add(localClassNotSmelly);
		}
		result.setClassesSmell(smells);
		result.setClassesNotSmelly(notSmells);
		return (result);
	}	

	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
		return (this.commitSmell.getTechniquesThresholds());
	}
	
	public boolean hasClassSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellsInCommit != null) {
			for(ClassDataSmelly smellInCommit: smellsInCommit.getClassesSmell() ) {
				if ( 	(smellInCommit.getSmell() != null)
						&& smellInCommit.getSmell().contains(smellType)
						&& smellInCommit.getListaTecnicas().contains(technique)
						&& smellInCommit.getDiretorioDaClasse().equals(filePath)
						&& smellInCommit.getNomeClasse().equals(className)
						) {
							result = true;
							break;
				}
			}
		}
		return (result);
	}
	
	public HashSet<ClassDataSmelly> getNotSmellingClassesBySmellAndTechnique(FilterSmellResult smellResult, String smellType, String selectedTechnique) {
		HashSet<ClassDataSmelly> result =  new HashSet<ClassDataSmelly>();
		for (ClassDataSmelly classNotSmelly : smellResult.getClassesNotSmelly()) {
			result.add(getNewClassDataSmelly(classNotSmelly));
		}
		for (ClassDataSmelly classSmelly : smellResult.getClassesSmell()) {
			if (	(classSmelly.getSmell() != null)
					&& classSmelly.getSmell().equals(smellType)
					&& (!classSmelly.getListaTecnicas().contains(selectedTechnique)) ) {
				ClassDataSmelly localClassSmelly = getNewClassDataSmelly(classSmelly);
				localClassSmelly.setSmell("");
				result.add(localClassSmelly);
			}
		}
		return (result);
	}

	public ClassDataSmelly getSmellCommitForClass(String commitId, String filePath, String className, String smellType, String technique) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType, technique);
		}
		return result;
	}
	
	public ClassDataSmelly getSmellCommitForClassFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String smellType, String technique) throws Exception {
		ClassDataSmelly result = null;
		for (ClassDataSmelly classSmell : smellResult.getClassesSmell()) {
			if (	(classSmell.getSmell() != null)
					&& classSmell.getSmell().equals(smellType)
					&& classSmell.getListaTecnicas().contains(technique)
					&& classSmell.getDiretorioDaClasse().equals(filePath) 
					&& classSmell.getNomeClasse().equals(className) ) {				
					result = getNewClassDataSmelly(classSmell);
					break;
			}
		}
		return result;
	}

	public ClassDataSmelly getNotSmellCommitForClass(String commitId, String filePath, String className, String smellType, String technique) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getNotSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType, technique);
		}
		return result;
	}
	
	public ClassDataSmelly getNotSmellCommitForClassFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String smellType, String technique) throws Exception {
		ClassDataSmelly result = null;
		for (ClassDataSmelly classNotSmell : smellResult.getClassesNotSmelly()) {
			if ( (classNotSmell.getDiretorioDaClasse().equals(filePath)) 
					&& classNotSmell.getNomeClasse().equals(className) ) {				
				result = getNewClassDataSmelly(classNotSmell);
				break;
			}
		}
		if (result == null) {
			for (ClassDataSmelly classSmell : smellResult.getClassesSmell()) {
				if ( (classSmell.getDiretorioDaClasse().equals(filePath)) 
						&& classSmell.getNomeClasse().equals(className) 
						&& classSmell.getSmell().equals(smellType)
						&& (!classSmell.getListaTecnicas().contains(technique)) 
						) {			
					ClassDataSmelly localClassSmelly = getNewClassDataSmelly(classSmell);
					localClassSmelly.setSmell("");
					result = localClassSmelly;
					break;
				}
			}
		}
		if (result != null) {
			result.setSmell("");
		}
		return result;
	}

	public ClassDataSmelly getSmellOrNotSmellCommitForClass(String commitId, String filePath, String className, String smellType, String technique) throws Exception {
		ClassDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getSmellOrNotSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType, technique);
		}
		return result;
	}

	public ClassDataSmelly getSmellOrNotSmellCommitForClassFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String smellType, String technique) throws Exception {
		ClassDataSmelly result = getSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType, technique);
		if (result == null) { 
			result = getNotSmellCommitForClassFromSmellResult(smellResult, filePath, className, smellType, technique);
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
					result.add(getNewClassDataSmelly(classSmelly));
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

	static private ClassDataSmelly getNewClassDataSmelly(ClassDataSmelly originalObject) {
		ClassDataSmelly result = new ClassDataSmelly(); 
		result.setCommit(originalObject.getCommit());
		result.setDiretorioDaClasse(originalObject.getDiretorioDaClasse());
		result.setNomeClasse(originalObject.getNomeClasse());
		result.setLinesOfCode(originalObject.getLinesOfCode());
		result.setClassDesignRole(originalObject.getClassDesignRole());
		if (originalObject.getListaTecnicas() != null) {
			for (String technique: originalObject.getListaTecnicas()) {
				result.addTecnica(technique);
			}
		}
		if (originalObject.getListaMensagens() != null) {
			for (String mensagem: originalObject.getListaMensagens()) {
				result.addMensagem(mensagem);
			}
		}
		result.setSmell(originalObject.getSmell());
		return (result);
	}

}
