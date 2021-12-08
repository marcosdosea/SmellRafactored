package org.smellrefactored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.contextsmell.FilterSmellResult;
import org.contextsmell.MethodDataSmelly;

public class SmellRefactoredResult {
	private Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly;
	private Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly;
	private HashSet<MethodDataSmelly> methodSmellyInitialNotRefactored;
	private FilterSmellResult smellsCommitInitial;

	private ArrayList<RefactoringData> listRefactoring;

	public HashSet<MethodDataSmelly> getMethodInitialSmellyNotRefactored() {
		return methodSmellyInitialNotRefactored;
	}

	public void setMethodSmellyInitialNotRefactored(HashSet<MethodDataSmelly> methodSmellyNotRefactored) {
		this.methodSmellyInitialNotRefactored = methodSmellyNotRefactored;
	}

	public ArrayList<RefactoringData> getListRefactoring() {
		return listRefactoring;
	}

	public void setListRefactoring(ArrayList<RefactoringData> listRefactoring) {
		this.listRefactoring = listRefactoring;
	}

	public Map<String, List<RefactoringData>> getListRefactoringsByMethodSmelly() {
		return listRefactoringsByMethodSmelly;
	}

	public void setListRefactoringsByMethodSmelly(Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly) {
		this.listRefactoringsByMethodSmelly = listRefactoringsByMethodSmelly;
	}

	public Map<String, List<RefactoringData>> getListRefactoringsByMethodNotSmelly() {
		return listRefactoringsByMethodNotSmelly;
	}

	public void setListRefactoringsByMethodNotSmelly(
			Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly) {
		this.listRefactoringsByMethodNotSmelly = listRefactoringsByMethodNotSmelly;
	}

	public FilterSmellResult getSmellsCommitInitial() {
		return smellsCommitInitial;
	}

	public void setSmellsCommitInitial(FilterSmellResult smellsCommitInitial) {
		this.smellsCommitInitial = smellsCommitInitial;
	}
}
