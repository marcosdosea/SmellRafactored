package org.smellrefactored;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperDto;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperManager;

public class RefactoringEvents {
	
	private LinkedHashMap<String, Integer> typeCounter = new LinkedHashMap<String, Integer>();  
	
	private String repositoryPath;
	private CommitRange commitRange;
	private ArrayList<RefactoringEvent> events = new ArrayList<RefactoringEvent>();
	
	public RefactoringEvents(String repositoryPath, CommitRange commitRange, String resultBaseFileName) throws Exception {
		this.repositoryPath = repositoryPath;
		this.commitRange = commitRange;
		
		RefactoringMinerWrapperManager refactoringMinerWrapperManager = new RefactoringMinerWrapperManager(repositoryPath, this.commitRange.getNextCommit(this.commitRange.getInitialCommitId()).getId(), this.commitRange.getFinalCommitId(), resultBaseFileName);
		List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListUsingJsonCache();
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			if (refactoringDto != null) {
				if (this.commitRange.exists(refactoringDto.commitId)) {
					CommitData commitData = this.commitRange.getCommitByIdAddingIfNotExists(refactoringDto.commitId);
					RefactoringEvent refactoringData = new RefactoringEvent(refactoringDto, this.repositoryPath);
					refactoringData.setCommitData(commitData);
					events.add(refactoringData);
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
	
	public CommitRange getCommitRange() {
		return (this.commitRange);
	}
	
}
