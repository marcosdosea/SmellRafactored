package org.smellrefactored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.smelldetector.model.MethodDataSmelly;

public class CommitMethodSmell {

	private CommitSmell commitSmell;
	
	public CommitMethodSmell(CommitSmell commitSmell) {
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
	public boolean hasMethodSmellPredictionForTechniqueInCommit(String commitId, String smellType, String technique, String filePath, String className, String methodName) throws Exception {
		boolean result = false;
		FilterSmellResult smellsInCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsInCommit != null) {
			for(MethodDataSmelly smellInCommit: smellsInCommit.getMetodosSmell() ) {
				if (smellInCommit.getSmell().contains(smellType)) {
					if ( (smellInCommit.getDiretorioDaClasse().equals(filePath))
							&& (smellInCommit.getNomeClasse().equals(className))
							&& (smellInCommit.getNomeMetodo().equals(methodName)) ) {
						if (smellInCommit.getListaTecnicas().contains(technique)) {
							result = true;
						}
					}
				}
			}
		}
		return (result);
	}

	public HashSet<MethodDataSmelly> getSmellingMethodsBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<MethodDataSmelly> result =  new HashSet<MethodDataSmelly>();
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				if (methodSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(methodSmelly);
				}
			}
		}
		return (result);
	}

	public HashSet<MethodDataSmelly> getNotSmellingMethodsBySmellAndTechnique(FilterSmellResult commitInitial, String smellType, String selectedTechnique) {
		HashSet<MethodDataSmelly> result =  new HashSet<MethodDataSmelly>();
		result.addAll(commitInitial.getMetodosNotSmelly());
		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {
			if (methodSmelly.getSmell().equals(smellType)) {
				if (!methodSmelly.getListaTecnicas().contains(selectedTechnique)) {
					result.add(methodSmelly);
				}
			}
		}
		return (result);
	}
	
	public MethodDataSmelly getSmellCommitForMethod(String commitId, String filePath, String className, String methodName, String smellType) throws Exception {
		MethodDataSmelly result = null;
		FilterSmellResult smellsCommit = this.commitSmell.obterSmellsCommit(commitId);
		if (smellsCommit != null) {
			for (MethodDataSmelly methodSmell : smellsCommit.getMetodosSmell()) {
				if (methodSmell.getSmell().equals(smellType)) {
					if ( (methodSmell.getDiretorioDaClasse().equals(filePath)) 
							&& methodSmell.getNomeClasse().equals(className) 
							&& methodSmell.getNomeMetodo().equals(methodName) ) {				
						result = methodSmell;
					}
				}
			}
		}
		return result;
	}

	public int countMethodSmellPredictionForTechniqueInCommit(FilterSmellResult commitInitial, String smellType, String technique) {
		HashSet<MethodDataSmelly> smellyMethods = getSmellingMethodsBySmellAndTechnique(commitInitial, smellType, technique);
		return (smellyMethods.size());
	}
	
	static public void consistMethodNotSmelly(FilterSmellResult smellsCommitInitial) throws Exception {
		for (MethodDataSmelly methodSmelly : smellsCommitInitial.getMetodosSmell()) {
			for (MethodDataSmelly methodNotSmelly : smellsCommitInitial.getMetodosNotSmelly()) {
				if (methodSmelly.getDiretorioDaClasse().equals(methodNotSmelly.getDiretorioDaClasse())
					&& methodSmelly.getNomeClasse().equals(methodNotSmelly.getNomeClasse())
					&& methodSmelly.getNomeMetodo().equals(methodNotSmelly.getNomeMetodo())
					&& methodSmelly.getCommit().equals(methodNotSmelly.getCommit())
					&& methodSmelly.getCharInicial() == methodNotSmelly.getCharInicial()) {
					throw new Exception("Method found in the list of smells and non-smells:" + methodSmelly.toString());
				}
			}
		}
	}
	
}
