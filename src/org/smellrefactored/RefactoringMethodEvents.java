package org.smellrefactored;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.refactoringminer.api.RefactoringType;

public class RefactoringMethodEvents {

	public static HashSet<String> getMethodRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		// Original: refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		refactoringTypes.add(RefactoringType.RENAME_METHOD.toString());
		refactoringTypes.add(RefactoringType.MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_RENAME_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PULL_UP_OPERATION.toString());
		refactoringTypes.add(RefactoringType.PUSH_DOWN_OPERATION.toString());
		refactoringTypes.add(RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.MOVE_AND_INLINE_OPERATION.toString());
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		/// refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
		return refactoringTypes;
	}
	
	private RefactoringEvents refactoringEvents;
	
	public RefactoringMethodEvents(RefactoringEvents refactoringEvents) {
		this.refactoringEvents = refactoringEvents;
	}
	
	public boolean hasRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}
	
	private ArrayList<RefactoringEvent> getRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> result = new ArrayList<RefactoringEvent>(); 
		String filePath = originalFilePath;
		String className = originalClassName;
		String methodName = originalMethodName;
		boolean renamedMethod;
		Date dateCommitRenamed = null;
		do {
			renamedMethod = false;
			String pathRenamedName = null;
			String classRenamedName = null;
			String methodRenamedName = null;
			for (RefactoringEvent event : this.refactoringEvents.getAllMergedIntoMaster()) {
				if (!event.getCommitId().equals(commitId)) {
					continue;
				}
				if ( (!RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) && (!RefactoringMethodEvents.getMethodRenameRefactoringTypes().contains(event.getRefactoringType())) && (!targetTefactoringTypes.contains(event.getRefactoringType())) ) {
					continue;
				}
				if (!event.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (!event.isSameClassRefactored(className)) {
					continue;
				}
				if (RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(event.getCommitData().getDate()) < 0)) ) {
						renamedMethod = true;
						// Change it
						dateCommitRenamed = event.getCommitData().getDate();
						pathRenamedName = event.getFileNameBefore();
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
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(event.getCommitData().getDate()) < 0)) ) {
						renamedMethod = true;
						// Do not change
						pathRenamedName = filePath;
						classRenamedName = className;
						// Change it
						dateCommitRenamed = event.getCommitData().getDate();
						methodRenamedName = event.getNewNameForMethodWhenRenameMethod();
					}
				}
			}
			if (renamedMethod) {
				filePath = pathRenamedName;
				className = classRenamedName;
				methodName = methodRenamedName;
			} else {
				dateCommitRenamed = null;
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
	
}
