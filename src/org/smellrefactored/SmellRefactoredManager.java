package org.smellrefactored;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperManager;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperDto;

public class SmellRefactoredManager {

	final boolean USE_SMELLS_COMMIT_OLD_CACHE = true; 
	
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private String urlRepository;
	private String repositoryPath;
	private String initialCommit;
	private String finalCommit;
	private List<LimiarTecnica> listaLimiarTecnica;

	private CommitRange commitRange;
	private RefactoringEvents refactoringEvents;
	
	private CommitSmell commitSmell;
	
	private String resultBaseFileName;


	HashSet<String> listCommitEvaluated = new HashSet<String>();
	
	public SmellRefactoredManager(String urlRepository, String repositoryPath, String initialCommit, String finalCommit,
			List<LimiarTecnica> listaLimiarTecnica, String resultBaseFileName) {
		this.urlRepository = urlRepository;
		this.repositoryPath = repositoryPath;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultBaseFileName = resultBaseFileName;

		try {
			prepareSmellRefactored();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void prepareSmellRefactored() throws Exception {
		if (initialCommit.equals(finalCommit)) {
			throw new Exception("At least 2 commits are required in the range of commits to be analyzed.");
		}
		
		RefactoringMinerWrapperManager refactoringMinerWrapperManager = new RefactoringMinerWrapperManager(urlRepository, repositoryPath, initialCommit, finalCommit, resultBaseFileName);

		// List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListWithoutCache();
		List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListUsingJsonCache();

		refactoringEvents = new RefactoringEvents(refactoringDtoList, this.repositoryPath);
		refactoringEvents.removeEventsForCommit(initialCommit);
		
		HashSet<String> commitsWithRefactorings = refactoringMinerWrapperManager.getCommitsWithRefactoringsFromRefactoringList(refactoringDtoList);

		logger.info("Total de refactorings encontrados: " + refactoringEvents.size());
		
		
		GitService gitService = new GitServiceImpl();
		gitService.cloneIfNotExists(repositoryPath, urlRepository);

		commitRange = new CommitRange(repositoryPath, initialCommit, finalCommit);
		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = commitRange.getCommitsMergedIntoMasterByIds(commitsWithRefactorings);

		commitSmell = new CommitSmell(repositoryPath, commitsWithRefactoringMergedIntoMaster, listaLimiarTecnica, resultBaseFileName);
		commitSmell.useOldCache(USE_SMELLS_COMMIT_OLD_CACHE); 
	}

	public void getSmellRefactoredMethods() {
		try {
			SmellRefactoredMethod smellRefactoredMethod = new SmellRefactoredMethod(refactoringEvents, initialCommit, commitRange, commitSmell, resultBaseFileName);
			smellRefactoredMethod.getSmellRefactoredMethods();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void getSmellRefactoredClasses() {
		try {
			SmellRefactoredClass smellRefactoredClass = new SmellRefactoredClass(refactoringEvents, initialCommit, commitRange, commitSmell, resultBaseFileName);
			smellRefactoredClass.getSmellRefactoredClasses();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static int countRealPositive(LinkedHashMap<String, Integer> refactoringsCounter, HashSet<String> targetTefactoringTypes) {
		int realPositive = 0;
		for (String targetTefactoringType: targetTefactoringTypes) {
			realPositive += refactoringsCounter.getOrDefault(targetTefactoringType, 0);
		}
		return realPositive;
	}
	
}
