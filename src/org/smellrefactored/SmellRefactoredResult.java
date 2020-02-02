package org.smellrefactored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.MethodDataSmelly;

public class SmellRefactoredResult {
	private Map<String, List<RefactoringEvent>> listRefactoringsByMethodSmelly;
	private Map<String, List<RefactoringEvent>> listRefactoringsByMethodNotSmelly;
	private HashSet<MethodDataSmelly> methodSmellyInitialNotRefactored;
	private FilterSmellResult smellsCommit;
	
	private ArrayList<RefactoringEvent> listRefactoring;
	
	public HashSet<MethodDataSmelly> getMethodInitialSmellyNotRefactored() {
		return methodSmellyInitialNotRefactored;
	}

	public void setMethodSmellyInitialNotRefactored(HashSet<MethodDataSmelly> methodSmellyNotRefactored) {
		this.methodSmellyInitialNotRefactored = methodSmellyNotRefactored;
	}

	public ArrayList<RefactoringEvent> getListRefactoring() {
		return listRefactoring;
	}

	public void setListRefactoring(ArrayList<RefactoringEvent> listRefactoring) {
		this.listRefactoring = listRefactoring;
	}

	public Map<String, List<RefactoringEvent>> getListRefactoringsByMethodSmelly() {
		return listRefactoringsByMethodSmelly;
	}

	public void setListRefactoringsByMethodSmelly(Map<String, List<RefactoringEvent>> listRefactoringsByMethodSmelly) {
		this.listRefactoringsByMethodSmelly = listRefactoringsByMethodSmelly;
	}

	public Map<String, List<RefactoringEvent>> getListRefactoringsByMethodNotSmelly() {
		return listRefactoringsByMethodNotSmelly;
	}

	public void setListRefactoringsByMethodNotSmelly(
			Map<String, List<RefactoringEvent>> listRefactoringsByMethodNotSmelly) {
		this.listRefactoringsByMethodNotSmelly = listRefactoringsByMethodNotSmelly;
	}

	public FilterSmellResult getSmellsCommit() {
		return smellsCommit;
	}

	public void setSmellsCommit(FilterSmellResult smellsCommit) {
		this.smellsCommit = smellsCommit;
	}
}
