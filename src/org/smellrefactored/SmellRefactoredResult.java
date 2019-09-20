package org.smellrefactored;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.designroleminer.smelldetector.model.FilterSmellResult;

public class SmellRefactoredResult {
	private FilterSmellResult smellsInitial;
	private FilterSmellResult smellsFinal;
	private Map<String, List<RefactoringData>> listRefactoringsByMethod;
	private Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly;
	private ArrayList<RefactoringData> listRefactoring;

	public ArrayList<RefactoringData> getListRefactoring() {
		return listRefactoring;
	}

	public void setListRefactoring(ArrayList<RefactoringData> listRefactoring) {
		this.listRefactoring = listRefactoring;
	}

	public FilterSmellResult getSmellsInitial() {
		return smellsInitial;
	}

	public void setSmellsInitial(FilterSmellResult smellsInitial) {
		this.smellsInitial = smellsInitial;
	}

	public FilterSmellResult getSmellsFinal() {
		return smellsFinal;
	}

	public void setSmellsFinal(FilterSmellResult smellsFinal) {
		this.smellsFinal = smellsFinal;
	}

	public Map<String, List<RefactoringData>> getListRefactoringsByMethod() {
		return listRefactoringsByMethod;
	}

	public void setListRefactoringsByMethod(Map<String, List<RefactoringData>> listRefactoringsByMethod) {
		this.listRefactoringsByMethod = listRefactoringsByMethod;
	}

	public Map<String, List<RefactoringData>> getListRefactoringsByMethodSmelly() {
		return listRefactoringsByMethodSmelly;
	}

	public void setListRefactoringsByMethodSmelly(Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly) {
		this.listRefactoringsByMethodSmelly = listRefactoringsByMethodSmelly;
	}
}
