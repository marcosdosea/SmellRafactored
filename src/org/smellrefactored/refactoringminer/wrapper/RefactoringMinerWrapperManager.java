package org.smellrefactored.refactoringminer.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
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

	private List<RefactoringMinerWrapperDto> getRefactoringDtoListFromRefactoringMiner() throws Exception {
		final List<String> allCommitIds = new ArrayList<String>();
		final List<String> refactoringCommitIds = new ArrayList<String>();
		final List<RefactoringMinerWrapperDto> refactoringDtoList = new ArrayList<RefactoringMinerWrapperDto>();
		final Gson gson = new Gson();
		final String individualCacheBaseFileName = this.cacheBaseFileName;
		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.openRepository(this.repositoryPath);
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		miner.detectBetweenCommits(repo, initialCommitId, finalCommitId, new RefactoringHandler() {
			@Override
			public boolean skipCommit(String commitId) {
				boolean result = false;
				String commitCacheFileName = individualCacheBaseFileName  + "-" + commitId + ".json";
				File cacheFile = new File(commitCacheFileName);
				if (cacheFile.exists()) {
					Gson gson = new Gson();  
					try {
						JsonReader reader = new JsonReader(new FileReader(commitCacheFileName));
						RefactoringMinerWrapperDto[] refactoringDtoArray = gson.fromJson(reader, RefactoringMinerWrapperDto[].class);
						if (refactoringDtoArray.length > 0) {
							refactoringCommitIds.add(commitId);
							for (RefactoringMinerWrapperDto refactoringDto: refactoringDtoArray) {
								refactoringDtoList.add(refactoringDto);
							}
						}
						allCommitIds.add(commitId);
						result = true;
						logger.info("Commit " + commitId + " refactorings obtained from individual cache.");
					} catch (FileNotFoundException e) {
						// do nothing
					}
				}
				return (result);  
			}
			@Override
			public void handle(String idCommit, List<Refactoring> refactorings) {
				final List<RefactoringMinerWrapperDto> commitDtoList = new ArrayList<RefactoringMinerWrapperDto>();
				for (Refactoring ref : refactorings) {
					RefactoringMinerWrapperDto refactoringDto = new RefactoringMinerWrapperDto();
					refactoringDto.wrapper(idCommit, ref);
					commitDtoList.add(refactoringDto);
					refactoringDtoList.add(refactoringDto);
				}
				
				String commitCacheBaseFileName = individualCacheBaseFileName  + "-" + idCommit;
				String commitCacheTempFileName = commitCacheBaseFileName + ".temp";
				String commitCacheFileName = commitCacheBaseFileName + ".json";
				try {
					File existingTempFile = new File(commitCacheTempFileName);
					existingTempFile.delete();
					File existingCacheFile = new File(commitCacheFileName);
					existingCacheFile.delete();
					FileWriter commitCacheTempFileHandler;
					commitCacheTempFileHandler = new FileWriter(commitCacheTempFileName);
					commitCacheTempFileHandler.append(gson.toJson(commitDtoList));
					commitCacheTempFileHandler.close();
					File tempfile = new File(commitCacheTempFileName);
					File newfile = new File(commitCacheFileName);
					tempfile.renameTo(newfile);
				} catch (IOException e) {
					e.printStackTrace();
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
