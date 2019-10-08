package org.smellrefactored;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.designroleminer.ClassMetricResult;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.DadosMetodo;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.threshold.DesignRoleTechnique;
import org.designroleminer.threshold.TechniqueExecutor;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
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

			ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
			HashSet<String> commitsWithRefactorings = new HashSet<String>();
			try {
				CSVReader reader = new CSVReader(
						new FileReader(System.getProperty("user.dir") + "\\" + "refactoring.csv"));
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

			HashSet<String> commitsWithRefactoringsMergedIntoMaster = new HashSet<String>();
			for (String commitRefactored : commitsWithRefactorings) {
				RevWalk revWalk = new RevWalk(repo);
				RevCommit masterHead = revWalk.parseCommit(repo.resolve("refs/heads/master"));
				// first a commit that was merged
				ObjectId id = repo.resolve(commitRefactored);
				RevCommit otherHead = revWalk.parseCommit(id);

				if (revWalk.isMergedInto(otherHead, masterHead)) {
					System.out.println("Commit " + otherHead + " is merged into master");
					commitsWithRefactoringsMergedIntoMaster.add(commitRefactored);
				} else {

					System.out.println("Commit " + otherHead + " is NOT merged into master");
				}
				revWalk.dispose();
			}

			Iterator<String> itCommitsWithRefactorings = commitsWithRefactoringsMergedIntoMaster.iterator();

			FilterSmellResult smellsCommit;
			Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly = new HashMap<String, List<RefactoringData>>();
			Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly = new HashMap<String, List<RefactoringData>>();

			// String treeName = "refs/heads/master"; // tag or branch
			Git git = new Git(repo);

			while (itCommitsWithRefactorings.hasNext()) {
				String commitAnalisar = itCommitsWithRefactorings.next();
				gitService.checkout(repo, finalCommit);
				// busca previous commit para coletar as métricas
				String previousCommitId = "";
				String messageCurrentCommit = "";
				String treeName = "master";

				Iterable<RevCommit> log = git.log().call();
				Iterator<RevCommit> logIterator = log.iterator();
				while (logIterator.hasNext()) {
					RevCommit currentCommit = logIterator.next();

					messageCurrentCommit = currentCommit.getFullMessage();
					if (currentCommit.getId().getName().equals(commitAnalisar)) {
						if (logIterator.hasNext())
							previousCommitId = (logIterator.next()).getId().getName();

						System.out.println("Full message" + currentCommit.getFullMessage());
						System.out.println("Short message" + currentCommit.getShortMessage());
						System.out.println("Type" + currentCommit.getType());
						System.out.println("Encoding name" + currentCommit.getEncodingName());
						System.out.println("Name" + currentCommit.getAuthorIdent());

						break;

					}
					// System.out.println("Date: " + (new Date(commit.getCommitTime() * 1000L)));
				}
				// garantir quando nao houver próximos commits
				if (previousCommitId.equals(""))
					previousCommitId = commitAnalisar;
				ArrayList<RefactoringData> listRefactoringCommitAnalisado = new ArrayList<RefactoringData>();
				for (RefactoringData refactoring : listRefactoring) {
					if (refactoring.getCommit().equals(commitAnalisar)) {
						refactoring.setFullMessage(messageCurrentCommit);
						listRefactoringCommitAnalisado.add(refactoring);
					}
				}

				gitService.checkout(repo, previousCommitId);
				logger.info("Iniciando a coleta de métricas do commit " + previousCommitId + "...");
				ArrayList<String> projetosAnalisar = new ArrayList<String>();
				projetosAnalisar.add(localFolder);
				ArrayList<ClassMetricResult> classesProjetosInicial = executor.getMetricsFromProjects(projetosAnalisar,
						System.getProperty("user.dir") + "\\metrics-initial\\", false);
				logger.info("Gerando smells com a lista de problemas de design encontrados...");
				smellsCommit = FilterSmells.filtrar(classesProjetosInicial, listaTecnicas, commitAnalisar);

				logger.info("Gerando lista de métodos smells com refatorações...");
				for (DadosMetodo methodSmelly : smellsCommit.getMetodosSmell()) {
					for (RefactoringData refactoring : listRefactoringCommitAnalisado) {
						boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
								.contains(methodSmelly.getNomeClasse())
								|| refactoring.getInvolvedClassesAfter().contains(methodSmelly.getNomeClasse());

						boolean isMethodRefactored = refactoring.getLeftSide().contains(methodSmelly.getNomeMetodo())
								|| refactoring.getRightSide().contains(methodSmelly.getNomeMetodo());
						if (isClassInvolved && isMethodRefactored) {
							String key = methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo()
									+ methodSmelly.getCommit() + methodSmelly.getSmell();
							List<RefactoringData> listByMethod = listRefactoringsByMethodSmelly.get(key);
							if (listByMethod == null)
								listByMethod = new ArrayList<RefactoringData>();
							RefactoringData refactoringResult = atribuir(methodSmelly, refactoring);
							listByMethod.add(refactoringResult);
							listRefactoringsByMethodSmelly.put(key, listByMethod);
						}
					}
				}

				logger.info("Gerando lista de métodos not smells com refatorações...");
				for (DadosMetodo methodNotSmelly : smellsCommit.getMetodosNotSmelly()) {
					for (RefactoringData refactoring : listRefactoringCommitAnalisado) {
						boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
								.contains(methodNotSmelly.getNomeClasse())
								|| refactoring.getInvolvedClassesAfter().contains(methodNotSmelly.getNomeClasse());

						boolean isMethodRefactored = refactoring.getLeftSide().contains(methodNotSmelly.getNomeMetodo())
								|| refactoring.getRightSide().contains(methodNotSmelly.getNomeMetodo());
						if (isClassInvolved && isMethodRefactored) {
							String key = methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo()
									+ methodNotSmelly.getCommit();
							List<RefactoringData> listByMethod = listRefactoringsByMethodNotSmelly.get(key);
							if (listByMethod == null)
								listByMethod = new ArrayList<RefactoringData>();

							RefactoringData refactoringResult = atribuir(methodNotSmelly, refactoring);
							listByMethod.add(refactoringResult);
							listRefactoringsByMethodNotSmelly.put(key, listByMethod);
						}
					}
				}
			}

			git.close();
			result.setListRefactoring(listRefactoring);
			result.setListRefactoringsByMethodSmelly(listRefactoringsByMethodSmelly);
			result.setListRefactoringsByMethodNotSmelly(listRefactoringsByMethodNotSmelly);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;

	}

	private static RefactoringData atribuir(DadosMetodo method, RefactoringData refactoring) {
		RefactoringData refactoringResult = new RefactoringData();
		refactoringResult.setCommit(method.getCommit());
		refactoringResult.setFullMessage(refactoring.getFullMessage());
		refactoringResult.setInvolvedClassesAfter(refactoring.getInvolvedClassesAfter());
		refactoringResult.setInvolvedClassesBefore(refactoring.getInvolvedClassesBefore());
		refactoringResult.setLeftSide(refactoring.getLeftSide());
		refactoringResult.setRightSide(refactoring.getRightSide());
		refactoringResult.setRefactoringName(refactoring.getRefactoringName());
		refactoringResult.setRefactoringType(refactoring.getRefactoringType());
		refactoringResult.setShortMessage(refactoring.getShortMessage());
		refactoringResult.setNomeClasse(method.getNomeClasse());
		refactoringResult.setNomeMetodo(method.getNomeMetodo());
		refactoringResult.setSmell(method.getSmell());
		refactoringResult.setClassDesignRole(method.getClassDesignRole());
		refactoringResult.setComplexity(method.getComplexity());
		refactoringResult.setEfferent(method.getEfferent());
		refactoringResult.setNumberOfParameters(method.getNumberOfParameters());
		refactoringResult.setLinesOfCode(method.getLinesOfCode());
		refactoringResult.setListaTecnicas(method.getListaTecnicas());
		return refactoringResult;
	}

	public static void storeResult(SmellRefactoredResult result, String fileName) {
		logger.info("Número de Métodos Não Smell Refatorados: " + result.getListRefactoringsByMethodNotSmelly().size());
		logger.info("Número de Métodos Smell Refatorados: " + result.getListRefactoringsByMethodSmelly().size());

		logger.info("Total de refatorações Métodos e Não Métodos:" + result.getListRefactoring().size());
		final PersistenceMechanism pmRef = new CSVFile(fileName);
		pmRef.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas", "Commit", "Refactoring",
				"Left Side", "Right Side");

		int countRefactoringsSmellyMethod = 0;
		for (String keyMetodo : result.getListRefactoringsByMethodSmelly().keySet()) {
			List<RefactoringData> lista = result.getListRefactoringsByMethodSmelly().get(keyMetodo);
			countRefactoringsSmellyMethod += lista.size();
			if (lista != null) {
				for (RefactoringData ref : lista) {
					pmRef.write(ref.getNomeClasse(), ref.getNomeMetodo(), ref.getSmell(), ref.getLinesOfCode(),
							ref.getComplexity(), ref.getEfferent(), ref.getNumberOfParameters(), ref.getListaTecnicas(),
							ref.getCommit(), ref.getRefactoringType(), ref.getLeftSide(), ref.getRightSide());
				}
			}
		}
		logger.info("Total de refatorações Métodos Smelly: " + countRefactoringsSmellyMethod);

		int countRefactoringsNotSmellyMethod = 0;
		for (String keyMetodo : result.getListRefactoringsByMethodNotSmelly().keySet()) {
			List<RefactoringData> lista = result.getListRefactoringsByMethodNotSmelly().get(keyMetodo);
			countRefactoringsNotSmellyMethod += lista.size();
			if (lista != null) {
				for (RefactoringData ref : lista) {
					pmRef.write(ref.getNomeClasse(), ref.getNomeMetodo(), ref.getSmell(), ref.getLinesOfCode(),
							ref.getComplexity(), ref.getEfferent(), ref.getNumberOfParameters(), ref.getListaTecnicas(),
							ref.getCommit(), ref.getRefactoringType(), ref.getLeftSide(), ref.getRightSide());
				}
			}
		}
		logger.info("Total de refatorações Métodos Not Smelly: " + countRefactoringsNotSmellyMethod);

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
