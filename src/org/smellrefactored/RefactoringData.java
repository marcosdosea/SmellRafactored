package org.smellrefactored;

public class RefactoringData {

	public String getRightSide() {
		return rightSide;
	}

	public void setRightSide(String rightSide) {
		this.rightSide = rightSide;
	}

	private String commit;
	private String refactoringName;
	private String refactoringType;
	private String leftSide;
	private String rightSide;
	private String involvedClassesAfter;
	private String involvedClassesBefore;
	private String shortMessage;
	private String fullMessage;

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getRefactoringName() {
		return refactoringName;
	}

	public void setRefactoringName(String refactoringName) {
		this.refactoringName = refactoringName;
	}

	public String getRefactoringType() {
		return refactoringType;
	}

	public void setRefactoringType(String refactoringType) {
		this.refactoringType = refactoringType;
	}

	public String getLeftSide() {
		return leftSide;
	}

	public void setLeftSide(String leftSide) {
		this.leftSide = leftSide;
	}

	public String getInvolvedClassesAfter() {
		return involvedClassesAfter;
	}

	public void setInvolvedClassesAfter(String involvedClassesAfter) {
		this.involvedClassesAfter = involvedClassesAfter;
	}

	public String getInvolvedClassesBefore() {
		return involvedClassesBefore;
	}

	public void setInvolvedClassesBefore(String involvedClassesBefore) {
		this.involvedClassesBefore = involvedClassesBefore;
	}

	public String getShortMessage() {
		return shortMessage;
	}

	public void setShortMessage(String shortMessage) {
		this.shortMessage = shortMessage;
	}

	public String getFullMessage() {
		return fullMessage;
	}

	public void setFullMessage(String fullMessage) {
		this.fullMessage = fullMessage;
	}

}
