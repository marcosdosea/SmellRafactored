package org.smellrefactored;

import java.util.Date;
import java.util.HashSet;

public class RefactoringData implements Comparable<RefactoringData> {

	public String getRightSide() {
		return rightSide;
	}

	public void setRightSide(String rightSide) {
		this.rightSide = rightSide;
	}

	private String commitId;
	private Date commitDate;
	private String refactoringName;
	private String refactoringType;
	private String leftSide;
	private String rightSide;
	private String fileNameBefore;
	private String fileNameAfter;
	private String involvedClassesAfter;
	private String involvedClassesBefore;
	private String shortMessage;
	private String fullMessage;
	
	private String nomeClasse;
	private String classDesignRole;
	private String nomeMetodo;



	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commit) {
		this.commitId = commit;
	}
	
	public String getNomeClasse() {
		return nomeClasse;
	}

	public void setNomeClasse(String nomeClasse) {
		this.nomeClasse = nomeClasse;
	}

	public String getNomeMetodo() {
		return nomeMetodo;
	}

	public void setNomeMetodo(String nomeMetodo) {
		this.nomeMetodo = nomeMetodo;
	}

	
	public String getFileNameBefore() {
		return fileNameBefore;
	}

	public void setFileNameBefore(String fileNameBefore) {
		this.fileNameBefore = fileNameBefore;
	}

	public String getFileNameAfter() {
		return fileNameAfter;
	}

	public void setFileNameAfter(String fileNameAfter) {
		this.fileNameAfter = fileNameAfter;
	}

	public String getClassDesignRole() {
		return classDesignRole;
	}

	public void setClassDesignRole(String classDesignRole) {
		this.classDesignRole = classDesignRole;
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
	
	public Date getCommitDate() {
		return commitDate;
	}

	public void setCommitDate(Date commitDate) {
		this.commitDate = commitDate;
	}

	public int compareTo(RefactoringData o) {
		return getCommitDate().compareTo(o.getCommitDate());
	}

}
