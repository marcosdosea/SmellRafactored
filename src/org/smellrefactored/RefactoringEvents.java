package org.smellrefactored;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.refactoringminer.api.RefactoringType;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperDto;

public class RefactoringEvents {
	
	private HashSet<String> getClassRenameRefactoringTypes() {
		HashSet<String> refactoringTypes = new HashSet<String>();
		refactoringTypes.add(RefactoringType.RENAME_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_CLASS.toString());
		refactoringTypes.add(RefactoringType.MOVE_RENAME_CLASS.toString());
		/// refactoringTypes.add(RefactoringType.MOVE_SOURCE_FOLDER.toString());
		/// refactoringTypes.add(RefactoringType.RENAME_PACKAGE.toString());
		return refactoringTypes;
	}

	private HashSet<String> getMethodRenameRefactoringTypes() {
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
	
	LinkedHashMap<String, Integer> typeCounter = new LinkedHashMap<String, Integer>();  
	
	private String repositoryPath;
	private ArrayList<RefactoringEvent> events = new ArrayList<RefactoringEvent>();
	
	public RefactoringEvents(List<RefactoringMinerWrapperDto> refactoringDtoList, String repositoryPath) {
		this.repositoryPath = repositoryPath;
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			if (refactoringDto != null) {
				RefactoringEvent refactoringData = new RefactoringEvent(refactoringDto, this.repositoryPath);
				events.add(refactoringData);
			}
		}
		this.updateTypeCounter();
	}
	
	public void removeEventsForCommit(String commitId) {
		for (RefactoringEvent event: events) {
			if (event.getCommitId().equals(commitId)) {
				events.remove(event);
			}
		}
		this.updateTypeCounter();
	}
	
	private void updateTypeCounter() {
		typeCounter.clear();
		for (RefactoringEvent event: events) {
			typeCounter.put(event.getRefactoringType(), typeCounter.getOrDefault(event.getRefactoringType(), 0) + 1);
		}
	}
	
	public int countType(String refactoringType) {
		return (this.typeCounter.getOrDefault(refactoringType, 0));
	}

	public int countTypes(HashSet<String> refactoringTypes) {
		int result = 0;
		for (String refactoringType: refactoringTypes) {
			result += countType(refactoringType);
		}
		return (result);
	}

	public ArrayList<RefactoringEvent> getAll() {
		return (this.events);
	}

	public int size() {
		return (this.events.size());
	}
	
	public boolean hasClassRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringEvent> refactoringsForCommit = getClassRefactoringsInCommit(commitId, originalFilePath, originalClassName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}
	
	
	public ArrayList<RefactoringEvent> getClassRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringEvent> result = new ArrayList<RefactoringEvent>(); 
		String filePath = originalFilePath;
		String className = originalClassName;
		boolean renamedClass;
		Date dateCommitRenamed = null;
		do {
			renamedClass = false;
			String pathRenamedName = null;
			String classRenamedName = null;
			for (RefactoringEvent event : events) {
				if (!event.getCommitId().equals(commitId)) {
					continue;
				}
				if ( (!this.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) && (!targetTefactoringTypes.contains(event.getRefactoringType())) ) {
					continue;
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
				if (this.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(event.getCommitDate()) < 0)) ) {
						renamedClass = true;
						dateCommitRenamed = event.getCommitDate();
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

	
	public boolean hasMethodRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) {
		ArrayList<RefactoringEvent> refactoringsForCommit = getMethodRefactoringsInCommit(commitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}

	public ArrayList<RefactoringEvent> getMethodRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) {
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
			for (RefactoringEvent event : events) {
				if (!event.getCommitId().equals(commitId)) {
					continue;
				}
				if ( (!this.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) && (!this.getMethodRenameRefactoringTypes().contains(event.getRefactoringType())) && (!targetTefactoringTypes.contains(event.getRefactoringType())) ) {
					continue;
				}
				if (!event.getFileNameBefore().equals(filePath)) {
					continue;
				}
				if (!event.isSameClassRefactored(className)) {
					continue;
				}
				if (this.getClassRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(event.getCommitDate()) < 0)) ) {
						renamedMethod = true;
						// Change it
						dateCommitRenamed = event.getCommitDate();
						pathRenamedName = event.getFileNameBefore();
						classRenamedName = event.getNewNameForClassWhenRenameClass();
						// Do not change
						methodRenamedName = methodName;
					}
				}
				if (!event.isSameMethodRefactored(className, methodName)) {
					continue;
				}
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					result.add(event);
				}
				if (this.getMethodRenameRefactoringTypes().contains(event.getRefactoringType())) {
					if ((dateCommitRenamed == null) || ( (dateCommitRenamed != null)
							&& (dateCommitRenamed.compareTo(event.getCommitDate()) < 0)) ) {
						renamedMethod = true;
						// Do not change
						pathRenamedName = filePath;
						classRenamedName = className;
						// Change it
						dateCommitRenamed = event.getCommitDate();
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
	
}
