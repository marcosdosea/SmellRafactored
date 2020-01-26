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
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperManager;
import org.smellrefactored.refactoringminer.wrapper.RefactoringMinerWrapperDto;

public class SmellRefactoredManager {

	
	final boolean USE_SMELLS_COMMIT_CACHE = false; 
	
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
		
		RefactoringMinerWrapperManager refactoringMinerWrapperManager = new RefactoringMinerWrapperManager(urlRepository, localFolder, initialCommit, finalCommit, resultBaseFileName);

		// List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListWithoutCache();
		List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListUsingJsonCache();

		listRefactoring = getRefactoringDataListFromRefactoringList(refactoringDtoList);
		HashSet<String> commitsWithRefactorings = refactoringMinerWrapperManager.getCommitsWithRefactoringsFromRefactoringList(refactoringDtoList);

		logger.info("Total de refactorings encontrados: " + listRefactoring.size());
		
		
		repo = gitService.cloneIfNotExists(localFolder, urlRepository);

		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = getCommitsWithRefactoringMergedIntoMaster(commitsWithRefactorings);

		commitSmell = new CommitSmell(gitService, repo, commitsWithRefactoringMergedIntoMaster, localFolder, listaLimiarTecnica, resultBaseFileName);
		commitSmell.useCache(USE_SMELLS_COMMIT_CACHE); 
	}

	
	
	private ArrayList<CommitData> getCommitsWithRefactoringMergedIntoMaster(HashSet<String> refactoringCommitIds)
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
		
		

		Collections.sort(commitsNotMergedIntoMaster);
		Collections.sort(commitsMergedIntoMaster);

		
		getChainedOrderedCommitsMergedIntoMaster();

		
		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = commitsWithRefactoringMergedIntoMasterFromRefactoringCommitIds(
				refactoringCommitIds);
		
		
		return commitsWithRefactoringMergedIntoMaster;
	}


	private ArrayList<CommitData> commitsWithRefactoringMergedIntoMasterFromRefactoringCommitIds(
			HashSet<String> refactoringCommitIds) {
		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = new ArrayList<CommitData>();
		Iterator<CommitData> it = commitsMergedIntoMaster.iterator();
		while (it.hasNext()) {
			CommitData commit = it.next();
			if (refactoringCommitIds.contains(commit.getId())) {
				commitsWithRefactoringMergedIntoMaster.add(commit);
			}
		}
		return commitsWithRefactoringMergedIntoMaster;
	}


	private void getChainedOrderedCommitsMergedIntoMaster() {
		Iterator<CommitData> it = commitsMergedIntoMaster.iterator();
		CommitData previousCommit = null;
		while (it.hasNext()) {
			CommitData commit = it.next();
			commit.setPrevious(previousCommit);
			if (previousCommit!=null) {
				previousCommit.setNext(commit);
			}
			previousCommit = commit; 
		}
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

	
	
	
	private ArrayList<RefactoringData> getRefactoringDataListFromRefactoringList(List<RefactoringMinerWrapperDto> refactoringDtoList) {
		ArrayList<RefactoringData> refactoringDataList = new ArrayList<RefactoringData>(); 
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			if (refactoringDto != null) {
				RefactoringData refactoringData = newRefactoringData(refactoringDto);
				refactoringDataList.add(refactoringData);
			}
		}
    return (refactoringDataList);
	}
	
	private RefactoringData newRefactoringData(RefactoringMinerWrapperDto refactoringDto) {
		RefactoringData result = new RefactoringData();
		result.setCommit(refactoringDto.commitId);
		result.setRefactoringName(refactoringDto.name);
		result.setRefactoringType(refactoringDto.type.toString());
		if ( (refactoringDto.leftSide != null) && (refactoringDto.leftSide.size()>0) )  {
			result.setFileNameBefore(makeFilePathCompatibleWithDesignRoleSmell(refactoringDto.leftSide.get(0).getFilePath()));
			result.setLeftSide(refactoringDto.leftSide.get(0).getCodeElement());
		}
		if ( (refactoringDto.rightSide != null) && (refactoringDto.rightSide.size()>0) ) {
			result.setFileNameAfter(makeFilePathCompatibleWithDesignRoleSmell(refactoringDto.rightSide.get(0).getFilePath()));
			result.setRightSide(refactoringDto.rightSide.get(0).getCodeElement());
		}
		result.setInvolvedClassesBefore(refactoringDto.involvedClassesBefore.toString());
		result.setInvolvedClassesAfter(refactoringDto.involvedClassesAfter.toString());

		
		completRefactoringData(result);
		return result;
	}

	
	private String makeFilePathCompatibleWithDesignRoleSmell(String filePath) {
		String newFilePath = this.localFolder + "\\" + filePath.replace("/","\\");
		newFilePath = newFilePath.replace("\\\\", "\\");
		return (newFilePath);
	}
	
	private static void completRefactoringData(RefactoringData result) {
		if (result.getRefactoringType().contains("VARIABLE")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
		} else if (result.getRefactoringType().contains("ATTRIBUTE")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
		} else if (result.getRefactoringType().contains("PARAMETER")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
		} else if (result.getRefactoringType().contains("RETURN_TYPE")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
		} else if (result.getRefactoringType().contains("OPERATION")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
			result.setNomeMetodo(SmellRefactoredMethod.extrairNomeMetodo(result.getLeftSide()));
		} else if (result.getRefactoringType().contains("METHOD")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
			result.setNomeMetodo(SmellRefactoredMethod.extrairNomeMetodo(result.getLeftSide()));
		} else  if (result.getRefactoringType().contains("EXTRACT_SUPERCLASS")) {
			result.setNomeClasse(result.getLeftSide());
		} else if (result.getRefactoringType().contains("CLASS")) {
			result.setNomeClasse(getClassNameFromInvolvedClassesBefore(result));
		} else if (result.getRefactoringType().contains("PACKAGE")) {
			/// @TODO: to implement
		} else if (result.getRefactoringType().contains("FOLDER")) {
			/// @TODO: to implement
		}
		if (result.getNomeClasse() == null) {
			// logger.error("Classe Name is NULL (line:" + line[0] + ", " + line[1] + ", " + line[2] + ", " + line[3] + ", " + line[4] + ", " + line[5] + ", " + line[6] + ")");
		}
		if (result.getNomeClasse() != null) {
			if (result.getNomeClasse() != "") {
				result.setClassDesignRole( getDesignRoleByClassName( result.getNomeClasse()) );
			}
		}
	}
	
	private static String getClassNameFromInvolvedClassesBefore(RefactoringData refactoringData) {
		return refactoringData.getInvolvedClassesBefore().replace("[", "").replace("]", "");
	}
	
	private static String getDesignRoleByClassName(String className) {
		/// @TODO: refinar detecção de DesignRole 
		// DesignRole designRole = new DesignRole();
		String result = ""; /// = designRole.findDefaultDesignRole(className); // private method in DesignRole 
		if (result=="") {
			result = "Undefined";
		}
		return result.toUpperCase();
	}
	
	
	
	
	
}
