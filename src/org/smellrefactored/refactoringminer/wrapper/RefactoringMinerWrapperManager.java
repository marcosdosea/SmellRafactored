package org.smellrefactored.refactoringminer.wrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RefactoringMinerWrapperManager {
	
	private String urlRepository;
	private String repositoryPath;
	private String initialCommitId;
	private String finalCommitId;
	private String resultBaseFileName;
	
	private GitService gitService;
	private Repository repo;

	static Logger logger = LoggerFactory.getLogger(RefactoringMinerWrapperManager.class);
	
	public RefactoringMinerWrapperManager(String urlRepository, String repositoryPath, String initialCommitId, String finalCommitId, String resultBaseFileName) throws Exception {
		this.urlRepository = urlRepository;
		this.repositoryPath = repositoryPath;
		this.initialCommitId = initialCommitId;
		this.finalCommitId = finalCommitId;
		this.resultBaseFileName = resultBaseFileName + "-refactoring" + "-" + this.initialCommitId + "-" + this.finalCommitId;
		
		gitService = new GitServiceImpl();
		repo = gitService.cloneIfNotExists(this.repositoryPath, this.urlRepository);

	}

	public List<RefactoringMinerWrapperDto> getRefactoringDtoListWithoutCache() throws Exception {
		return (this.getRefactoringDtoListFromRefactoringMiner());
	}

	public List<RefactoringMinerWrapperDto> getRefactoringDtoListUsingJsonCache() throws Exception {
		List<RefactoringMinerWrapperDto> refactoringDtoList;
		RefactoringMinerWrapperCacheJson cacheJson = new RefactoringMinerWrapperCacheJson(resultBaseFileName);
		if (!cacheJson.hasCache()) {
			refactoringDtoList = this.getRefactoringDtoListFromRefactoringMiner();
			cacheJson.saveRefactoringDtoListToFile(refactoringDtoList);
		}
		refactoringDtoList = cacheJson.getRefactoringDtoListFromFile();
		return (refactoringDtoList);
	}

	private List<RefactoringMinerWrapperDto> getRefactoringDtoListFromRefactoringMiner() throws Exception {
		final List<RefactoringMinerWrapperDto> refactoringDtoList = new ArrayList<RefactoringMinerWrapperDto>();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		miner.detectBetweenCommits(repo, initialCommitId, finalCommitId, new RefactoringHandler() {
			@Override
			public void handle(String idCommit, List<Refactoring> refactorings) {
				for (Refactoring ref : refactorings) {
					RefactoringMinerWrapperDto refactorinDto = new RefactoringMinerWrapperDto();
					refactorinDto.wrapper(idCommit, ref);
					refactoringDtoList.add(refactorinDto);
				}
			}
		});
		return (refactoringDtoList);
	}
	
	
	public HashSet<String> getCommitsWithRefactoringsFromRefactoringList(List<RefactoringMinerWrapperDto> refactoringDtoList) {
		HashSet<String> commitsWithRefactorings = new HashSet<String>();
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			if (refactoringDto != null) {
				commitsWithRefactorings.add(refactoringDto.commitId);
			}
		}
		return (commitsWithRefactorings);
	}

	
}
