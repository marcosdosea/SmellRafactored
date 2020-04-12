package org.smellrefactored.classes;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;

import org.refactoringminer.api.RefactoringType;
import org.smellrefactored.CommitData;
import org.smellrefactored.RefactoringEvent;
import org.smellrefactored.RefactoringEvents;

public class RefactoringClassEvents {

	public static HashSet<String> getFilePathRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		return refactoringTypes;
	}

	// public static HashSet<String> getPackageRenameRefactoringTypes() {
	// 	HashSet<String> refactoringTypes = new HashSet<String>();
	// 	refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
	// 	return refactoringTypes;
	// }

	public static HashSet<String> getClassRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		return refactoringTypes;
	}
	
	private RefactoringEvents refactoringEvents;
	
	public RefactoringClassEvents(RefactoringEvents refactoringEvents) {
		this.refactoringEvents = refactoringEvents;
		
	}
	
	public boolean hasRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, targetTefactoringTypes, false);
		return (refactoringsForCommit.size()>0);		 
	}
	
	public boolean hasRefactoringInThisCommitOrInFuture(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, targetTefactoringTypes, true);
		return (refactoringsForCommit.size()>0);		 
	}
	
	private ArrayList<RefactoringEvent> getRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes, boolean walkUntilFindOne) throws Exception {
		ArrayList<RefactoringEvent> result = new ArrayList<RefactoringEvent>();
		CommitData commit = this.refactoringEvents.getCommitRange().getCommitById(commitId);
		String filePath = originalFilePath;
		String className = originalClassName;
		boolean renamedClass;
		ZonedDateTime dateTimeUtcCommitRenamed = null;
		do {
			renamedClass = false;
			String filePathRenamedName = null;
			String classRenamedName = null;
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
						&& (!targetTefactoringTypes.contains(event.getRefactoringType())) ) {
					continue;
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					RefactoringClassEvents.validateClassRefactoring(event);
				}
				if (!event.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (RefactoringClassEvents.getFilePathRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateTimeUtcCommitRenamed == null) || ( (dateTimeUtcCommitRenamed != null)
							&& (dateTimeUtcCommitRenamed.compareTo(event.getCommitData().getDateTimeUtc()) < 0)) ) {
						renamedClass = true;
						// Change it
						dateTimeUtcCommitRenamed = event.getCommitData().getDateTimeUtc();
						filePathRenamedName = event.getFileNameAfter();
						// Do not change
						classRenamedName = className;
					}
				}
				if (!event.isSameClassRefactored(className))  {
					continue;
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					result.add(event);
				}
				if (RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateTimeUtcCommitRenamed == null) || ( (dateTimeUtcCommitRenamed != null)
							&& (dateTimeUtcCommitRenamed.compareTo(event.getCommitData().getDateTimeUtc()) < 0)) ) {
						renamedClass = true;
						dateTimeUtcCommitRenamed = event.getCommitData().getDateTimeUtc();
						filePathRenamedName = event.getFileNameAfter();
						classRenamedName = event.getNewNameForClassWhenRenameClass();
					}
				}
			}
			if (renamedClass) {
				filePath = filePathRenamedName;
				className = classRenamedName;
			} else {
				dateTimeUtcCommitRenamed = null;
			}
		} while (renamedClass);
		return (result);
	}
	
	static public void validateClassRefactoring(RefactoringEvent event) throws Exception {
		if (event.getClassName() == null) {
			throw new Exception("NULL class name for " + event.getRefactoringType() + " refactoring type");
		}
		if (event.getClassName().contains("[") || event.getClassName().contains("]") || event.getClassName().contains(",") || event.getClassName().contains(" ")) {
			throw new Exception("DIRTY class name for " + event.getRefactoringType() + " refactoring type");
		}
	}

	static public String getCommitClassKey(RefactoringEvent event) {
		return (
				event.getCommitId()
				+ "|" + event.getFileNameBefore()
				+ "|" + event.getClassName()
				);
	}

	
}
