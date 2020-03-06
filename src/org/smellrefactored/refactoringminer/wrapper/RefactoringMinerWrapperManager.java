package org.smellrefactored.refactoringminer.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;


public class RefactoringMinerWrapperManager {
	
	private String repositoryPath;
	private String initialCommitId;
	private String finalCommitId;
	private String cacheBaseFileName;
	private String fullCacheFileName;
	
	static Logger logger = LoggerFactory.getLogger(RefactoringMinerWrapperManager.class);
	
	public RefactoringMinerWrapperManager(String repositoryPath, String initialCommitId, String finalCommitId, String resultBaseFileName) throws Exception {
		this.repositoryPath = repositoryPath;
		this.initialCommitId = initialCommitId;
		this.finalCommitId = finalCommitId;
		
		String cacheDir = Paths.get(resultBaseFileName).getParent().getFileName() + "\\cache\\refactoring\\" + Paths.get(resultBaseFileName).getFileName();
		File cacheDirHandler = new File(cacheDir);
		cacheDirHandler.mkdirs();
		this.cacheBaseFileName =  cacheDir + "\\" + Paths.get(resultBaseFileName).getFileName() + "-refactoring";
		this.fullCacheFileName = this.cacheBaseFileName  + "-" + this.initialCommitId + "-" + this.finalCommitId;
	}

	public String getIndividualCacheFileName(String commitId) {
		return this.cacheBaseFileName  + "-" + commitId;
	}
	
	public List<RefactoringMinerWrapperDto> getRefactoringDtoListWithoutCache() throws Exception {
		return (this.getRefactoringDtoListFromRefactoringMiner());
	}

	public List<RefactoringMinerWrapperDto> getRefactoringDtoListUsingJsonCache() throws Exception {
		List<RefactoringMinerWrapperDto> refactoringDtoList;
		RefactoringMinerWrapperCacheJson cacheJson = new RefactoringMinerWrapperCacheJson(fullCacheFileName);
		if (!cacheJson.hasCache()) {
			refactoringDtoList = this.getRefactoringDtoListFromRefactoringMiner();
			cacheJson.saveRefactoringDtoListToFile(refactoringDtoList);
		}
		refactoringDtoList = cacheJson.getRefactoringDtoListFromFile();
		return (refactoringDtoList);
	}

	public List<RefactoringMinerWrapperDto> getRefactoringDtoListUsingJsonIndividualCache() throws Exception {
		List<RefactoringMinerWrapperDto> result = new ArrayList<RefactoringMinerWrapperDto>();
		List<String> commitIds = new ArrayList<String>();
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(this.repositoryPath);
	    Iterable<RevCommit> walk = gitService.createRevsWalkBetweenCommits(repo, this.initialCommitId, this.finalCommitId);
	    Iterator<RevCommit> walkIterator = walk.iterator();
	    while (walkIterator.hasNext()) {
	        RevCommit commit = walkIterator.next();
	        String commitId = commit.getId().getName();
	        commitIds.add(commitId);
	        addRefactoringDtosFromCommitIdToList(commitId, result);
	    }	
	    return (result);
	}
	
	private void addRefactoringDtosFromCommitIdToList(String commitId, List<RefactoringMinerWrapperDto> refactoringMinerWrapperDtos) throws Exception {
		final String individualCacheBaseFileName = this.cacheBaseFileName + "-" + commitId;
		String commitCacheFileName = individualCacheBaseFileName + ".json";
		File cacheFile = new File(commitCacheFileName);
		if (cacheFile.exists()) {
			logger.info("Getting list of refactorings for the " + commitId + " commit from the individual cache file");
			addRefactoringDtoListFromJsonCommitCacheToList(commitCacheFileName, refactoringMinerWrapperDtos);
		} else {
			logger.info("Getting list of refactorings for the " + commitId + " commit from RefactoringMinwe");
			List<RefactoringMinerWrapperDto> newDtos = getRefactoringDtoListFromRefactoringMiner(commitId);
			logger.info("Saving the " + commitId + " commit refactor list to the individual cache file");
			saveRefactoringDtoListToJsonCommitCache(newDtos, commitCacheFileName);
			for (RefactoringMinerWrapperDto newDto: newDtos) {
				refactoringMinerWrapperDtos.add(newDto);
			}
		}
	}

