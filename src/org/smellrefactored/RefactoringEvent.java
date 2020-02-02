package org.smellrefactored;

import java.util.Date;

import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperDto;

public class RefactoringEvent implements Comparable<RefactoringEvent> {

	private String repositoryPath;
	
	private String commitId;
	private Date commitDate;
	private String refactoringName;
	private String refactoringType;
	private String leftSide;
	private String rightSide;
	private String fileNameBefore;
	private String fileNameAfter;
	private String involvedClassesBefore;
	private String involvedClassesAfter;
	private String shortMessage;
	private String fullMessage;
	
	private String className;
	private String methodName;
	
	public RefactoringEvent(RefactoringMinerWrapperDto refactoringDto, String repositoryPath) {
		this.repositoryPath = repositoryPath;
		
		this.setCommitId(refactoringDto.commitId);
		this.setRefactoringName(refactoringDto.name);
		this.setRefactoringType(refactoringDto.type.toString());
		if ( (refactoringDto.leftSide != null) && (refactoringDto.leftSide.size()>0) )  {
			this.setFileNameBefore(makeFilePathCompatibleWithDesignRoleSmell(refactoringDto.leftSide.get(0).getFilePath()));
			this.setLeftSide(refactoringDto.leftSide.get(0).getCodeElement());
		}
		if ( (refactoringDto.rightSide != null) && (refactoringDto.rightSide.size()>0) ) {
			this.setFileNameAfter(makeFilePathCompatibleWithDesignRoleSmell(refactoringDto.rightSide.get(0).getFilePath()));
			this.setRightSide(refactoringDto.rightSide.get(0).getCodeElement());
		}
		this.setInvolvedClassesBefore(refactoringDto.involvedClassesBefore.toString());
		this.setInvolvedClassesAfter(refactoringDto.involvedClassesAfter.toString());
		
		this.completRefactoringData();
	}
	
	public String getCommitId() {
		return commitId;
	}

	public void setCommitId(String commit) {
		this.commitId = commit;
	}
	
	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
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

	public String getRightSide() {
		return rightSide;
	}

	public void setRightSide(String rightSide) {
		this.rightSide = rightSide;
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

	public int compareTo(RefactoringEvent o) {
		return getCommitDate().compareTo(o.getCommitDate());
	}
	
	private String makeFilePathCompatibleWithDesignRoleSmell(String filePath) {
		String newFilePath = this.repositoryPath + "\\" + filePath.replace("/","\\");
		newFilePath = newFilePath.replace("\\\\", "\\");
		return (newFilePath);
	}
	
	public boolean isSameClassRefactored(String className) {
		// boolean isClassInvolvementBefore = this.involvedClassesBefore.contains(className);
		// boolean isClassInvolvementAfter  = this.involvedClassesAfter.contains(className);
		// boolean isClassInvolvement  = (isClassInvolvementBefore || isClassInvolvementAfter);
		boolean isClassSameName = ( (this.className != null) && (this.className.equals(className)) );
		return (isClassSameName);
	}

	public boolean isSameMethodRefactored(String className, String methodName) {
		boolean isClassSameName = isSameClassRefactored(className);
		// boolean isMethodLeftSide = this.leftSide().contains(methodName);
		// boolean isMethodRightSide = this.eightSide().contains(methodName);
		// boolean isMethodSide = (isMethodLeftSide || isMethodRightSide);
		boolean isMethodSameName = ( (this.methodName != null) && (this.methodName.equals(methodName)) );
		return (isClassSameName && isMethodSameName);
	}
	
	private void completRefactoringData() {
		if (refactoringType.contains("VARIABLE")) {
			className = getClassNameFromInvolvedClassesBefore();
		} else if (refactoringType.contains("ATTRIBUTE")) {
			className = getClassNameFromInvolvedClassesBefore();
		} else if (refactoringType.contains("PARAMETER")) {
			className = getClassNameFromInvolvedClassesBefore();
		} else if (refactoringType.contains("RETURN_TYPE")) {
			className = getClassNameFromInvolvedClassesBefore();
		} else if (refactoringType.contains("OPERATION")) {
			className = getClassNameFromInvolvedClassesBefore();
			methodName = extractMethodName(leftSide);
		} else if (refactoringType.contains("METHOD")) {
			className = getClassNameFromInvolvedClassesBefore();
			methodName = extractMethodName(leftSide);
		} else  if (refactoringType.contains("EXTRACT_SUPERCLASS")) {
			className = leftSide;
		} else if (refactoringType.contains("CLASS")) {
			className = getClassNameFromInvolvedClassesBefore();
		} else if (refactoringType.contains("PACKAGE")) {
			/// @TODO: to implement
		} else if (refactoringType.contains("FOLDER")) {
			/// @TODO: to implement
		}
	}
	
	private String getClassNameFromInvolvedClassesBefore() {
		return involvedClassesBefore.replace("[", "").replace("]", "");
	}
	
	public String getNewNameForClassWhenRenameClass() {
		return (extractMethodName(rightSide));
	}
	
	public String getNewNameForMethodWhenRenameMethod() {
		return (extractMethodName(rightSide));
	}
	
	private String extractMethodName(String side) {
		String result = side;
		int methodNameEnd = result.indexOf("(");
		if (methodNameEnd >= 0) {
			result = result.substring(0, methodNameEnd);
		}
		int methodNameBegin = result.lastIndexOf(" ") + 1;
		if (methodNameBegin >= 0) {
			result = result.substring(methodNameBegin);
		}
		return (result);
	}

}
