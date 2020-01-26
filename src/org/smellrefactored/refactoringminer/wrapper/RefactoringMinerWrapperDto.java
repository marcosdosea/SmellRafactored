package org.smellrefactored.refactoringminer.wrapper;

import java.util.List;

import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringType;

import gr.uom.java.xmi.diff.CodeRange;

public class RefactoringMinerWrapperDto {
	
	/**
	 * WARNING: modifications in this class probably made the previously generated cache
	 *          files unusable. Increase the version of the cache file to avoid errors in
	 *          processing.
	 */
	
	public String commitId;
	public String name; 
	public RefactoringType type; 
	public List<String> involvedClassesBefore;
	public List<String> involvedClassesAfter;
	public List<CodeRange> leftSide;
	public List<CodeRange> rightSide;
	
	public void wrapper(String commitId, Refactoring refactoring) {
		this.commitId = commitId;
		this.name = refactoring.getName(); 
		this.type = refactoring.getRefactoringType(); 
		this.involvedClassesBefore = refactoring.getInvolvedClassesBeforeRefactoring();
		this.involvedClassesAfter = refactoring.getInvolvedClassesAfterRefactoring();
		this.leftSide = refactoring.leftSide();
		this.rightSide = refactoring.rightSide();
	}
}
