package org.smellrefactored;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
import org.eclipse.jgit.revwalk.RevCommit;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

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
	
	private String resultFileName;
	ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster;


	HashSet<String> listCommitEvaluated = new HashSet<String>();
	
	String fileRefactoringName;

	public SmellRefactoredManager(String urlRepository, String localFolder, String initialCommit, String finalCommit,
			List<LimiarTecnica> listaLimiarTecnica, String resultFileName) {
		this.urlRepository = urlRepository;
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultFileName = resultFileName;
		
		gitService = new GitServiceImpl();
		commitsWithRefactoringMergedIntoMaster = new ArrayList<CommitData>();

		fileRefactoringName = resultFileName + "-refactoring.csv";
		
		prepareSmellRefactored();
	}

	private void prepareSmellRefactored() {
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		
		try {
			repo = gitService.cloneIfNotExists(localFolder, urlRepository);

			File fileRefactoring = new File(fileRefactoringName);
			if (!fileRefactoring.exists()) {
				final PersistenceMechanism pmRefactoring = new CSVFile(fileRefactoringName, false);
				pmRefactoring.write("Commmit-id", "Refactring-name", "Refactoring-Type", "Code Element Left",
						"Code Element Right", "Class Before", "Class After");
				// detect list of refactoring between commits
				miner.detectBetweenCommits(repo, initialCommit, finalCommit, new RefactoringHandler() {
					@Override
					public void handle(String idCommit, List<Refactoring> refactorings) {
						for (Refactoring ref : refactorings) {
							pmRefactoring.write(idCommit, ref.getName(), ref.getRefactoringType(),
								ref.leftSide().size() > 0 ? ref.leftSide().get(0).getCodeElement() : 0,
								ref.rightSide().size() > 0 ? ref.rightSide().get(0).getCodeElement() : 0,
								ref.getInvolvedClassesBeforeRefactoring(),
								ref.getInvolvedClassesAfterRefactoring());
						}
					}
				});
			}

			// Obter lista de commits que possuem refatorações
			HashSet<String> commitsWithRefactorings = new HashSet<String>();
			try {
				CSVReader reader = new CSVReader(new FileReader(fileRefactoringName));
				String[] nextLine;
				nextLine = reader.readNext(); // descartar a primeira linha
				if (nextLine != null) {
					while ((nextLine = reader.readNext()) != null) {
						RefactoringData refactoringData = getRecordFromLine(nextLine);
						commitsWithRefactorings.add(refactoringData.getCommit());
						listRefactoring.add(refactoringData);
					}
				}
				reader.close();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			logger.info("Total de refactorings encontrados: " + listRefactoring.size());

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

			
			commitSmell = new CommitSmell(gitService, repo, commitsWithRefactoringMergedIntoMaster, localFolder, listaLimiarTecnica, resultFileName);
			
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
	
	public void getSmellRefactoredMethods() {
		try {
			SmellRefactoredMethod smellRefactoredMethod = new SmellRefactoredMethod(listRefactoring, initialCommit, commitsMergedIntoMaster, commitSmell, resultFileName);
			smellRefactoredMethod.getSmellRefactoredMethods();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	public void getSmellRefactoredClasses() {
		try {
			SmellRefactoredClass smellRefactoredClass = new SmellRefactoredClass(listRefactoring, initialCommit, commitsMergedIntoMaster, commitSmell, resultFileName);
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
	
	
	
	private static RefactoringData getRecordFromLine(String[] line) {
		RefactoringData result = new RefactoringData();
		try {
			result.setCommit(line[0]);
			result.setRefactoringName(line[1]);
			result.setRefactoringType(line[2]);
			result.setLeftSide(line[3]);
			result.setRightSide(line[4]);
			result.setInvolvedClassesBefore(line[5]);
			result.setInvolvedClassesAfter(line[6]);
			
			
			
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
		} catch (Exception e) {
			logger.error(e.getMessage() + " (line:" + line[0] + ", " + line[1] + ", " + line[2] + ", " + line[3] + ", " + line[4] + ", " + line[5] + ", " + line[6] + ")");
		}
		return result;
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
