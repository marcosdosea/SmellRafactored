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

	private String commit;
	private String refactoringName;
	private String refactoringType;
	private String leftSide;
	private String rightSide;
	private String involvedClassesAfter;
	private String involvedClassesBefore;
	private String shortMessage;
	private String fullMessage;
	
	
	private int numberOfClasses;
	private int numberOfMethods;
	private int systemLOC;
	private Date commitDate;

	private String fileNameBefore;
	private String fileNameAfter;

	private String nomeClasse;
	private String nomeMetodo;
	private int linesOfCode;
	private int complexity;
	private int numberOfParameters;
	private int efferent;
	private HashSet<String> listaTecnicas;
	private String smell;
	private String classDesignRole;

	
	
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

	public int getLinesOfCode() {
		return linesOfCode;
	}

	public void setLinesOfCode(int linesOfCode) {
		this.linesOfCode = linesOfCode;
	}

	public int getComplexity() {
		return complexity;
	}

	public void setComplexity(int complexity) {
		this.complexity = complexity;
	}

	public int getNumberOfParameters() {
		return numberOfParameters;
	}

	public void setNumberOfParameters(int numberOfParameters) {
		this.numberOfParameters = numberOfParameters;
	}

	public int getEfferent() {
		return efferent;
	}

	public void setEfferent(int efferent) {
		this.efferent = efferent;
	}

	public HashSet<String> getListaTecnicas() {
		return listaTecnicas;
	}

	public void setListaTecnicas(HashSet<String> listaTecnicas) {
		this.listaTecnicas = listaTecnicas;
	}

	public String getSmell() {
		return smell;
	}

	public void setSmell(String smell) {
		this.smell = smell;
	}

	public String getClassDesignRole() {
		return classDesignRole;
	}

	public void setClassDesignRole(String classDesignRole) {
		this.classDesignRole = classDesignRole;
	}

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

	public int getNumberOfClasses() {
		return numberOfClasses;
	}

	public void setNumberOfClasses(int numberOfClasses) {
		this.numberOfClasses = numberOfClasses;
	}

	public int getSystemLOC() {
		return systemLOC;
	}

	public void setSystemLOC(int systemLOC) {
		this.systemLOC = systemLOC;
	}

	public int getNumberOfMethods() {
		return numberOfMethods;
	}

	public void setNumberOfMethods(int numberOfMethods) {
		this.numberOfMethods = numberOfMethods;
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
