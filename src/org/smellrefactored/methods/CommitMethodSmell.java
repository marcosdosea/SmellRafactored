package org.smellrefactored.methods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.smellrefactored.CommitSmell;

public class CommitMethodSmell {

	private CommitSmell commitSmell;
	
	public CommitMethodSmell(CommitSmell commitSmell) {
		this.commitSmell = commitSmell;
	}

	public ArrayList<FilterSmellResult> getSmellsFromCommits(ArrayList<String> commitIds) throws Exception {
		return (this.commitSmell.getSmellsFromCommits(commitIds));
	}	
	
	public FilterSmellResult getSmellsFromCommit(String commitId) throws Exception {
		return (this.commitSmell.getSmellsFromCommit(commitId));
	}	
	
	public FilterSmellResult getSmellsFromCommitSmellTechnique(String commitId, String smellType, String technique) throws Exception {
		FilterSmellResult result = new FilterSmellResult(commitId);
		HashSet<MethodDataSmelly> smells = new HashSet<MethodDataSmelly>();
		HashSet<MethodDataSmelly> notSmells = new HashSet<MethodDataSmelly>();
		FilterSmellResult smellsFromCommit = this.getSmellsFromCommit(commitId);
		for (MethodDataSmelly methodSmelly: smellsFromCommit.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				MethodDataSmelly localMethodSmelly = getNewMethodDataSmelly(methodSmelly);
				if (methodSmelly.getListaTecnicas().contains(technique)) {
					smells.add(localMethodSmelly);
				} else {
					localMethodSmelly.setSmell("");
					notSmells.add(localMethodSmelly);
				}
			}
		}
		for (MethodDataSmelly methodNotSmelly: smellsFromCommit.getMetodosNotSmelly()) {
			notSmells.add(getNewMethodDataSmelly(methodNotSmelly));
		}
		result.setMetodosSmell(smells);
		result.setMetodosNotSmelly(notSmells);
		return (result);
	}	

	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
		return (this.commitSmell.getTechniquesThresholds());
	}
	
	public boolean hasMethodSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className, String methodName) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellsInCommit != null) {
			for(MethodDataSmelly smellInCommit: smellsInCommit.getMetodosSmell() ) {
				if (	(smellInCommit.getSmell() != null)
						&& smellInCommit.getSmell().contains(smellType)
						&& smellInCommit.getListaTecnicas().contains(technique)
						&& smellInCommit.getDiretorioDaClasse().equals(filePath)
						&& smellInCommit.getNomeClasse().equals(className)
						&& smellInCommit.getNomeMetodo().equals(methodName) ) {
							result = true;
							break;
				}
			}
		}
		return (result);
	}

	public HashSet<MethodDataSmelly> getNotSmellingMethodsBySmellAndTechnique(FilterSmellResult smellResult, String smellType, String selectedTechnique) {
		HashSet<MethodDataSmelly> result =  new HashSet<MethodDataSmelly>();
		for (MethodDataSmelly methodNotSmelly : smellResult.getMetodosNotSmelly()) {
			result.add(getNewMethodDataSmelly(methodNotSmelly));
		}
		for (MethodDataSmelly methodSmelly : smellResult.getMetodosSmell()) {
			if (	(methodSmelly.getSmell() != null)
					&& methodSmelly.getSmell().equals(smellType)
					&& (!methodSmelly.getListaTecnicas().contains(selectedTechnique)) ) {
					MethodDataSmelly localMethodSmelly = getNewMethodDataSmelly(methodSmelly);
					localMethodSmelly.setSmell("");
					result.add(localMethodSmelly);
			}
		}
		return (result);
	}
	
	public MethodDataSmelly getSmellCommitForMethod(String commitId, String filePath, String className, String methodName, String smellType, String technique) throws Exception {
		MethodDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getSmellCommitForMethodFromSmellResult(smellResult, filePath, className, methodName, smellType, technique);
		}
		return result;
	}

	public MethodDataSmelly getSmellCommitForMethodFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String methodName, String smellType, String technique) throws Exception {
		MethodDataSmelly result = null;
		for (MethodDataSmelly methodSmell : smellResult.getMetodosSmell()) {
			if ( (methodSmell.getSmell() != null)
					&& methodSmell.getSmell().equals(smellType)
					&& methodSmell.getListaTecnicas().contains(technique)
					&& methodSmell.getDiretorioDaClasse().equals(filePath)
					&& methodSmell.getNomeClasse().equals(className) 
					&& methodSmell.getNomeMetodo().equals(methodName) ) {				
					result = getNewMethodDataSmelly(methodSmell);
					break;
			}
		}
		return result;
	}

	public MethodDataSmelly getNotSmellCommitForMethod(String commitId, String filePath, String className, String methodName, String smellType, String technique) throws Exception {
		MethodDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getNotSmellCommitForMethodFromSmellResult(smellResult, filePath, className, methodName, smellType, technique);
		}
		return result;
	}

	public MethodDataSmelly getNotSmellCommitForMethodFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String methodName, String smellType, String technique) throws Exception {
		MethodDataSmelly result = null;
		for (MethodDataSmelly methodNotSmell : smellResult.getMetodosNotSmelly()) {
			if ( (methodNotSmell.getDiretorioDaClasse().equals(filePath)) 
					&& methodNotSmell.getNomeClasse().equals(className) 
					&& methodNotSmell.getNomeMetodo().equals(methodName) ) {	
				result = getNewMethodDataSmelly(methodNotSmell);
				break;
			}
		}
		if (result == null) {
			for (MethodDataSmelly methodSmell : smellResult.getMetodosSmell()) {
				if ( (methodSmell.getDiretorioDaClasse().equals(filePath)) 
						&& methodSmell.getNomeClasse().equals(className) 
						&& methodSmell.getNomeMetodo().equals(methodName)
						&& methodSmell.getSmell().equals(smellType)
						&& (!methodSmell.getListaTecnicas().contains(technique)) 
						) {				
					MethodDataSmelly localMethodSmelly = getNewMethodDataSmelly(methodSmell);
					localMethodSmelly.setSmell("");
					result = localMethodSmelly;
					break;
				}
			}
		}
		if (result != null) {
			result.setSmell("");
		}
		return result;
	}

	public MethodDataSmelly getSmellOrNotSmellCommitForMethod(String commitId, String filePath, String className, String methodName, String smellType, String technique) throws Exception {
		MethodDataSmelly result = null;
		FilterSmellResult smellResult = this.commitSmell.getSmellsFromCommit(commitId);
		if (smellResult != null) {
			result = getSmellOrNotSmellCommitForMethodFromSmellResult(smellResult, filePath, className, methodName, smellType, technique);
		}
		return result;
	}

	public MethodDataSmelly getSmellOrNotSmellCommitForMethodFromSmellResult(FilterSmellResult smellResult, String filePath, String className, String methodName, String smellType, String technique) throws Exception {
		MethodDataSmelly result = getSmellCommitForMethodFromSmellResult(smellResult, filePath, className, methodName, smellType, technique);
		if (result == null) { 
			result = getNotSmellCommitForMethodFromSmellResult(smellResult, filePath, className, methodName, smellType, technique);
		}
		return result;
	}
	
	public int countMethodSmellPredictionForTechniqueInCommit(FilterSmellResult smellResult, String smellType, String technique) {
		HashSet<MethodDataSmelly> smellyMethods = getSmellingMethodsBySmellAndTechnique(smellResult, smellType, technique);
		return (smellyMethods.size());
	}
	
	public HashSet<MethodDataSmelly> getSmellingMethodsBySmellAndTechnique(FilterSmellResult smellResult, String smellType, String selectedTechnique) {
		HashSet<MethodDataSmelly> result =  new HashSet<MethodDataSmelly>();
		for (MethodDataSmelly methodSmelly : smellResult.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				if (methodSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(getNewMethodDataSmelly(methodSmelly));
				}
			}
		}
		return (result);
	}

	static public void consistMethodNotSmelly(FilterSmellResult smellResult) throws Exception {
		for (MethodDataSmelly methodSmelly : smellResult.getMetodosSmell()) {
			for (MethodDataSmelly methodNotSmelly : smellResult.getMetodosNotSmelly()) {
				if (methodSmelly.getCommit().equals(methodNotSmelly.getCommit())
					&& methodSmelly.getDiretorioDaClasse().equals(methodNotSmelly.getDiretorioDaClasse())
					&& methodSmelly.getNomeClasse().equals(methodNotSmelly.getNomeClasse())
					&& methodSmelly.getNomeMetodo().equals(methodNotSmelly.getNomeMetodo())
					&& methodSmelly.getCharInicial() == methodNotSmelly.getCharInicial()) {
					throw new Exception("Method found in the list of smells and non-smells:" + methodSmelly.toString());
				}
			}
		}
	}

	static private MethodDataSmelly getNewMethodDataSmelly(MethodDataSmelly originalObject) {
		MethodDataSmelly result = new MethodDataSmelly(); 
		result.setCommit(originalObject.getCommit());
		result.setDiretorioDaClasse(originalObject.getDiretorioDaClasse());
		result.setNomeClasse(originalObject.getNomeClasse());
		result.setNomeMetodo(originalObject.getNomeMetodo());
		result.setLinhaInicial(originalObject.getLinhaInicial());
		result.setLinesOfCode(originalObject.getLinesOfCode());
		result.setComplexity(originalObject.getComplexity());
		result.setNumberOfParameters(originalObject.getNumberOfParameters());
		result.setEfferent(originalObject.getEfferent());
		result.setCharInicial(originalObject.getCharInicial());
		result.setCharFinal(originalObject.getCharFinal());
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
