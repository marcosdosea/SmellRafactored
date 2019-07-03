package org.smellrefactored;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.designroleminer.ClassMetricResult;
import org.designroleminer.smelldetector.CarregaSalvaArquivo;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.DadosMetodoSmell;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.technique.DesignRoleTechnique;
import org.designroleminer.technique.TechniqueExecutor;
import org.eclipse.jgit.lib.Repository;
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

public class SmellRefactoredManager {

	private static final String COMMA_DELIMITER = ",";
	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	public static void main(String[] args) {
		
		Map<String, List<RefactoringData>> refactoringsByMethod = new HashMap<String, List<RefactoringData>>();

		String pastaProjeto = "D:\\Projetos\\_Android\\sms-backup-plus";
		String commitInicial = "9a3a27418362c157aa79f160302c41a2c0cc67c5";
		String commitFinal = "12390bc25c2e4e6355ccd04e6b13dfdb689bdf2b";

		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		TechniqueExecutor executor = new TechniqueExecutor(new DesignRoleTechnique());

		try {

			Repository repo = gitService.cloneIfNotExists(pastaProjeto,
					"https://github.com/jberkel/sms-backup-plus.git");

			final PersistenceMechanism pm = new CSVFile(System.getProperty("user.dir") + "\\" + "refactorings.csv");
			
			
			miner.detectBetweenCommits(repo, commitInicial, commitFinal, new RefactoringHandler() {
				@Override
				public void handle(RevCommit commitData, List<Refactoring> refactorings) {

					// RefactoringData data;
					// System.out.println("Refactorings at " + commitData.getId().getName());
					for (Refactoring ref : refactorings) {
						pm.write(commitData.getId().getName(), ref.getName(), ref.getRefactoringType(),
								// ref.leftSide().size() > 0 ? ref.leftSide().get(0).getCodeElement() : 0 ,
								// ref.rightSide().size() > 0 ? ref.rightSide().get(0).getCodeElement() : 0 ,
								ref.getInvolvedClassesAfterRefactoring(), ref.getInvolvedClassesBeforeRefactoring(),
								commitData.getShortMessage(), commitData.getFullMessage());
						// data = new RefactoringData();
						// data.setCommit(commitData.getId().getName());
						// data.setRefactoringName(ref.getName());
						// data.setRefactoringType(ref.getRefactoringType().toString());
						// data.setLeftSide(ref.leftSide().size() > 0 ?
						// ref.leftSide().get(0).getCodeElement() : "");
						// data.setRightSide(ref.rightSide().size() > 0 ?
						// ref.rightSide().get(0).getCodeElement() : "");
						// data.setInvolvedClassesBefore(ref.getInvolvedClassesBeforeRefactoring().toString());
						// data.setInvolvedClassesAfter(ref.getInvolvedClassesAfterRefactoring().toString());
						// data.setShortMessage(commitData.getShortMessage());
						// data.setFullMessage(commitData.getFullMessage());
						// pm.write(data.getCommit(), data.getRefactoringName(),
						// data.getRefactoringType(),
						// data.getLeftSide(), data.getRightSide(), data.getInvolvedClassesBefore(),
						// data.getInvolvedClassesAfter(), data.getShortMessage(),
						// data.getFullMessage());
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
			ArrayList<ClassMetricResult> classesProjetosInicial = executor.getMetricsFromProjects(projetosAnalisar);
			logger.info("Gerando smells com a lista de problemas de design encontrados...");
			HashMap<String, DadosMetodoSmell> metodosSmellInicial = null;
			metodosSmellInicial = FilterSmells.filtrar(classesProjetosInicial, listaTecnicas, metodosSmellInicial);
			// FilterSmells.gravarMetodosSmell(metodosSmellInicial,
			// "\\results\\simple\\SMELLS_INICIAL.csv");

			// projeto final version 1.5.10
			logger.info("Iniciando a coleta de métricas do versão inicial do projeto...");
			gitService.checkout(repo, commitFinal);
			ArrayList<String> projetoFinal = new ArrayList<String>();
			projetoFinal.add(pastaProjeto);
			ArrayList<ClassMetricResult> metricasProjetosFinal = executor.getMetricsFromProjects(projetoFinal);
			logger.info("Gerando smells com a lista de problemas de design encontrados...");
			HashMap<String, DadosMetodoSmell> metodosSmellFinal = null;
			metodosSmellFinal = FilterSmells.filtrar(metricasProjetosFinal, listaTecnicas, metodosSmellFinal);
			// FilterSmells.gravarMetodosSmell(metodosSmellInicial,
			// "\\results\\simple\\SMELLS_FINAL.csv");
			
			if (metodosSmellInicial.size() != metodosSmellFinal.size())
				logger.debug(
						"diferenca inicial e final" + metodosSmellInicial.size() + "--- " + metodosSmellFinal.size());

			ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
			try {
				Scanner scanner = new Scanner(new File(System.getProperty("user.dir") + "\\" + "refactorings.csv"));
				while (scanner.hasNextLine()) {
					listRefactoring.add(getRecordFromLine(scanner.nextLine()));
				}
				scanner.close();
			} catch (Exception e) {
				logger.error(e.getMessage());
			}

			for (Map.Entry<String, DadosMetodoSmell> entry : metodosSmellInicial.entrySet()) {
				DadosMetodoSmell smell = entry.getValue();
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassRefacactored = refactoring.getInvolvedClassesBefore().contains(smell.getNomeClasse())
							|| refactoring.getInvolvedClassesAfter().contains(smell.getNomeClasse());

					boolean isMethodRefactored = refactoring.getLeftSide().contains(smell.getNomeMetodo())
							|| refactoring.getRightSide().contains(smell.getNomeMetodo());
					if (isClassRefacactored && isMethodRefactored) {
						String key = smell.getNomeClasse() + smell.getNomeMetodo();
						List<RefactoringData> listByMethod = refactoringsByMethod.get(key);
						if (listByMethod == null)
							listByMethod = new ArrayList<RefactoringData>();
						listByMethod.add(refactoring);
						refactoringsByMethod.put(key, listByMethod);
					}
				}
			}

			final PersistenceMechanism pmResult = new CSVFile(System.getProperty("user.dir") + "\\refactoring-sms.csv");
			pm.write("Class", "Method", "Commit", "Refactoring", "Full Message");

			for (Map.Entry<String, DadosMetodoSmell> entry : metodosSmellInicial.entrySet()) {
				DadosMetodoSmell smell = entry.getValue();
				String key = smell.getNomeClasse() + smell.getNomeMetodo();
				List<RefactoringData> lista = refactoringsByMethod.get(key);
				if (lista == null) {
					pmResult.write(smell.getNomeClasse(), smell.getNomeMetodo(), "", "", "");
				} else {
					for (RefactoringData ref : lista) {
						pmResult.write(smell.getNomeClasse(), smell.getNomeMetodo(), ref.getCommit(),
								ref.getRefactoringType(), ref.getFullMessage());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		//return refactoringsByMethod;

	}

	private static RefactoringData getRecordFromLine(String line) {
		RefactoringData result = new RefactoringData();
		try {
			Scanner rowScanner = new Scanner(line);

			rowScanner.useDelimiter(COMMA_DELIMITER);
			while (rowScanner.hasNext()) {
				result.setCommit(rowScanner.next());
				result.setRefactoringName(rowScanner.next());
				result.setRefactoringName(rowScanner.next());
				result.setRefactoringType(rowScanner.next());
				result.setLeftSide(rowScanner.next());
				result.setRightSide(rowScanner.next());
				result.setInvolvedClassesBefore(rowScanner.next());
				result.setInvolvedClassesAfter(rowScanner.next());
				result.setShortMessage(rowScanner.next());
				result.setFullMessage(rowScanner.next());
			}
			rowScanner.close();
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;
	}
}
