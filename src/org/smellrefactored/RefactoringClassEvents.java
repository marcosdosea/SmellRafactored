package org.smellrefactored;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

import org.refactoringminer.api.RefactoringType;

public class RefactoringClassEvents {

	public static HashSet<String> getClassRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		/// refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
		return refactoringTypes;
	}
	
	private RefactoringEvents refactoringEvents;
	
	public RefactoringClassEvents(RefactoringEvents refactoringEvents) {
		this.refactoringEvents = refactoringEvents;
		
	}
	
	public boolean hasRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getRefactoringsInCommit(commitId, originalFilePath, originalClassName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}
	
	private ArrayList<RefactoringEvent> getRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> result = new ArrayList<RefactoringEvent>(); 
		String filePath = originalFilePath;
		String className = originalClassName;
		boolean renamedClass;
		Date dateCommitRenamed = null;
		do {
			renamedClass = false;
			String pathRenamedName = null;
			String classRenamedName = null;
			for (RefactoringEvent event : this.refactoringEvents.getAllMergedIntoMaster()) {
				if (!event.getCommitId().equals(commitId)) {
					continue;
				}
				if ( (!RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) && (!targetTefactoringTypes.contains(event.getRefactoringType())) ) {
					continue;
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					RefactoringClassEvents.validateClassRefactoring(event);
				}
				if (!event.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (!event.isSameClassRefactored(className))  {
					continue;
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					result.add(event);
				}
				if (RefactoringClassEvents.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(event.getCommitData().getDate()) < 0)) ) {
						renamedClass = true;
						dateCommitRenamed = event.getCommitData().getDate();
						pathRenamedName = event.getFileNameAfter();
						classRenamedName = event.getNewNameForClassWhenRenameClass();
					}
				}
			}
			if (renamedClass) {
				filePath = pathRenamedName;
				className = classRenamedName;
			} else {
				dateCommitRenamed = null;
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

}
