package org.smellrefactored;

import java.io.FileReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.designroleminer.MetricReport;
import org.designroleminer.smelldetector.FilterSmells;
import org.designroleminer.smelldetector.model.FilterSmellResult;
import org.designroleminer.smelldetector.model.LimiarTecnica;
import org.designroleminer.smelldetector.model.MethodDataSmelly;
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

			// detect list of refactoring between commits
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

			// Cria lista de commits que possuem refatorações
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

			/// Filtra os commits com refactoring cujo commit feito no master ou pull
			/// request realizados no master
			HashSet<String> commitsWithRefactoringsMergedIntoMaster = new HashSet<String>();
			for (String commitRefactored : commitsWithRefactorings) {
				RevWalk revWalk = new RevWalk(repo);
				RevCommit masterHead = revWalk.parseCommit(repo.resolve("refs/heads/master"));
				// first a commit that was merged
				ObjectId id = repo.resolve(commitRefactored);
				RevCommit otherHead = revWalk.parseCommit(id);

				if (revWalk.isMergedInto(otherHead, masterHead)) {
					if ((otherHead.getParentCount() == 1) || (otherHead.getShortMessage().contains("merge")
							&& otherHead.getShortMessage().contains("pull"))) {
						commitsWithRefactoringsMergedIntoMaster.add(commitRefactored);
					}
				}
				revWalk.close();
				revWalk.dispose();
			}
			gitService.checkout(repo, initialCommit);
			//logger.info("Iniciando a coleta de métricas do commit inicial " + initialCommit + "...");
			ArrayList<String> projetosAnalisar = new ArrayList<String>();
			projetosAnalisar.add(localFolder);
			MetricReport report = executor.getMetricsFromProjects(projetosAnalisar,
					System.getProperty("user.dir") + "\\metrics-initial\\", false);

			//logger.info("Gerando smells com a lista de problemas de design encontrados...");
			FilterSmellResult smellsCommitInitial = FilterSmells.filtrar(report.all(), listaTecnicas, initialCommit);

			HashSet<MethodDataSmelly> metodosSmellyInitialNotRefactored = smellsCommitInitial.getMetodosSmell();
			Iterator<String> itCommits = commitsWithRefactoringsMergedIntoMaster.iterator();
			FilterSmellResult smellsCommit;

			Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly = new HashMap<String, List<RefactoringData>>();
			Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly = new HashMap<String, List<RefactoringData>>();

			// Busca o commit anterior ao commit onde houve refactoring
			Git git = new Git(repo);
			while (itCommits.hasNext()) {
				String commitAnalisar = itCommits.next();

				// vai para o último commit para pegar o log
				gitService.checkout(repo, finalCommit);
				// busca previous commit para coletar as métricas
				String previousCommitId = "";
				String messageCurrentCommit = "";
				Date dateCommit = new Date();
				Iterable<RevCommit> log = git.log().call();
				Iterator<RevCommit> logIterator = log.iterator();
				while (logIterator.hasNext()) {
					RevCommit currentCommit = logIterator.next();

					messageCurrentCommit = currentCommit.getFullMessage();
					dateCommit = new Date(currentCommit.getCommitTime() * 1000L);
					if (currentCommit.getId().getName().equals(commitAnalisar)) {
						if (logIterator.hasNext())
							previousCommitId = (logIterator.next()).getId().getName();
						break;

					}

				}
				// garantir quando nao houver próximos commits
				if (previousCommitId.equals(""))
					previousCommitId = commitAnalisar;
				ArrayList<RefactoringData> listRefactoringCommitAnalisado = new ArrayList<RefactoringData>();
				for (RefactoringData refactoring : listRefactoring) {
					if (refactoring.getCommit().equals(commitAnalisar)) {
						refactoring.setFullMessage(messageCurrentCommit);
						refactoring.setCommitDate(dateCommit);
						listRefactoringCommitAnalisado.add(refactoring);
					}
				}

				gitService.checkout(repo, previousCommitId);
				logger.info("Iniciando a coleta de métricas do commit " + previousCommitId + "...");
				// ArrayList<String> projetosAnalisar = new ArrayList<String>();
				// projetosAnalisar.add(localFolder);
				MetricReport reportInitial = executor.getMetricsFromProjects(projetosAnalisar,
						System.getProperty("user.dir") + "\\metrics-initial\\", false);
				//logger.info("Gerando smells com a lista de problemas de design encontrados...");
				smellsCommit = FilterSmells.filtrar(reportInitial.all(), listaTecnicas, commitAnalisar);

				//logger.info("Gerando lista de métodos smells com refatorações...");
				for (MethodDataSmelly methodSmelly : smellsCommit.getMetodosSmell()) {

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
							refactoringResult.setNumberOfClasses(reportInitial.getNumberOfClasses());
							refactoringResult.setSystemLOC(reportInitial.getSystemLOC());
							refactoringResult.setNumberOfMethods(reportInitial.getNumberOfMethods());
							listByMethod.add(refactoringResult);
							listRefactoringsByMethodSmelly.put(key, listByMethod);
							metodosSmellyInitialNotRefactored.remove(methodSmelly); // ou seja, é smelly e foi
																					// refatorado em algum momento da
																					// evolução
						}
					}
				}

				//logger.info("Gerando lista de métodos not smells com refatorações...");
				for (MethodDataSmelly methodNotSmelly : smellsCommit.getMetodosNotSmelly()) {
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
							refactoringResult.setNumberOfClasses(reportInitial.getNumberOfClasses());
							refactoringResult.setSystemLOC(reportInitial.getSystemLOC());
							refactoringResult.setNumberOfMethods(reportInitial.getNumberOfMethods());
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
			result.setMethodSmellyInitialNotRefactored(metodosSmellyInitialNotRefactored);
			return result;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return result;

	}

	private static RefactoringData atribuir(MethodDataSmelly method, RefactoringData refactoring) {
		RefactoringData refactoringResult = new RefactoringData();
		refactoringResult.setCommit(method.getCommit());
		refactoringResult.setFullMessage(refactoring.getFullMessage());
		refactoringResult.setCommitDate(refactoring.getCommitDate());
		refactoringResult.setNumberOfClasses(refactoring.getNumberOfClasses());
		refactoringResult.setNumberOfMethods(refactoring.getNumberOfMethods());
		refactoringResult.setSystemLOC(refactoring.getSystemLOC());
		
		
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

	public static void storeResult(SmellRefactoredResult result, String fileName, boolean imprimirMensagemCommit) {
		final PersistenceMechanism pmRef = new CSVFile(fileName);
		pmRef.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas", "Commit", "Refactoring",
				"Left Side", "Right Side", "Full Message");

		for (String keyMetodo : result.getListRefactoringsByMethodSmelly().keySet()) {
			List<RefactoringData> lista = result.getListRefactoringsByMethodSmelly().get(keyMetodo);
			if (lista != null) {
				for (RefactoringData ref : lista) {
					pmRef.write(ref.getNomeClasse(), ref.getNomeMetodo(), ref.getSmell(), ref.getLinesOfCode(),
							ref.getComplexity(), ref.getEfferent(), ref.getNumberOfParameters(), ref.getListaTecnicas(),
							ref.getCommit(), ref.getRefactoringType(), ref.getLeftSide(), ref.getRightSide(),
							imprimirMensagemCommit ? ref.getFullMessage() : "");
				}
			}
		}
		
		for (String keyMetodo : result.getListRefactoringsByMethodNotSmelly().keySet()) {
			List<RefactoringData> lista = result.getListRefactoringsByMethodNotSmelly().get(keyMetodo);
			if (lista != null) {
				for (RefactoringData ref : lista) {
					pmRef.write(ref.getNomeClasse(), ref.getNomeMetodo(), ref.getSmell(), ref.getLinesOfCode(),
							ref.getComplexity(), ref.getEfferent(), ref.getNumberOfParameters(), ref.getListaTecnicas(),
							ref.getCommit(), ref.getRefactoringType(), ref.getLeftSide(), ref.getRightSide(),
							imprimirMensagemCommit ? ref.getFullMessage() : "");
				}
			}
		}
		
		for (MethodDataSmelly methodSmelly : result.getMethodInitialSmellyNotRefactored()) {
			pmRef.write(methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
					methodSmelly.getLinesOfCode(), methodSmelly.getComplexity(), methodSmelly.getEfferent(),
					methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(), methodSmelly.getCommit(), "",
					"", "", "");
		}
	}

	public static void evaluateStoreResults(SmellRefactoredResult result, String fileName) {
		final PersistenceMechanism pmResult = new CSVFile(fileName, true);
		
		pmResult.write("RELATÓRIO COMPLETO SISTEMA");
		HashMap<String, RefactoringData> dadosCommits = new HashMap<String, RefactoringData>();
		int countRefactoringsSmellyMethod = 0;
		for (String keyMetodo : result.getListRefactoringsByMethodSmelly().keySet()) {
			List<RefactoringData> lista = result.getListRefactoringsByMethodSmelly().get(keyMetodo);
			countRefactoringsSmellyMethod += lista.size();
			for(RefactoringData ref: lista) {
				dadosCommits.put(ref.getCommit(), ref);
			}
		}
		int countRefactoringsNotSmellyMethod = 0;
		for (String keyMetodo : result.getListRefactoringsByMethodNotSmelly().keySet()) {
			List<RefactoringData> lista = result.getListRefactoringsByMethodNotSmelly().get(keyMetodo);
			countRefactoringsNotSmellyMethod += lista.size();
			for(RefactoringData ref: lista) {
				dadosCommits.put(ref.getCommit(), ref);
			}
		}
		pmResult.write("Commit", "Date Commit", "Number Classes", "Number Methods", "System LOC");
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

		for (String commit: dadosCommits.keySet()) {
			RefactoringData ref = dadosCommits.get(commit);
			
			pmResult.write(ref.getCommit(), formatter.format(ref.getCommitDate()), ref.getNumberOfClasses(), ref.getNumberOfMethods(), ref.getSystemLOC());
		}
		pmResult.write("Total de refatorações Métodos Smelly: ", countRefactoringsSmellyMethod);
		pmResult.write("Total de refatorações Métodos Not Smelly: ", countRefactoringsNotSmellyMethod);
		pmResult.write("Número de Métodos Não Smell Refatorados: ",  result.getListRefactoringsByMethodNotSmelly().size());
		pmResult.write("Número de Métodos Smell Refatorados: ",  result.getListRefactoringsByMethodSmelly().size());
		pmResult.write("Total de refatorações Métodos e Não Métodos:", result.getListRefactoring().size());
		pmResult.write("Métodos Smelly not refactored: ", result.getMethodInitialSmellyNotRefactored().size());

		saveLongMethodEvaluation(result, pmResult);
		saveComplexityMethodEvaluation(result, pmResult);
		saveEfferentCouplingMethodEvaluation(result, pmResult);
		saveManyParametersMethodEvaluation(result, pmResult);
	}

	private static void saveLongMethodEvaluation(SmellRefactoredResult result, final PersistenceMechanism pmResult) {
		float falseNegative = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodNotSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if ((method.getSmell() == null) && !method.getRefactoringType().contains("RENAME")) {
					falseNegative++;
				}
			}
		}
		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;
		for (MethodDataSmelly method : result.getMethodInitialSmellyNotRefactored()) {
			if (method.getSmell().equals("Metodo Longo")) {
				if (method.getListaTecnicas().contains("V"))
					falsePositiveV++;
				if (method.getListaTecnicas().contains("X"))
					falsePositiveX++;
				if (method.getListaTecnicas().contains("R"))
					falsePositiveR++;
				if (method.getListaTecnicas().contains("D"))
					falsePositiveD++;
			}
		}
		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if (method.getSmell().equals("Metodo Longo") && !method.getRefactoringType().contains("RENAME")) {
					if (method.getListaTecnicas().contains("V"))
						truePositiveV++;
					if (method.getListaTecnicas().contains("X"))
						truePositiveX++;
					if (method.getListaTecnicas().contains("R"))
						truePositiveR++;
					if (method.getListaTecnicas().contains("D"))
						truePositiveD++;
				}
			}
		}
		float precisionV = (truePositiveV + falsePositiveV) != 0 ? truePositiveV / (truePositiveV + falsePositiveV) : 0;
		float precisionX = (truePositiveX + falsePositiveX) != 0 ? truePositiveX / (truePositiveX + falsePositiveX) : 0;
		float precisionR = (truePositiveR + falsePositiveR) != 0 ? truePositiveR / (truePositiveR + falsePositiveR) : 0;
		float precisionD = (truePositiveD + falsePositiveD) != 0 ? truePositiveD / (truePositiveD + falsePositiveD) : 0;
		
		float recallV = (truePositiveV + falseNegative) != 0 ? truePositiveV / (truePositiveV + falseNegative) : 0;
		float recallX = (truePositiveX + falseNegative) != 0 ? truePositiveX / (truePositiveX + falseNegative) : 0;
		float recallR = (truePositiveR + falseNegative) != 0 ? truePositiveR / (truePositiveR + falseNegative) : 0;
		float recallD = (truePositiveD + falseNegative) != 0 ? truePositiveD / (truePositiveD + falseNegative) : 0;
		
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;
		
		pmResult.write("LONG METHODS");
		pmResult.write("False Positive (V) = ", falsePositiveV);
		pmResult.write("False Positive (X) = ", falsePositiveX);
		pmResult.write("False Positive (R) = ", falsePositiveR);
		pmResult.write("False Positive (D) = ", falsePositiveD);
		pmResult.write("True Positive  (V) = ", truePositiveV);
		pmResult.write("True Positive (X) = ", truePositiveX);
		pmResult.write("True Positive (R) = ", truePositiveR);
		pmResult.write("True Positive (D) = ", truePositiveD);
		pmResult.write("False Negative = ", falseNegative);
		pmResult.write("Precision (V) = ", precisionV);
		pmResult.write("Precision (X) = ", precisionX);
		pmResult.write("Precision (R) = ", precisionR);
		pmResult.write("Precision (D) = ", precisionD);
		pmResult.write("Recall (V) = ", recallV);
		pmResult.write("Recall (X) = ", recallX);
		pmResult.write("Recall (R) = ", recallR);
		pmResult.write("Recall (D) = ", recallD);
		pmResult.write("F-measure (V) = ", fMeasureV);
		pmResult.write("F-measure (X) = ", fMeasureX);
		pmResult.write("F-measure (R) = ", fMeasureR);
		pmResult.write("F-measure (D) = ", fMeasureD);
		pmResult.write("");
	}
	
	private static void saveComplexityMethodEvaluation(SmellRefactoredResult result, final PersistenceMechanism pmResult) {
		float falseNegative = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodNotSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if ((method.getSmell() == null) && !method.getRefactoringType().contains("RENAME")) {
					falseNegative++;
				}
			}
		}
		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;
		for (MethodDataSmelly method : result.getMethodInitialSmellyNotRefactored()) {
			if (method.getSmell().equals("Muitos Desvios")) {
				if (method.getListaTecnicas().contains("V"))
					falsePositiveV++;
				if (method.getListaTecnicas().contains("X"))
					falsePositiveX++;
				if (method.getListaTecnicas().contains("R"))
					falsePositiveR++;
				if (method.getListaTecnicas().contains("D"))
					falsePositiveD++;
			}
		}
		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if (method.getSmell().equals("Muitos Desvios") && !method.getRefactoringType().contains("RENAME")) {
					if (method.getListaTecnicas().contains("V"))
						truePositiveV++;
					if (method.getListaTecnicas().contains("X"))
						truePositiveX++;
					if (method.getListaTecnicas().contains("R"))
						truePositiveR++;
					if (method.getListaTecnicas().contains("D"))
						truePositiveD++;
				}
			}
		}
		float precisionV = (truePositiveV + falsePositiveV) != 0 ? truePositiveV / (truePositiveV + falsePositiveV) : 0;
		float precisionX = (truePositiveX + falsePositiveX) != 0 ? truePositiveX / (truePositiveX + falsePositiveX) : 0;
		float precisionR = (truePositiveR + falsePositiveR) != 0 ? truePositiveR / (truePositiveR + falsePositiveR) : 0;
		float precisionD = (truePositiveD + falsePositiveD) != 0 ? truePositiveD / (truePositiveD + falsePositiveD) : 0;
		
		
		float recallV = (truePositiveV + falseNegative) != 0 ? truePositiveV / (truePositiveV + falseNegative) : 0;
		float recallX = (truePositiveX + falseNegative) != 0 ? truePositiveX / (truePositiveX + falseNegative) : 0;
		float recallR = (truePositiveR + falseNegative) != 0 ? truePositiveR / (truePositiveR + falseNegative) : 0;
		float recallD = (truePositiveD + falseNegative) != 0 ? truePositiveD / (truePositiveD + falseNegative) : 0;
		
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;
		
		pmResult.write("COMPLEXITY METHOD");
		pmResult.write("False Positive (V) = ", falsePositiveV);
		pmResult.write("False Positive (X) = ", falsePositiveX);
		pmResult.write("False Positive (R) = ", falsePositiveR);
		pmResult.write("False Positive (D) = ", falsePositiveD);
		pmResult.write("True Positive  (V) = ", truePositiveV);
		pmResult.write("True Positive (X) = ", truePositiveX);
		pmResult.write("True Positive (R) = ", truePositiveR);
		pmResult.write("True Positive (D) = ", truePositiveD);
		pmResult.write("False Negative = ", falseNegative);
		pmResult.write("Precision (V) = ", precisionV);
		pmResult.write("Precision (X) = ", precisionX);
		pmResult.write("Precision (R) = ", precisionR);
		pmResult.write("Precision (D) = ", precisionD);
		pmResult.write("Recall (V) = ", recallV);
		pmResult.write("Recall (X) = ", recallX);
		pmResult.write("Recall (R) = ", recallR);
		pmResult.write("Recall (D) = ", recallD);
		pmResult.write("F-measure (V) = ", fMeasureV);
		pmResult.write("F-measure (X) = ", fMeasureX);
		pmResult.write("F-measure (R) = ", fMeasureR);
		pmResult.write("F-measure (D) = ", fMeasureD);
		pmResult.write("");
	}


	private static void saveEfferentCouplingMethodEvaluation(SmellRefactoredResult result, final PersistenceMechanism pmResult) {
		float falseNegative = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodNotSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if ((method.getSmell() == null) && !method.getRefactoringType().contains("RENAME")) {
					falseNegative++;
				}
			}
		}
		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;
		for (MethodDataSmelly method : result.getMethodInitialSmellyNotRefactored()) {
			if (method.getSmell().equals("Alto Acoplamento Efferent")) {
				if (method.getListaTecnicas().contains("V"))
					falsePositiveV++;
				if (method.getListaTecnicas().contains("X"))
					falsePositiveX++;
				if (method.getListaTecnicas().contains("R"))
					falsePositiveR++;
				if (method.getListaTecnicas().contains("D"))
					falsePositiveD++;
			}
		}
		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if (method.getSmell().equals("Alto Acoplamento Efferent") && !method.getRefactoringType().contains("RENAME")) {
					if (method.getListaTecnicas().contains("V"))
						truePositiveV++;
					if (method.getListaTecnicas().contains("X"))
						truePositiveX++;
					if (method.getListaTecnicas().contains("R"))
						truePositiveR++;
					if (method.getListaTecnicas().contains("D"))
						truePositiveD++;
				}
			}
		}
		float precisionV = (truePositiveV + falsePositiveV) != 0 ? truePositiveV / (truePositiveV + falsePositiveV) : 0;
		float precisionX = (truePositiveX + falsePositiveX) != 0 ? truePositiveX / (truePositiveX + falsePositiveX) : 0;
		float precisionR = (truePositiveR + falsePositiveR) != 0 ? truePositiveR / (truePositiveR + falsePositiveR) : 0;
		float precisionD = (truePositiveD + falsePositiveD) != 0 ? truePositiveD / (truePositiveD + falsePositiveD) : 0;
		
		float recallV = (truePositiveV + falseNegative) != 0 ? truePositiveV / (truePositiveV + falseNegative) : 0;
		float recallX = (truePositiveX + falseNegative) != 0 ? truePositiveX / (truePositiveX + falseNegative) : 0;
		float recallR = (truePositiveR + falseNegative) != 0 ? truePositiveR / (truePositiveR + falseNegative) : 0;
		float recallD = (truePositiveD + falseNegative) != 0 ? truePositiveD / (truePositiveD + falseNegative) : 0;
		
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;

		pmResult.write("EFFERENT COUPLING");
		pmResult.write("False Positive (V) = ", falsePositiveV);
		pmResult.write("False Positive (X) = ", falsePositiveX);
		pmResult.write("False Positive (R) = ", falsePositiveR);
		pmResult.write("False Positive (D) = ", falsePositiveD);
		pmResult.write("True Positive  (V) = ", truePositiveV);
		pmResult.write("True Positive (X) = ", truePositiveX);
		pmResult.write("True Positive (R) = ", truePositiveR);
		pmResult.write("True Positive (D) = ", truePositiveD);
		pmResult.write("False Negative = ", falseNegative);
		pmResult.write("Precision (V) = ", precisionV);
		pmResult.write("Precision (X) = ", precisionX);
		pmResult.write("Precision (R) = ", precisionR);
		pmResult.write("Precision (D) = ", precisionD);
		pmResult.write("Recall (V) = ", recallV);
		pmResult.write("Recall (X) = ", recallX);
		pmResult.write("Recall (R) = ", recallR);
		pmResult.write("Recall (D) = ", recallD);
		pmResult.write("F-measure (V) = ", fMeasureV);
		pmResult.write("F-measure (X) = ", fMeasureX);
		pmResult.write("F-measure (R) = ", fMeasureR);
		pmResult.write("F-measure (D) = ", fMeasureD);
		pmResult.write("");

	}


	private static void saveManyParametersMethodEvaluation(SmellRefactoredResult result, final PersistenceMechanism pmResult) {
		float falseNegative = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodNotSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if ((method.getSmell() == null) && method.getRefactoringType().contains("RENAME")) {
					falseNegative++;
				}
			}
		}
		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;
		for (MethodDataSmelly method : result.getMethodInitialSmellyNotRefactored()) {
			if (method.getSmell().equals("Muitos Parametros")) {
				if (method.getListaTecnicas().contains("V"))
					falsePositiveV++;
				if (method.getListaTecnicas().contains("X"))
					falsePositiveX++;
				if (method.getListaTecnicas().contains("R"))
					falsePositiveR++;
				if (method.getListaTecnicas().contains("D"))
					falsePositiveD++;
			}
		}
		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;
		for (List<RefactoringData> listMethods : result.getListRefactoringsByMethodSmelly().values()) {
			for (RefactoringData method : listMethods) {
				if (method.getSmell().equals("Muitos Parametros") && method.getRefactoringType().contains("RENAME")) {
					if (method.getListaTecnicas().contains("V"))
						truePositiveV++;
					if (method.getListaTecnicas().contains("X"))
						truePositiveX++;
					if (method.getListaTecnicas().contains("R"))
						truePositiveR++;
					if (method.getListaTecnicas().contains("D"))
						truePositiveD++;
				}
			}
		}
		float precisionV = (truePositiveV + falsePositiveV) != 0 ? truePositiveV / (truePositiveV + falsePositiveV) : 0;
		float precisionX = (truePositiveX + falsePositiveX) != 0 ? truePositiveX / (truePositiveX + falsePositiveX) : 0;
		float precisionR = (truePositiveR + falsePositiveR) != 0 ? truePositiveR / (truePositiveR + falsePositiveR) : 0;
		float precisionD = (truePositiveD + falsePositiveD) != 0 ? truePositiveD / (truePositiveD + falsePositiveD) : 0;
		
		float recallV = (truePositiveV + falseNegative) != 0 ? truePositiveV / (truePositiveV + falseNegative) : 0;
		float recallX = (truePositiveX + falseNegative) != 0 ? truePositiveX / (truePositiveX + falseNegative) : 0;
		float recallR = (truePositiveR + falseNegative) != 0 ? truePositiveR / (truePositiveR + falseNegative) : 0;
		float recallD = (truePositiveD + falseNegative) != 0 ? truePositiveD / (truePositiveD + falseNegative) : 0;
		
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;

		pmResult.write("MANY PARAMETERS");
		pmResult.write("False Positive (V) = ", falsePositiveV);
		pmResult.write("False Positive (X) = ", falsePositiveX);
		pmResult.write("False Positive (R) = ", falsePositiveR);
		pmResult.write("False Positive (D) = ", falsePositiveD);
		pmResult.write("True Positive  (V) = ", truePositiveV);
		pmResult.write("True Positive (X) = ", truePositiveX);
		pmResult.write("True Positive (R) = ", truePositiveR);
		pmResult.write("True Positive (D) = ", truePositiveD);
		pmResult.write("False Negative = ", falseNegative);
		pmResult.write("Precision (V) = ", precisionV);
		pmResult.write("Precision (X) = ", precisionX);
		pmResult.write("Precision (R) = ", precisionR);
		pmResult.write("Precision (D) = ", precisionD);
		pmResult.write("Recall (V) = ", recallV);
		pmResult.write("Recall (X) = ", recallX);
		pmResult.write("Recall (R) = ", recallR);
		pmResult.write("Recall (D) = ", recallD);
		pmResult.write("F-measure (V) = ", fMeasureV);
		pmResult.write("F-measure (X) = ", fMeasureX);
		pmResult.write("F-measure (R) = ", fMeasureR);
		pmResult.write("F-measure (D) = ", fMeasureD);
		pmResult.write("");
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
