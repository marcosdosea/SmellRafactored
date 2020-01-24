package org.smellrefactored;

import java.io.File;
import java.io.FileReader;
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
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class RefactoringMinerWrapper {
	
	private String urlRepository;
	private String localFolder;
	private String initialCommit;
	private String finalCommit;
	private String resultBaseFileName;
	
	private GitService gitService;
	private Repository repo;

	private ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
	private HashSet<String> commitsWithRefactorings = new HashSet<String>();
	
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	
	public RefactoringMinerWrapper(String urlRepository, String localFolder, String initialCommit, String finalCommit, String resultBaseFileName) throws Exception {
		this.urlRepository = urlRepository;
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.resultBaseFileName = resultBaseFileName;

		gitService = new GitServiceImpl();
		repo = gitService.cloneIfNotExists(this.localFolder, this.urlRepository);
		
		process();
	}

	private void process() throws Exception {
		String csvFileRefactoringVersion = "1";
		String csvFileRefactoringName = resultBaseFileName + "-refactoring-v" + csvFileRefactoringVersion + "-"  + initialCommit + "-" + finalCommit + ".csv";
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		File fileRefactoring = new File(csvFileRefactoringName);
		if (!fileRefactoring.exists()) {
			generateRefactoringFile(csvFileRefactoringName, miner);
		}
		generateListRefactoringAndCommitsWithRefactoringFromCsvFile(csvFileRefactoringName);
	}
	
	private void generateRefactoringFile(String fileRefactoringName, GitHistoryRefactoringMiner miner) throws Exception {
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
	
	private void generateListRefactoringAndCommitsWithRefactoringFromCsvFile(String fileRefactoringName) {
		try {
			CSVReader reader = new CSVReader(new FileReader(fileRefactoringName));
			String[] nextLine;
			nextLine = reader.readNext(); // ignore header
			if (nextLine != null) {
				while ((nextLine = reader.readNext()) != null) {
					RefactoringData refactoringData = getRecordFromLine(nextLine);
					commitsWithRefactorings.add(refactoringData.getCommit());
					listRefactoring.add(refactoringData);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
	}

	private static RefactoringData getRecordFromLine(String[] line) {
		RefactoringData result = new RefactoringData();
		try {
			// logger.info("Refactoring file line content [:" + line[0] + ", " + line[1] + ", " + line[2] + ", " + line[3] + ", " + line[4] + ", " + line[5] + ", " + line[6] + "]");
			result.setCommit(line[0]);
			result.setRefactoringName(line[1]);
			result.setRefactoringType(line[2]);
			result.setLeftSide(line[3]);
			result.setRightSide(line[4]);
			result.setInvolvedClassesBefore(line[5]);
			result.setInvolvedClassesAfter(line[6]);
			completRefactoringData(result);
		} catch (Exception e) {
			e.getStackTrace();
		}
		return result;
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
	
	
	public ArrayList<RefactoringData> getListRefactoring() {
		return (this.listRefactoring);
	}
	
	public HashSet<String> getCommitsWithRefactorings() {
		return (this.commitsWithRefactorings);
	}

}
