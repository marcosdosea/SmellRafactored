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
	private String localFolder;
	private String initialCommit;
	private String finalCommit;
	private List<LimiarTecnica> listaLimiarTecnica;

	private CommitRange commitRange;
	
	private ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
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
		
		RefactoringMinerWrapperManager refactoringMinerWrapperManager = new RefactoringMinerWrapperManager(urlRepository, localFolder, initialCommit, finalCommit, resultBaseFileName);

		// List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListWithoutCache();
		List<RefactoringMinerWrapperDto> refactoringDtoList = refactoringMinerWrapperManager.getRefactoringDtoListUsingJsonCache();

		listRefactoring = getRefactoringDataListFromRefactoringListIgnoringInitialCommit(refactoringDtoList);
		HashSet<String> commitsWithRefactorings = refactoringMinerWrapperManager.getCommitsWithRefactoringsFromRefactoringList(refactoringDtoList);

		logger.info("Total de refactorings encontrados: " + listRefactoring.size());
		
		
		GitService gitService = new GitServiceImpl();
		gitService.cloneIfNotExists(localFolder, urlRepository);

		commitRange = new CommitRange(localFolder, initialCommit, finalCommit);
		ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = commitRange.getCommitsMergedIntoMasterByIds(commitsWithRefactorings);

		commitSmell = new CommitSmell(localFolder, commitsWithRefactoringMergedIntoMaster, listaLimiarTecnica, resultBaseFileName);
		commitSmell.useOldCache(USE_SMELLS_COMMIT_OLD_CACHE); 
	}

	
	


	public void getSmellRefactoredMethods() {
		try {
			SmellRefactoredMethod smellRefactoredMethod = new SmellRefactoredMethod(listRefactoring, initialCommit, commitRange, commitSmell, resultBaseFileName);
			smellRefactoredMethod.getSmellRefactoredMethods();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public void getSmellRefactoredClasses() {
		try {
			SmellRefactoredClass smellRefactoredClass = new SmellRefactoredClass(listRefactoring, initialCommit, commitRange, commitSmell, resultBaseFileName);
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

	
	
	
	private ArrayList<RefactoringData> getRefactoringDataListFromRefactoringListIgnoringInitialCommit(List<RefactoringMinerWrapperDto> refactoringDtoList) {
		ArrayList<RefactoringData> refactoringDataList = new ArrayList<RefactoringData>(); 
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			if (refactoringDto != null) {
				if (!refactoringDto.commitId.equals(this.initialCommit)) {
					RefactoringData refactoringData = newRefactoringData(refactoringDto);
					refactoringDataList.add(refactoringData);
				}
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
