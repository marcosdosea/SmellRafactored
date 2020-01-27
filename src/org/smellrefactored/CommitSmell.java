package org.smellrefactored;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.designroleminer.MetricReport;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.ClassDataSmelly;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.smelldetector.model.MethodDataSmelly;
import org.designroleminer.threshold.TechniqueExecutor;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class CommitSmell {

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	private Date startAt = new Date();
	
	private GitService gitService;
	private Repository repo;
	private ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster;
	private String localFolder;
	private List<LimiarTecnica> listaLimiarTecnica;
	String resultFileName;
	
	private boolean usingOldCache = false;
	
	private LinkedHashMap<String, LimiarTecnica> techniquesThresholds;
	
	PersistenceMechanism pmResultSmellRefactoredCommit;

	HashSet<String> listCommitEvaluated = new HashSet<String>();

	
	public CommitSmell(GitService gitService, Repository repo, ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster, String localFolder, List<LimiarTecnica> listaLimiarTecnica, String resultFileName) {
		this.gitService = gitService;
		this.repo = repo;
		this.commitsWithRefactoringMergedIntoMaster = commitsWithRefactoringMergedIntoMaster;
		this.localFolder = localFolder;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultFileName = resultFileName;
		
		techniquesThresholds = new LinkedHashMap<String, LimiarTecnica>();
		for (LimiarTecnica limiarTecnica : this.listaLimiarTecnica) {
			techniquesThresholds.put(limiarTecnica.getTecnica(), limiarTecnica);
		}

		pmResultSmellRefactoredCommit = new CSVFile(resultFileName + "-smellRefactored-commit.csv", false);
	}
	
	public void useOldCache(boolean onOff) {
		this.usingOldCache = onOff;
		if (this.usingOldCache) {
			logger.warn("The smells commit OLD CACHE was turned ON.");
			logger.warn("Use smells commit OLD cache only to speed of maintenance and development on this project.");
			logger.warn("Clear the cache whenever the weather is cloudy ;)");
		} else {
			logger.info("The smells commit OLD CACHE was turned OFF.");
		}
	}
	
	public FilterSmellResult obterSmellsPreviousCommit(String commitId) {
		CommitData previousCommit = null;
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			if (commit.getId().equals(commit.getPrevious().getId())) {
				logger.error("Commit " + commit.getId() + " tem a si mesmo como commit prévio.");
				continue;
			}
			if (commit.getId().equals(commitId)) {
				previousCommit = commit.getPrevious();
				break;
			}
		}
		FilterSmellResult smellsPreviousCommit = null;
		if (previousCommit != null) {
			try {
				smellsPreviousCommit = obterSmellsCommit(previousCommit.getId());
			} catch (Exception e) {
				e.getStackTrace();
			}
		}
		return smellsPreviousCommit;
	}
	
	public FilterSmellResult obterSmellsCommit(String commitId) throws Exception {
		FilterSmellResult smellsCommit;
		if ( (cacheExists(commitId)) && (usingOldCache || isRecentCache(commitId)) ) {
			smellsCommit = getSmellsCommitFromCache(commitId);
		} else {
			smellsCommit = getSmellsCommitFromGitRepository(commitId);
			saveSmellsCommitToCache(smellsCommit);
		}
		return (smellsCommit);
	}

	private boolean isRecentCache(String commitId) throws Exception {
		String cacheFileName = getCacheFileName(commitId);
		File cacheFile = new File(cacheFileName);
		return (this.startAt.before(new Date(cacheFile.lastModified()))); 
	}
	private boolean cacheExists(String commitId) throws Exception {
		String cacheFileName = getCacheFileName(commitId);
		File cacheFile = new File(cacheFileName);
		return (cacheFile.exists());
	}
	private void saveSmellsCommitToCache(FilterSmellResult smellsCommit) throws Exception {
		Gson gson = new Gson();
		String cacheFileName = getCacheFileName(smellsCommit.getCommitId());
		File existingFile = new File(cacheFileName);
		if (existingFile.exists()) {
			logger.info("Erasing old cache for commit " + smellsCommit.getCommitId() + "...");
			existingFile.delete();
		}
		String cacheFileNameTemp = cacheFileName.replace(".json", ".temp");
		logger.info("Saving smells to cache for commit " + smellsCommit.getCommitId() + "...");
		FileWriter cacheFileHandler = new FileWriter(cacheFileNameTemp);
		cacheFileHandler.append(gson.toJson(smellsCommit));
		cacheFileHandler.close();
		File tempfile = new File(cacheFileNameTemp);
		File newfile = new File(cacheFileName);
		tempfile.renameTo(newfile);
	}
	private FilterSmellResult getSmellsCommitFromCache(String commitId) throws Exception {
		logger.info("Getting smells from cache for commit " + commitId + "...");
		Gson gson = new Gson();  
		String cacheFileName = getCacheFileName(commitId);
		JsonReader reader = new JsonReader(new FileReader(cacheFileName));
		return (gson.fromJson(reader, FilterSmellResult.class));  
	}
	private String getCacheFileName(String commitId) throws Exception {
		return (this.resultFileName + "-smellsCommit-" + commitId + "-cache.json");
	}
	
	
	private FilterSmellResult getSmellsCommitFromGitRepository(String commitId) throws Exception {
		logger.info("Getting metrics for the commit " + commitId + "...");
		ArrayList<String> projetosAnalisar = new ArrayList<String>();
		projetosAnalisar.add(localFolder);
		TechniqueExecutor executor = new TechniqueExecutor(repo, gitService);
		MetricReport report = executor.getMetricsFromProjects(projetosAnalisar,
				System.getProperty("user.dir") + "\\metrics\\", commitId);

		if (listCommitEvaluated.size() == 0) {
			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");
		}
		if (!listCommitEvaluated.contains(commitId)) {
			listCommitEvaluated.add(commitId);
			pmResultSmellRefactoredCommit.write(commitId, report.getNumberOfClasses(), report.getNumberOfMethods(),
					report.getSystemLOC());
		}
		

		
		logger.info("Gerando smells com a lista de problemas de design encontrados...");
		FilterSmellResult smellsCommitInitial = FilterSmells.filtrar(report.all(), listaLimiarTecnica, commitId);
		FilterSmells.gravarMetodosSmell(smellsCommitInitial.getMetodosSmell(), resultFileName + "-smells-commit-initial-method.csv");
		FilterSmells.gravarClassesSmell(smellsCommitInitial.getClassesSmell(), resultFileName + "-smells-commit-initial-class.csv");
		consistMethodNotSmelly(smellsCommitInitial);
		consistClassNotSmelly(smellsCommitInitial);
		return smellsCommitInitial;
	}
	
	private void consistMethodNotSmelly(FilterSmellResult smellsCommitInitial) {
		for (MethodDataSmelly methodSmelly : smellsCommitInitial.getMetodosSmell()) {
			for (MethodDataSmelly methodNotSmelly : smellsCommitInitial.getMetodosNotSmelly()) {
				if (methodSmelly.getDiretorioDaClasse().equals(methodNotSmelly.getDiretorioDaClasse())
					&& methodSmelly.getNomeClasse().equals(methodNotSmelly.getNomeClasse())
					&& methodSmelly.getNomeMetodo().equals(methodNotSmelly.getNomeMetodo())
					&& methodSmelly.getCommit().equals(methodNotSmelly.getCommit())
					&& methodSmelly.getCharInicial() == methodNotSmelly.getCharInicial()) {
					logger.error("Method found in the list of smells and non-smells.:" + methodSmelly.toString());
				}
			}
		}
	}

	private void consistClassNotSmelly(FilterSmellResult smellsCommitInitial) {
		for (ClassDataSmelly classSmelly : smellsCommitInitial.getClassesSmell()) {
			for (ClassDataSmelly classNotSmelly : smellsCommitInitial.getClassesNotSmelly()) {
				if (classSmelly.getDiretorioDaClasse().equals(classSmelly.getDiretorioDaClasse())
			        && classSmelly.getNomeClasse().equals(classNotSmelly.getNomeClasse())
					&& classSmelly.getCommit().equals(classNotSmelly.getCommit())) {
					logger.error("Class found in the list of smells and non-smells.:" + classSmelly.toString());
				}
			}
		}
	}

	
	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
 		return (this.techniquesThresholds);
	}
	
	
}
