package org.smellrefactored;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import org.designroleminer.MetricReport;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.threshold.TechniqueExecutor;
import org.eclipse.jgit.lib.Repository;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class CommitSmell {
	
	final private int MAXIMUM_COMMITS_IN_MEMORY_CACHE = 100;
	
	private GitService gitService;
	private Repository repo;
	private String localFolder;
	private List<LimiarTecnica> listaLimiarTecnica;

	String resultFileName;
	
	private Date startAt = new Date();
	private boolean usingOldCache = false;
	LinkedHashMap<String, FilterSmellResult> memoryCache = new LinkedHashMap<String, FilterSmellResult>();  
	
	private LinkedHashMap<String, LimiarTecnica> techniquesThresholds;
	
	PersistenceMechanism pmResultSmellRefactoredCommit;

	HashSet<String> listCommitEvaluated = new HashSet<String>();

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	public CommitSmell(String localFolder, ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster, List<LimiarTecnica> listaLimiarTecnica, String resultFileName) throws Exception {
		this.localFolder = localFolder;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultFileName = resultFileName;
		
		gitService = new GitServiceImpl();
		repo = gitService.openRepository(localFolder);
		
		techniquesThresholds = new LinkedHashMap<String, LimiarTecnica>();
		for (LimiarTecnica limiarTecnica : this.listaLimiarTecnica) {
			techniquesThresholds.put(limiarTecnica.getTecnica(), limiarTecnica);
		}

		pmResultSmellRefactoredCommit = new CSVFile(resultFileName + "-smells-commit.csv", false);
	}
	
	public void useOldCache(boolean onOff) {
		this.usingOldCache = onOff;
		if (this.usingOldCache) {
			logger.warn("The smells commit OLD CACHE was turned ON.");
			logger.warn("Use smells commit OLD CACHE only to speed of maintenance and development on this project.");
			logger.warn("Clear or disable OLD CACHE whenever the weather is cloudy ;)");
		} else {
			logger.info("The smells commit OLD CACHE was turned OFF.");
		}
	}

	public ArrayList<FilterSmellResult> obterSmellsCommits(ArrayList<String> commitIds) throws Exception {
		ArrayList<FilterSmellResult> result = new ArrayList<FilterSmellResult>();
		for (String commitId: commitIds) {
			result.add(obterSmellsCommit(commitId));
		}
		return (result);
	}

	public FilterSmellResult obterSmellsCommit(String commitId) throws Exception {
		FilterSmellResult smellResult;
		if (memoryCache.containsKey(commitId)) {
			smellResult = memoryCache.get(commitId);
		} else {
			if ( (cacheExists(commitId)) && (usingOldCache || isRecentCache(commitId)) ) {
				smellResult = getSmellsCommitFromCache(commitId);
			} else {
				smellResult = getSmellsCommitFromGitRepository(commitId);
				saveSmellsCommitToCache(smellResult);
			}
			if (memoryCache.size() >= MAXIMUM_COMMITS_IN_MEMORY_CACHE) {
				memoryCache.clear();
			}
			memoryCache.put(commitId, smellResult);
		}
		if (smellResult == null) {
			throw new Exception("Null response to the smell query from commit " + commitId + ".");
		}
		return (smellResult);
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
		existingFile.mkdirs();
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
		String cacheBaseFileName = Paths.get(resultFileName).getParent().getFileName() + "\\cache\\smell\\" + Paths.get(resultFileName).getFileName() + "\\" + Paths.get(resultFileName).getFileName();
		return (cacheBaseFileName + "-smells-commit-" + commitId + "-cache.json");
	}
	
	private FilterSmellResult getSmellsCommitFromGitRepository(String commitId) throws Exception {
		logger.info("Getting metrics for the commit " + commitId + "...");
		ArrayList<String> projetosAnalisar = new ArrayList<String>();
		projetosAnalisar.add(localFolder);
		TechniqueExecutor executor = new TechniqueExecutor(repo, gitService);
		MetricReport report = executor.getMetricsFromProjects(projetosAnalisar, System.getProperty("user.dir") + "\\metrics\\", commitId);
		if (listCommitEvaluated.size() == 0) {
			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");
		}
		if (!listCommitEvaluated.contains(commitId)) {
			listCommitEvaluated.add(commitId);
			pmResultSmellRefactoredCommit.write(commitId, report.getNumberOfClasses(), report.getNumberOfMethods(), report.getSystemLOC());
		}
		logger.info("Gerando smells com a lista de problemas de design encontrados...");
		FilterSmellResult smellResult = FilterSmells.filtrar(report.all(), listaLimiarTecnica, commitId);
		// FilterSmells.gravarMetodosSmell(smellResult.getMetodosSmell(), resultFileName + "-smells-commit-" + commitId + "-method.csv");
		// FilterSmells.gravarClassesSmell(smellResult.getClassesSmell(), resultFileName + "-smells-commit-" + commitId + "-class.csv");
		CommitMethodSmell.consistMethodNotSmelly(smellResult);
		CommitClassSmell.consistClassNotSmelly(smellResult);
		return smellResult;
	}
	
	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
 		return (this.techniquesThresholds);
	}
	
}
