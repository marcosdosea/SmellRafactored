package org.smellrefactored.methods;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import org.refactoringminer.api.RefactoringType;
import org.smellrefactored.CommitData;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.RefactoringEvents;
import org.smellrefactored.classes.RefactoringClassEvents;

public class RefactoringMethodEvents {

	public static HashSet<String> getMethodRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		return refactoringTypes;
	}
	
	private RefactoringEvents refactoringEvents;
	
	public RefactoringMethodEvents(RefactoringEvents refactoringEvents) {
		this.refactoringEvents = refactoringEvents;
	}
	
	public boolean hasRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes, false);
		return (refactoringsForCommit.size()>0);		 
	}
	
	public boolean hasRefactoringInThisCommitOrInFuture(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes, true);
		return (refactoringsForCommit.size()>0);		 
	}
	
	private ArrayList<RefactoringEvent> getRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes, boolean walkUntilFindOne) throws Exception {
		ArrayList<RefactoringEvent> result = new ArrayList<RefactoringEvent>(); 
		CommitData commit = this.refactoringEvents.getCommitRange().getCommitById(commitId);
		String filePath = originalFilePath;
		String className = originalClassName;
		String methodName = originalMethodName;
		boolean renamedMethod;
		ZonedDateTime dateTimeUtcCommitRenamed = null;
		do {
			renamedMethod = false;
			String filePathRenamedName = null;
			String classRenamedName = null;
			String methodRenamedName = null;
			for (RefactoringEvent event : this.refactoringEvents.getAllMergedIntoMaster()) {
				if (walkUntilFindOne) {
					if (result.size() > 0) {
						break;
					}
					if (commit.compareTo(event.getCommitData()) > 0) {
						continue;
					}
				} else {
					if (!event.getCommitId().equals(commitId)) {
						continue;
					}
				}
				if ( (!RefactoringClassEvents.getFilePathRenameRefactoringTypes().contains(event.getRefactoringType()))
						&& (!RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) 
						&& (!RefactoringMethodEvents.getMethodRenameRefactoringTypes().contains(event.getRefactoringType())) 
						&& (!targetTefactoringTypes.contains(event.getRefactoringType())) ) {
					continue;
				}
				if (!event.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (RefactoringClassEvents.getFilePathRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateTimeUtcCommitRenamed == null) || ( (dateTimeUtcCommitRenamed != null)
							&& (dateTimeUtcCommitRenamed.compareTo(event.getCommitData().getDateTimeUtc()) < 0)) ) {
						renamedMethod = true;
						// Change it
						dateTimeUtcCommitRenamed = event.getCommitData().getDateTimeUtc();
						filePathRenamedName = event.getFileNameBefore();
						// Do not change
						classRenamedName = className;
						methodRenamedName = methodName;
					}
				}
				if (!event.isSameClassRefactored(className)) {
					continue;
				}
				if (RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateTimeUtcCommitRenamed == null) || ( (dateTimeUtcCommitRenamed != null)
							&& (dateTimeUtcCommitRenamed.compareTo(event.getCommitData().getDateTimeUtc()) < 0)) ) {
						renamedMethod = true;
						// Change it
						dateTimeUtcCommitRenamed = event.getCommitData().getDateTimeUtc();
						filePathRenamedName = event.getFileNameBefore();
						classRenamedName = event.getNewNameForClassWhenRenameClass();
						// Do not change
						methodRenamedName = methodName;
					}
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					this.validateMethodRefactoring(event);
				}
				if (!event.isSameMethodRefactored(className, methodName)) {
					continue;
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					result.add(event);
				}
				if (RefactoringMethodEvents.getMethodRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateTimeUtcCommitRenamed == null) || ( (dateTimeUtcCommitRenamed != null)
							&& (dateTimeUtcCommitRenamed.compareTo(event.getCommitData().getDateTimeUtc()) < 0)) ) {
						renamedMethod = true;
						// Do not change
						filePathRenamedName = filePath;
						classRenamedName = className;
						// Change it
						dateTimeUtcCommitRenamed = event.getCommitData().getDateTimeUtc();
						methodRenamedName = event.getNewNameForMethodWhenRenameMethod();
					}
				}
			}
			if (renamedMethod) {
				filePath = filePathRenamedName;
				className = classRenamedName;
				methodName = methodRenamedName;
			} else {
				dateTimeUtcCommitRenamed = null;
			}
		} while (renamedMethod);
		return (result);
	}

	private void validateMethodRefactoring(RefactoringEvent event) throws Exception {
		RefactoringClassEvents.validateClassRefactoring(event);
		if (event.getMethodName() == null) {
			throw new Exception("NULL method name for " + event.getRefactoringType() + " refactoring type: " + event.getMethodName());
		}
		if (event.getMethodName().contains("[") || event.getMethodName().contains("]") || event.getMethodName().contains(",") || event.getMethodName().contains(" ")) {
			throw new Exception("DIRTY method name for " + event.getRefactoringType() + " refactoring type: " + event.getMethodName());
		}
	}
	
	static public String getCommitMethodKey(RefactoringEvent event) {
		return (
				event.getCommitId()
				+ "|" + event.getFileNameBefore()
				+ "|" + event.getClassName()
				+ "|" + event.getMethodName()
				);
	}

}
