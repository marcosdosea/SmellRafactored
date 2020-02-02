package org.smellrefactored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.refactoringminer.api.RefactoringType;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperDto;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperManager;

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
	private CommitRange commitRange;
	private ArrayList<RefactoringEvent> events = new ArrayList<RefactoringEvent>();
	
	public RefactoringEvents(String repositoryPath, CommitRange commitRange, String resultBaseFileName) throws Exception {
		this.repositoryPath = repositoryPath;
		this.commitRange = commitRange;
		
		ArrayList<CommitData> commitsMergedIntoMaster = this.commitRange.getCommitsMergedIntoMaster();
		RefactoringMinerWrapperManager refactoringMinerWrapperManager = new RefactoringMinerWrapperManager(repositoryPath, this.commitRange.getNextCommit(this.commitRange.getInitialCommitId()).getId(), this.commitRange.getFinalCommitId(), resultBaseFileName);
		List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListUsingJsonCache();
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			if (refactoringDto != null) {
				for (CommitData commitMergedIntoMaster: commitsMergedIntoMaster) {
					if (refactoringDto.commitId.equals(commitMergedIntoMaster.getId())) {
						RefactoringEvent refactoringData = new RefactoringEvent(refactoringDto, this.repositoryPath);
						refactoringData.setCommitData(commitMergedIntoMaster);
						events.add(refactoringData);
						break;
					}
				}
			}
		}
		Collections.sort(events);
		
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

	public ArrayList<RefactoringEvent> getAllMergedIntoMaster() {
		return (this.events);
	}

	public int size() {
		return (this.events.size());
	}

	public boolean hasClassRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getClassRefactoringsInCommit(commitId, originalFilePath, originalClassName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}
	
	public ArrayList<RefactoringEvent> getClassRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, HashSet<String> targetTefactoringTypes) throws Exception {
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
				if (targetTefactoringTypes.contains(event.getRefactoringType())) {
					this.validateClassRefactoring(event);
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

	private void validateClassRefactoring(RefactoringEvent event) throws Exception {
		if (event.getClassName() == null) {
			throw new Exception("NULL class name for " + event.getRefactoringType() + " refactoring type");
		}
		if (event.getClassName().contains("[") || event.getClassName().contains("]") || event.getClassName().contains(",") || event.getClassName().contains(" ")) {
			throw new Exception("DIRTY class name for " + event.getRefactoringType() + " refactoring type");
		}
	}
	
	public boolean hasMethodRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) throws Exception {
		ArrayList<RefactoringEvent> refactoringsForCommit = getMethodRefactoringsInCommit(commitId, originalFilePath, originalClassName, originalMethodName, targetTefactoringTypes);
		return (refactoringsForCommit.size()>0);		 
	}

	
	public ArrayList<RefactoringEvent> getMethodRefactoringsInCommit(String commitId, String originalFilePath, String originalClassName, String originalMethodName, HashSet<String> targetTefactoringTypes) throws Exception {
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
				if (this.getMethodRenameRefactoringTypes().contains(event.getRefactoringType())) {
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
		if (event.getClassName() == null) {
			throw new Exception("NULL class name for " + event.getRefactoringType() + " refactoring type: " + event.getClassName());
		}
		if (event.getClassName().contains("[") || event.getClassName().contains("]") || event.getClassName().contains(",") || event.getClassName().contains(" ")) {
			throw new Exception("DIRTY class name for " + event.getRefactoringType() + " refactoring type: " + event.getClassName());
		}
		if (event.getMethodName() == null) {
			throw new Exception("NULL method name for " + event.getRefactoringType() + " refactoring type: " + event.getMethodName());
		}
		if (event.getMethodName().contains("[") || event.getMethodName().contains("]") || event.getMethodName().contains(",") || event.getMethodName().contains(" ")) {
			throw new Exception("DIRTY method name for " + event.getRefactoringType() + " refactoring type: " + event.getMethodName());
		}
	}
	
}
