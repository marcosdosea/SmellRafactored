package org.smellrefactored;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.designroleminer.ClassMetricResult;
import org.designroleminer.smelldetector.CarregaSalvaArquivo;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.DadosMetodoSmell;
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

	private static final String COMMA_DELIMITER = ",";
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	public static void main(String[] args) {

		Map<String, List<RefactoringData>> refactoringsByMethod = new HashMap<String, List<RefactoringData>>();
		TechniqueExecutor executor = new TechniqueExecutor(new DesignRoleTechnique());

		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		String pastaProjeto = "D:\\Projetos\\_Android\\sms-backup-plus";
		String commitInicial = "9a3a27418362c157aa79f160302c41a2c0cc67c5";
		String commitFinal = "12390bc25c2e4e6355ccd04e6b13dfdb689bdf2b";

		try {

			Repository repo = gitService.cloneIfNotExists(pastaProjeto,
					"https://github.com/jberkel/sms-backup-plus.git");

			final PersistenceMechanism pm = new CSVFile(System.getProperty("user.dir") + "\\refactoring.csv");
			pm.write("Commmit-id", "Refactring-name", "Refactoring-Type", "Code Element Left", "Code Element Right",
					"Class Before", "Class After"/* , "Short Message", "Full Message" */);

			miner.detectBetweenCommits(repo, commitInicial, commitFinal, new RefactoringHandler() {
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

			logger.info("Carregando valores limiares...");
			List<LimiarTecnica> listaTecnicas = CarregaSalvaArquivo
					.carregarLimiares(System.getProperty("user.dir") + "\\thresholds\\");

			// projeto inicial version 1.5.7
			logger.info("Iniciando a coleta de métricas do versão inicial do projeto...");
			gitService.checkout(repo, commitInicial);
			ArrayList<String> projetosAnalisar = new ArrayList<String>();
			projetosAnalisar.add(pastaProjeto);
			ArrayList<ClassMetricResult> classesProjetosInicial = executor.getMetricsFromProjects(projetosAnalisar,
					System.getProperty("user.dir") + "\\metrics-initial\\");
			logger.info("Gerando smells com a lista de problemas de design encontrados...");
			HashMap<String, DadosMetodoSmell> metodosSmellInicial = null;
			HashSet<MethodData> metodosSmellyInitial = null;
			metodosSmellInicial = FilterSmells.filtrar(classesProjetosInicial, listaTecnicas, metodosSmellInicial,
					metodosSmellyInitial);
			// FilterSmells.gravarMetodosSmell(metodosSmellInicial,
			// "\\results\\simple\\SMELLS_INICIAL.csv");

			// projeto final version 1.5.10
			logger.info("Iniciando a coleta de métricas do versão inicial do projeto...");
			gitService.checkout(repo, commitFinal);
			ArrayList<String> projetoFinal = new ArrayList<String>();
			projetoFinal.add(pastaProjeto);
			ArrayList<ClassMetricResult> metricasProjetosFinal = executor.getMetricsFromProjects(projetoFinal,
					System.getProperty("user.dir") + "\\metrics-final\\");

			logger.info("Gerando smells com a lista de problemas de design encontrados...");
			HashMap<String, DadosMetodoSmell> metodosSmellFinal = null;
			HashSet<MethodData> metodosSmellyFinal = null;
			metodosSmellFinal = FilterSmells.filtrar(metricasProjetosFinal, listaTecnicas, metodosSmellFinal,
					metodosSmellyFinal);

			if (metodosSmellInicial.size() != metodosSmellFinal.size())
				logger.debug(
						"diferenca inicial e final" + metodosSmellInicial.size() + "--- " + metodosSmellFinal.size());

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
			for (Map.Entry<String, DadosMetodoSmell> entry : metodosSmellInicial.entrySet()) {
				DadosMetodoSmell smell = entry.getValue();
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassInvolved = refactoring.getInvolvedClassesBefore().contains(smell.getNomeClasse())
							|| refactoring.getInvolvedClassesAfter().contains(smell.getNomeClasse());

					boolean isMethodRefactored = refactoring.getLeftSide().contains(smell.getNomeMetodo())
							|| refactoring.getRightSide().contains(smell.getNomeMetodo());
					if (isClassInvolved && isMethodRefactored) {
						String key = smell.getNomeClasse() + smell.getNomeMetodo() + smell.getSmell();
						List<RefactoringData> listByMethod = refactoringsByMethod.get(key);
						if (listByMethod == null)
							listByMethod = new ArrayList<RefactoringData>();
						listByMethod.add(refactoring);
						refactoringsByMethod.put(key, listByMethod);
					}
				}
			}

			logger.info("Gerando lista de not métodos smells...");
			HashSet<MethodData> metodosNotSmelly = new HashSet<MethodData>();
			for (ClassMetricResult classe : classesProjetosInicial) {
				for (MethodData metodo : classe.getMetricsByMethod().keySet()) {
					if (metodosSmellyInitial != null)
						if (!metodosSmellyInitial.contains(metodo))
							metodosNotSmelly.add(metodo);
				}
			}

			logger.info("Gerando lista de métodos not smells com refatorações...");
			HashMap<String, List<RefactoringData>> refactoringNotSmellyMethods = new HashMap<String, List<RefactoringData>>();
			int countRefactoringNotSmellyMethods = 0;
			for (MethodData method : metodosNotSmelly) {
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassInvolved = refactoring.getInvolvedClassesBefore().contains(method.getNomeClasse())
							|| refactoring.getInvolvedClassesAfter().contains(method.getNomeClasse());

					boolean isMethodRefactored = refactoring.getLeftSide().contains(method.getNomeMethod())
							|| refactoring.getRightSide().contains(method.getNomeMethod());
					if (isClassInvolved && isMethodRefactored) {
						String key = method.getNomeClasse() + method.getNomeMethod();
						List<RefactoringData> listByMethod = refactoringsByMethod.get(key);
						if (listByMethod == null)
							listByMethod = new ArrayList<RefactoringData>();
						listByMethod.add(refactoring);
						refactoringNotSmellyMethods.put(key, listByMethod);
						countRefactoringNotSmellyMethods++;
					}
				}
			}

			int countRefatoctrings = listRefactoring.size();
			int countRefactoringSmellyMethods = countRefatoctrings - countRefactoringNotSmellyMethods;
			logger.info("Número total de refatorações:" + countRefatoctrings);
			logger.info("Número total de refatorações em Métodos Não Smell:" + countRefactoringNotSmellyMethods);
			logger.info("Número total de refatorações em Métodos Smell:" + countRefactoringSmellyMethods);

			final PersistenceMechanism pmRef = new CSVFile(System.getProperty("user.dir") + "\\refactoring-sms.csv");
			pmRef.write("Class", "Method", "Commit", "Smell", "Tecnicas", "Refactoring", "Full Message");

			for (Map.Entry<String, DadosMetodoSmell> entry : metodosSmellInicial.entrySet()) {
				DadosMetodoSmell smell = entry.getValue();
				String key = smell.getNomeClasse() + smell.getNomeMetodo() + smell.getSmell();
				List<RefactoringData> lista = refactoringsByMethod.get(key);
				if (lista != null) {
					for (RefactoringData ref : lista) {
						pmRef.write(smell.getNomeClasse(), smell.getNomeMetodo(), ref.getCommit(), smell.getSmell(),
								smell.getListaTecnicas(), ref.getRefactoringType(), ref.getFullMessage());
					}
				}
			}

			for (MethodData metodo : metodosNotSmelly) {
				String key = metodo.getNomeClasse() + metodo.getNomeMethod();
				List<RefactoringData> lista = refactoringsByMethod.get(key);
				if (lista != null) {
					for (RefactoringData ref : lista) {
						pmRef.write(metodo.getNomeClasse(), metodo.getNomeMethod(), "", "", "", "", "");
					}
				}
			}
		} catch (

		Exception e) {
			logger.error(e.getMessage());
		}

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
