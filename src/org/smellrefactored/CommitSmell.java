package org.smellrefactored;

import java.util.ArrayList;
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
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommitSmell {

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);
	
	private GitService gitService;
	private Repository repo;
	private ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster;
	private String localFolder;
	private List<LimiarTecnica> listaLimiarTecnica;
	String resultFileName;
	
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
	
	public FilterSmellResult obterSmellsPreviousCommit(String commitId) {
		CommitData previousCommit = null;
		boolean achouCommit = false;
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			if (commit.getId().equals(commitId)) {
				previousCommit = commit.getPrevious();
				achouCommit = true;
				break;
			}
		}
		FilterSmellResult smellsPreviousCommit = null;
		try {
			if (achouCommit) {
				smellsPreviousCommit = obterSmellsCommit(previousCommit.getId());
			} else {
				smellsPreviousCommit = obterSmellsCommit(commitId);
			}
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
		return smellsPreviousCommit;
	}
	
	public FilterSmellResult obterSmellsCommit(String commit) throws Exception {
		logger.info("Iniciando a coleta de métricas do commit " + commit + "...");
		ArrayList<String> projetosAnalisar = new ArrayList<String>();
		projetosAnalisar.add(localFolder);
		TechniqueExecutor executor = new TechniqueExecutor(repo, gitService);
		MetricReport report = executor.getMetricsFromProjects(projetosAnalisar,
				System.getProperty("user.dir") + "\\metrics\\", commit);

		if (listCommitEvaluated.size() == 0) {
			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");
		}
		if (!listCommitEvaluated.contains(commit)) {
			listCommitEvaluated.add(commit);
			pmResultSmellRefactoredCommit.write(commit, report.getNumberOfClasses(), report.getNumberOfMethods(),
					report.getSystemLOC());
		}
		logger.info("Gerando smells com a lista de problemas de design encontrados...");
		FilterSmellResult smellsCommitInitial = FilterSmells.filtrar(report.all(), listaLimiarTecnica, commit);
		FilterSmells.gravarMetodosSmell(smellsCommitInitial.getMetodosSmell(), resultFileName + "-smells-commit-initial-method.csv");
		FilterSmells.gravarClassesSmell(smellsCommitInitial.getClassesSmell(), resultFileName + "-smells-commit-initial-class.csv");
		return smellsCommitInitial;
	}
	
	public LinkedHashMap<String, LimiarTecnica> getTechniquesThresholds() {
 		return (this.techniquesThresholds);
	}
	
	
}
