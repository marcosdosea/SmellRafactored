package org.smellrefactored;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmellRefactoredManager {

	final boolean ANALYZE_FIRST_COMMIT_ONLY = true; // Very, very, very slow!

	final boolean USE_SMELLS_COMMIT_OLD_CACHE = false; // This can ignore new adjustments in DesignRoleMiner.

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private String repositoryUrl;
	private String repositoryPath;
	private String initialCommitId;
	private String finalCommitId;
	private List<LimiarTecnica> techniqueThresholds;

	private CommitRange commitRange;
	private RefactoringEvents refactoringEvents;
	
	private ArrayList<String> smellCommitIds = new ArrayList<String>(); 
	private CommitSmell commitSmell;
	
	private String resultBaseFileName;

	HashSet<String> listCommitEvaluated = new HashSet<String>();
	
	public SmellRefactoredManager(String repositoryUrl, String repositoryPath, String initialCommitId, String finalCommitId,
			List<LimiarTecnica> listaLimiarTecnica, String resultBaseFileName) {
		this.repositoryUrl = repositoryUrl;
		this.repositoryPath = repositoryPath;
		this.initialCommitId = initialCommitId;
		this.finalCommitId = finalCommitId;
		this.techniqueThresholds = listaLimiarTecnica;
		this.resultBaseFileName = resultBaseFileName;

		logger.info("");
		logger.info("******");
		logger.info("** REPOSITORY: " + this.repositoryUrl + " ==>> " + this.repositoryPath);
		logger.info("******");
		try {
			prepareSmellRefactored();
		} catch (Exception e) {
			 StringWriter sw = new StringWriter();
	            e.printStackTrace(new PrintWriter(sw));
	            String exceptionAsString = sw.toString();
	            System.out.println();
				try {
					Files.writeString(Paths.get(resultBaseFileName + "-error.txt"), exceptionAsString);
				} catch (IOException e1) {
					// do nothing
				}
			e.printStackTrace();
		}
	}


	private void prepareSmellRefactored() throws Exception {
		if (initialCommitId.equals(finalCommitId)) {
			throw new Exception("At least 2 commits are required in the range of commits to be analyzed.");
		}

		GitService gitService = new GitServiceImpl();
		gitService.cloneIfNotExists(repositoryPath, repositoryUrl);
		commitRange = new CommitRange(repositoryUrl, repositoryPath, initialCommitId, finalCommitId);
		logger.info("Commits found: " + commitRange.size());
		
		refactoringEvents = new RefactoringEvents(this.repositoryPath, commitRange, resultBaseFileName);
		logger.info("Refactorings found: " + refactoringEvents.size());

		if (ANALYZE_FIRST_COMMIT_ONLY) {
			smellCommitIds.add(initialCommitId);
		} else {
			smellCommitIds = commitRange.getIds();
			smellCommitIds.remove(finalCommitId);
		}
		
		commitSmell = new CommitSmell(repositoryPath, techniqueThresholds, resultBaseFileName);
		commitSmell.useOldCache(USE_SMELLS_COMMIT_OLD_CACHE);
	}

	public void getSmellRefactoredMethods() {
		try {
			SmellRefactoredMethod smellRefactoredMethod = new SmellRefactoredMethod(refactoringEvents, smellCommitIds, commitRange, commitSmell, resultBaseFileName);
			smellRefactoredMethod.getSmellRefactoredMethods();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void getSmellRefactoredClasses() {
		try {
			SmellRefactoredClass smellRefactoredClass = new SmellRefactoredClass(refactoringEvents, smellCommitIds, commitRange, commitSmell, resultBaseFileName);
			smellRefactoredClass.getSmellRefactoredClasses();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

}
