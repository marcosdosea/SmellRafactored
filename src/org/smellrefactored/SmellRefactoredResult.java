package org.smellrefactored;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SmellRefactoredResult {
	private Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly;
	private Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly;
	private ArrayList<RefactoringData> listRefactoring;

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

	public void setListRefactoringsByMethodNotSmelly(Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly) {
		this.listRefactoringsByMethodNotSmelly = listRefactoringsByMethodNotSmelly;
	}
}
