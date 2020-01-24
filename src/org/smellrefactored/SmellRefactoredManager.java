package org.smellrefactored;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmellRefactoredManager {

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private String urlRepository;
	private String localFolder;
	private String initialCommit;
	private String finalCommit;
	private List<LimiarTecnica> listaLimiarTecnica;
	private GitService gitService;
	private Repository repo;

	private ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
	private ArrayList<CommitData> commitsMergedIntoMaster = new ArrayList<CommitData>();
	private CommitSmell commitSmell;
	
	private String resultBaseFileName;


	HashSet<String> listCommitEvaluated = new HashSet<String>();
	
	public SmellRefactoredManager(String urlRepository, String localFolder, String initialCommit, String finalCommit,
			List<LimiarTecnica> listaLimiarTecnica, String resultBaseFileName) {
		this.urlRepository = urlRepository;
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultBaseFileName = resultBaseFileName;
		
		gitService = new GitServiceImpl();

		try {
			prepareSmellRefactored();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	private void prepareSmellRefactored() throws Exception {
		
		RefactoringMinerWrapper refactoringMinerWrapper = new RefactoringMinerWrapper(urlRepository, localFolder, initialCommit, finalCommit, resultBaseFileName);
		listRefactoring = refactoringMinerWrapper.getListRefactoring();
		HashSet<String> commitsWithRefactorings = refactoringMinerWrapper.getCommitsWithRefactorings();
		logger.info("Total de refactorings encontrados: " + listRefactoring.size());
		
		
		repo = gitService.cloneIfNotExists(localFolder, urlRepository);

		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = getCommitsWithRefactoringMergedIntoMaster(commitsWithRefactorings);

		commitSmell = new CommitSmell(gitService, repo, commitsWithRefactoringMergedIntoMaster, localFolder, listaLimiarTecnica, resultBaseFileName);
	}

	private ArrayList<CommitData> getCommitsWithRefactoringMergedIntoMaster(HashSet<String> commitsWithRefactorings)
			throws Exception, GitAPIException, NoHeadException, MissingObjectException, IncorrectObjectTypeException,
			IOException, AmbiguousObjectException {
		// Filtra os commits com refactoring cujo commit feito no master ou pull request
		// realizados no master
		ArrayList<CommitData> commitsNotMergedIntoMaster = new ArrayList<CommitData>();
		Git git = new Git(repo);
		gitService.checkout(repo, finalCommit);
			Iterable<RevCommit> log = git.log().call();
		Iterator<RevCommit> logIterator = log.iterator();
		RevWalk revWalk = new RevWalk(repo, 3);
		RevCommit masterHead = revWalk.parseCommit(repo.resolve("refs/heads/master"));
		while (logIterator.hasNext()) {
			RevCommit currentCommit = logIterator.next();
			CommitData commitData = new CommitData();
			commitData.setId(currentCommit.getId().getName());
			commitData.setFullMessage(currentCommit.getFullMessage());
			commitData.setShortMessage(currentCommit.getShortMessage());
			commitData.setDate(new Date(currentCommit.getCommitTime() * 1000L));
			ObjectId id = repo.resolve(commitData.getId());
			RevCommit otherHead = revWalk.parseCommit(id);
			if (revWalk.isMergedInto(otherHead, masterHead)) {
				if ((otherHead.getParentCount() == 1)
						|| (otherHead.getShortMessage().toUpperCase().contains("MERGE")
								&& otherHead.getShortMessage().toUpperCase().contains("PULL"))) {
					commitsMergedIntoMaster.add(commitData);
				} else {
					commitsNotMergedIntoMaster.add(commitData);
				}
			}
			revWalk.close();
			revWalk.dispose();
		}
		git.close();
		
		
		// @TODO: Verificar: o código abaixo estava apenas implementado para análise de classes
		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = new ArrayList<CommitData>();
		Collections.sort(commitsMergedIntoMaster);
		Collections.sort(commitsNotMergedIntoMaster);
		Iterator<CommitData> it = commitsMergedIntoMaster.iterator();
		CommitData previousCommit = null;
		while (it.hasNext()) {
			CommitData commit = it.next();
			commit.setPrevious(previousCommit);
			previousCommit = new CommitData();
			previousCommit.setDate(commit.getDate());
			previousCommit.setFullMessage(commit.getFullMessage());
			previousCommit.setShortMessage(commit.getShortMessage());
			previousCommit.setId(commit.getId());
			if (commitsWithRefactorings.contains(commit.getId())) {
				commitsWithRefactoringMergedIntoMaster.add(commit);
			}
		}
		// Fim do bloco de código a verificar
		return commitsWithRefactoringMergedIntoMaster;
	}

	
	public void getSmellRefactoredMethods() {
		try {
			SmellRefactoredMethod smellRefactoredMethod = new SmellRefactoredMethod(listRefactoring, initialCommit, commitsMergedIntoMaster, commitSmell, resultBaseFileName);
			smellRefactoredMethod.getSmellRefactoredMethods();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void getSmellRefactoredClasses() {
		try {
			SmellRefactoredClass smellRefactoredClass = new SmellRefactoredClass(listRefactoring, initialCommit, commitsMergedIntoMaster, commitSmell, resultBaseFileName);
			smellRefactoredClass.getSmellRefactoredClasses();
		} catch (Exception e) {
			logger.error(e.getMessage());
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