	private void addRefactoringDtoListFromJsonCommitCacheToList(String commitCacheFileName, List<RefactoringMinerWrapperDto> refactoringMinerWrapperDtos) throws FileNotFoundException {
		Gson gson = new Gson();  
		JsonReader reader = new JsonReader(new FileReader(commitCacheFileName));
		RefactoringMinerWrapperDto[] refactoringDtoArray = gson.fromJson(reader, RefactoringMinerWrapperDto[].class);
			for (RefactoringMinerWrapperDto refactoringDto: refactoringDtoArray) {
				refactoringMinerWrapperDtos.add(refactoringDto);
			}
	}

	private static void saveRefactoringDtoListToJsonCommitCache(List<RefactoringMinerWrapperDto> refactoringDtoList, String commitCacheFileName) throws IOException {
		String commitCacheTempFileName = commitCacheFileName.replace(".json", ".temp");
		Gson gson = new Gson();
		File existingTempFile = new File(commitCacheTempFileName);
		if (existingTempFile.exists()) {
			existingTempFile.delete();
		}
		File existingCacheFile = new File(commitCacheFileName);
		if (existingCacheFile.exists()) {
			existingCacheFile.delete();
		}
		FileWriter commitCacheTempFileHandler;
		commitCacheTempFileHandler = new FileWriter(commitCacheTempFileName);
		commitCacheTempFileHandler.append(gson.toJson(refactoringDtoList));
		commitCacheTempFileHandler.close();
		File tempfile = new File(commitCacheTempFileName);
		File newfile = new File(commitCacheFileName);
		tempfile.renameTo(newfile);
	}
	
	private List<RefactoringMinerWrapperDto> getRefactoringDtoListFromRefactoringMiner(String commitId) throws Exception {
		final List<RefactoringMinerWrapperDto> result = new ArrayList<RefactoringMinerWrapperDto>();
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(this.repositoryPath);
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		miner.detectAtCommit(repo, commitId, new RefactoringHandler() {
			@Override
			public void handle(String idCommit, List<Refactoring> refactorings) {
				for (Refactoring ref : refactorings) {
					RefactoringMinerWrapperDto refactoringDto = new RefactoringMinerWrapperDto();
					refactoringDto.wrapper(idCommit, ref);
					result.add(refactoringDto);
				}
			}
		});
		repo.close();
		return (result);
	}
	
	private List<RefactoringMinerWrapperDto> getRefactoringDtoListFromRefactoringMiner() throws Exception {
		final List<String> allCommitIds = new ArrayList<String>();
		final List<String> refactoringCommitIds = new ArrayList<String>();
		final List<RefactoringMinerWrapperDto> refactoringDtoList = new ArrayList<RefactoringMinerWrapperDto>();
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(this.repositoryPath);
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		miner.detectBetweenCommits(repo, initialCommitId, finalCommitId, new RefactoringHandler() {
			@Override
			public void handle(String idCommit, List<Refactoring> refactorings) {
				final List<RefactoringMinerWrapperDto> commitDtoList = new ArrayList<RefactoringMinerWrapperDto>();
				for (Refactoring ref : refactorings) {
					RefactoringMinerWrapperDto refactoringDto = new RefactoringMinerWrapperDto();
					refactoringDto.wrapper(idCommit, ref);
					// commitDtoList.add(refactoringDto);
					refactoringDtoList.add(refactoringDto);
				}
				if (commitDtoList.size() > 0) {
				 	refactoringCommitIds.add(idCommit);
				}
				allCommitIds.add(idCommit);
			}
		});
		repo.close();
		saveCommitIdsToFile(allCommitIds, this.fullCacheFileName + "-allCommits");
		saveCommitIdsToFile(refactoringCommitIds, this.fullCacheFileName + "-refactoringsCommits");
		return (refactoringDtoList);
	}
	
	private void saveCommitIdsToFile(List<String> commitIds, String baseFileName) throws IOException {
		String tempFileName = baseFileName + ".temp";
		String fileName = baseFileName + ".commitList";
		File existingTempFile = new File(tempFileName);
		existingTempFile.delete();
		File existingCacheFile = new File(fileName);
		existingCacheFile.delete();
		FileWriter tempFileHandler = new FileWriter(tempFileName);
		for (String commitId: commitIds) {
			tempFileHandler.write(commitId + "\n");
		}
		tempFileHandler.close();
		File tempfile = new File(tempFileName);
		File newfile = new File(fileName);
		tempfile.renameTo(newfile);
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
