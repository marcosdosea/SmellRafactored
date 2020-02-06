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
import org.smellrefactored.classes.CommitClassSmell;
import org.smellrefactored.methods.CommitMethodSmell;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class CommitSmell {
	
	final private int MAXIMUM_SMELLS_AND_NOT_SMELLS_IN_MEMORY_CACHE = 10 * 10000;
	
	private GitService gitService;
	private Repository repo;
	private String localFolder;
	private List<LimiarTecnica> techniqueThresholds;

	private String resultFileName;
	
	private Date startAt = new Date();
	private boolean usingOldCache = false;
	private LinkedHashMap<String, FilterSmellResult> memoryCache = new LinkedHashMap<String, FilterSmellResult>();  
	
	private LinkedHashMap<String, LimiarTecnica> techniquesThresholds;
	
	private PersistenceMechanism pmResultSmellRefactoredCommit;

	private HashSet<String> listCommitEvaluated = new HashSet<String>();
	
	private int amountOfSmellsInMemoryCache = 0;
	private int memoryCacheHits = 0;

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	public CommitSmell(String localFolder, List<LimiarTecnica> listaLimiarTecnica, String resultFileName) throws Exception {
		this.localFolder = localFolder;
		this.techniqueThresholds = listaLimiarTecnica;
		this.resultFileName = resultFileName;
		
		gitService = new GitServiceImpl();
		repo = gitService.openRepository(localFolder);
		
		techniquesThresholds = new LinkedHashMap<String, LimiarTecnica>();
		for (LimiarTecnica limiarTecnica : this.techniqueThresholds) {
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
			logger.info("The smells commit old cache was turned OFF.");
		}
	}

	public ArrayList<FilterSmellResult> getSmellsFromCommits(ArrayList<String> commitIds) throws Exception {
		ArrayList<FilterSmellResult> result = new ArrayList<FilterSmellResult>();
		for (String commitId: commitIds) {
			result.add(getSmellsFromCommit(commitId));
		}
		return (result);
	}

	public FilterSmellResult getSmellsFromCommit(String commitId) throws Exception {
		FilterSmellResult smellResult;
		if ( (MAXIMUM_SMELLS_AND_NOT_SMELLS_IN_MEMORY_CACHE>0) && (memoryCache.containsKey(commitId)) ) {
			smellResult = memoryCache.get(commitId);
			memoryCacheHits++;
		} else {
			if ( (cacheExists(commitId)) && (usingOldCache || isRecentCache(commitId)) ) {
				smellResult = getSmellsCommitFromCache(commitId);
			} else {
				smellResult = getSmellsCommitFromGitRepository(commitId);
				saveSmellsCommitToCache(smellResult);
			}
			if (MAXIMUM_SMELLS_AND_NOT_SMELLS_IN_MEMORY_CACHE>0) {
				if ((amountOfSmellsInMemoryCache + countSmellsInSmellResult(smellResult)) >= MAXIMUM_SMELLS_AND_NOT_SMELLS_IN_MEMORY_CACHE) {
					// logger.info("Clearing " + CommitSmell.class + " memory cache with " + amountOfSmellsInMemoryCache + " smells/not smells from " + memoryCache.size() + " commits.");
					memoryCache.clear();
					amountOfSmellsInMemoryCache = 0;
				}
				memoryCache.put(commitId, smellResult);
				amountOfSmellsInMemoryCache += countSmellsInSmellResult(smellResult);
			}
		}
		if (smellResult == null) {
			throw new Exception("Null response to the smell query from commit " + this.localFolder + " " + commitId + ".");
		}
		return (smellResult);
	}
	
	private int countSmellsInSmellResult(FilterSmellResult smellResult) {
		return (smellResult.getClassesSmell().size() 
				+ smellResult.getClassesNotSmelly().size()
				+ smellResult.getMetodosSmell().size()
				+ smellResult.getMetodosNotSmelly().size()
		);
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
			// logger.info("Erasing old cache for commit " + this.localFolder + " " + smellsCommit.getCommitId() + "...");
			existingFile.delete();
		}
		String cacheFileNameTemp = cacheFileName.replace(".json", ".temp");
		logger.info("Saving smells to cache for commit " + this.localFolder + " " + smellsCommit.getCommitId() + "...");
		FileWriter cacheFileHandler = new FileWriter(cacheFileNameTemp);
		cacheFileHandler.append(gson.toJson(smellsCommit));
		cacheFileHandler.close();
		File tempfile = new File(cacheFileNameTemp);
		File newfile = new File(cacheFileName);
		tempfile.renameTo(newfile);
	}
	
	private FilterSmellResult getSmellsCommitFromCache(String commitId) throws Exception {
		logger.info("Getting smells from cache for commit " + this.localFolder + " " + commitId + "...");
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
		logger.info("Detecting code smells based on threshold derivation techniques...");
		FilterSmellResult smellResult = FilterSmells.filtrar(report.all(), techniqueThresholds, commitId);
		// FilterSmells.gravarMetodosSmell(smellResult.getMetodosSmell(), resultFileName + "-smells-commit-" + commitId + "-method.csv");
		// FilterSmells.gravarClassesSmell(smellResult.getClassesSmell(), resultFileName + "-smells-commit-" + commitId + "-class.csv");
		CommitMethodSmell.consistMethodNotSmelly(smellResult);
		CommitClassSmell.consistClassNotSmelly(smellResult);
		return smellResult;
	}
	
	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
 		return (this.techniquesThresholds);
	}
	
	public void resetMemoryCacheHits() {
		this.memoryCacheHits = 0;
	}	

	public int getMemoryCacheHits() {
		return (this.memoryCacheHits);
	}	
}