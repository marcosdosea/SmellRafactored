package org.smellrefactored;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.designroleminer.ClassMetricResult;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.DadosMetodoSmell;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.threshold.DesignRoleTechnique;
import org.designroleminer.threshold.TechniqueExecutor;
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

import com.github.mauricioaniche.ck.MethodData;
import com.opencsv.CSVReader;

public class SmellRefactoredManager {

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	public static SmellRefactoredResult getSmellRefactoredBetweenCommit(String urlRepository, String localFolder,
			String initialCommit, String finalCommit, List<LimiarTecnica> listaTecnicas) {

		SmellRefactoredResult result = new SmellRefactoredResult();
		TechniqueExecutor executor = new TechniqueExecutor(new DesignRoleTechnique());
		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		try {

			Repository repo = gitService.cloneIfNotExists(localFolder, urlRepository);

			final PersistenceMechanism pm = new CSVFile(System.getProperty("user.dir") + "\\refactoring.csv");
			pm.write("Commmit-id", "Refactring-name", "Refactoring-Type", "Code Element Left", "Code Element Right",
					"Class Before", "Class After"/* , "Short Message", "Full Message" */);

			miner.detectBetweenCommits(repo, initialCommit, finalCommit, new RefactoringHandler() {
				@Override
				public void handle(String idCommit, List<Refactoring> refactorings) {

					for (Refactoring ref : refactorings) {

						pm.write(idCommit, ref.getName(), ref.getRefactoringType(),
								ref.leftSide().size() > 0 ? ref.leftSide().get(0).getCodeElement() : 0,
								ref.rightSide().size() > 0 ? ref.rightSide().get(0).getCodeElement() : 0,
								ref.getInvolvedClassesBeforeRefactoring(),
								ref.getInvolvedClassesAfterRefactoring()/*
																		 * , commitData.getShortMessage(),
																		 * commitData.getFullMessage()
																		 */);
					}
				}
			});

			logger.info("Iniciando a coleta de métricas do versão inicial do projeto...");
			gitService.checkout(repo, initialCommit);
			ArrayList<String> projetosAnalisar = new ArrayList<String>();
			projetosAnalisar.add(localFolder);
			ArrayList<ClassMetricResult> classesProjetosInicial = executor.getMetricsFromProjects(projetosAnalisar,
					System.getProperty("user.dir") + "\\metrics-initial\\");
			logger.info("Gerando smells com a lista de problemas de design encontrados...");
			FilterSmellResult smellsInitial = FilterSmells.filtrar(classesProjetosInicial, listaTecnicas);

			// projeto final version 1.5.10
			logger.info("Iniciando a coleta de métricas do versão inicial do projeto...");
			gitService.checkout(repo, finalCommit);
			ArrayList<String> projetoFinal = new ArrayList<String>();
			projetoFinal.add(localFolder);
			ArrayList<ClassMetricResult> metricasProjetosFinal = executor.getMetricsFromProjects(projetoFinal,
					System.getProperty("user.dir") + "\\metrics-final\\");

			logger.info("Gerando smells com a lista de problemas de design encontrados...");
			FilterSmellResult smellsFinal = FilterSmells.filtrar(metricasProjetosFinal, listaTecnicas);

			logger.debug("Número de Métodos Smell no inicio: " + smellsInitial.getListaMethodsSmelly().size());
			logger.debug("Número de Métodos Smell no Final: " + smellsFinal.getListaMethodsSmelly().size());

			ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
			try {
				CSVReader reader = new CSVReader(
						new FileReader(System.getProperty("user.dir") + "\\" + "refactoring.csv"));
				String[] nextLine;
				while ((nextLine = reader.readNext()) != null) {
					listRefactoring.add(getRecordFromLine(nextLine));
				}
				reader.close();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			logger.info("Gerando lista de métodos smells com refatorações...");
			Map<String, List<RefactoringData>> listRefactoringsByMethod = new HashMap<String, List<RefactoringData>>();
			for (Map.Entry<String, DadosMetodoSmell> entry : smellsInitial.getMetodosSmell().entrySet()) {
				DadosMetodoSmell smell = entry.getValue();
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassInvolved = refactoring.getInvolvedClassesBefore().contains(smell.getNomeClasse())
							|| refactoring.getInvolvedClassesAfter().contains(smell.getNomeClasse());

					boolean isMethodRefactored = refactoring.getLeftSide().contains(smell.getNomeMetodo())
							|| refactoring.getRightSide().contains(smell.getNomeMetodo());
					if (isClassInvolved && isMethodRefactored) {
						String key = smell.getNomeClasse() + smell.getNomeMetodo() + smell.getSmell();
						List<RefactoringData> listByMethod = listRefactoringsByMethod.get(key);
						if (listByMethod == null)
							listByMethod = new ArrayList<RefactoringData>();
						listByMethod.add(refactoring);
						listRefactoringsByMethod.put(key, listByMethod);
					}
				}
			}

			logger.info("Gerando lista de métodos not smells com refatorações...");
			Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly = new HashMap<String, List<RefactoringData>>();
			for (MethodData method : smellsInitial.getListaMethodsNotSmelly()) {
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassInvolved = refactoring.getInvolvedClassesBefore().contains(method.getNomeClasse())
							|| refactoring.getInvolvedClassesAfter().contains(method.getNomeClasse());

					boolean isMethodRefactored = refactoring.getLeftSide().contains(method.getNomeMethod())
							|| refactoring.getRightSide().contains(method.getNomeMethod());
					if (isClassInvolved && isMethodRefactored) {
						String key = method.getNomeClasse() + method.getNomeMethod();
						List<RefactoringData> listByMethod = listRefactoringsByMethodSmelly.get(key);
						if (listByMethod == null)
							listByMethod = new ArrayList<RefactoringData>();
						listByMethod.add(refactoring);
						listRefactoringsByMethodSmelly.put(key, listByMethod);
					}
				}
			}

			result.setListRefactoring(listRefactoring);
			result.setSmellsFinal(smellsFinal);
			result.setSmellsInitial(smellsInitial);
			result.setListRefactoringsByMethod(listRefactoringsByMethod);
			result.setListRefactoringsByMethodSmelly(listRefactoringsByMethodSmelly);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;

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
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;
	}
}
