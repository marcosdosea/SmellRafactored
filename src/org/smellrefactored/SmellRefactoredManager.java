package org.smellrefactored;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

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
import org.eclipse.jgit.revwalk.DepthWalk.RevWalk;
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

import com.opencsv.CSVReader;

public class SmellRefactoredManager {

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredManager.class);

	private String urlRepository;
	private String localFolder;
	private String initialCommit;
	private String finalCommit;
	private List<LimiarTecnica> listaLimiarTecnica;
	private GitService gitService;
	private Repository repo;
	private TechniqueExecutor executor;

	public SmellRefactoredManager(String urlRepository, String localFolder, String initialCommit, String finalCommit,
			List<LimiarTecnica> listaLimiarTecnica) {
		this.urlRepository = urlRepository;
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.listaLimiarTecnica = listaLimiarTecnica;
		gitService = new GitServiceImpl();
		executor = new TechniqueExecutor(new DesignRoleTechnique());
	}

	public SmellRefactoredResult getSmellRefactoredBetweenCommit(String resultFile) {

		SmellRefactoredResult result = new SmellRefactoredResult();

		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		try {
			repo = gitService.cloneIfNotExists(localFolder, urlRepository);
			final PersistenceMechanism pm = new CSVFile(System.getProperty("user.dir") + "\\refactoring.csv");

			pm.write("Commmit-id", "Refactring-name", "Refactoring-Type", "Code Element Left", "Code Element Right",
					"Class Before", "Class After");

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

			// Obter lista de commits que possuem refatorações
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

			// Filtra os commits com refactoring cujo commit feito no master ou pull request
			// realizados no master
			ArrayList<CommitData> commitsMergedIntoMaster = new ArrayList<CommitData>();
			ArrayList<CommitData> commitsNotMergedIntoMaster = new ArrayList<CommitData>();

			Git git = new Git(repo);
			gitService.checkout(repo, finalCommit);

			Iterable<RevCommit> log = git.log().call();
			Iterator<RevCommit> logIterator = log.iterator();
			RevWalk revWalk = new RevWalk(repo, 3);
			RevCommit masterHead = revWalk.parseCommit(repo.resolve("refs/heads/master"));
			while (logIterator.hasNext()) {
				RevCommit currentCommit = logIterator.next();
				CommitData commitData = new CommitData();
				commitData.setId(currentCommit.getId().getName());
				commitData.setFullMessage(currentCommit.getFullMessage());
				commitData.setShortMessage(currentCommit.getShortMessage());
				commitData.setDate(new Date(currentCommit.getCommitTime() * 1000L));
				ObjectId id = repo.resolve(commitData.getId());
				RevCommit otherHead = revWalk.parseCommit(id);
				if (revWalk.isMergedInto(otherHead, masterHead)) {
					if ((otherHead.getParentCount() == 1) || (otherHead.getShortMessage().contains("merge")
							&& otherHead.getShortMessage().contains("pull"))) {
						commitsMergedIntoMaster.add(commitData);
					} else {
						commitsNotMergedIntoMaster.add(commitData);
					}
				}
				revWalk.close();
				revWalk.dispose();
			}
			git.close();

			Collections.sort(commitsMergedIntoMaster);
			Collections.sort(commitsNotMergedIntoMaster);

			ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster = new ArrayList<CommitData>();
			Iterator<CommitData> it = commitsMergedIntoMaster.iterator();
			CommitData previousCommit = null;
			while (it.hasNext()) {
				CommitData commit = it.next();
				commit.setPrevious(previousCommit);
				previousCommit = new CommitData();
				previousCommit.setDate(commit.getDate());
				previousCommit.setFullMessage(commit.getFullMessage());
				previousCommit.setShortMessage(commit.getShortMessage());
				previousCommit.setId(commit.getId());
				if (commitsWithRefactorings.contains(commit.getId()))
					commitsWithRefactoringMergedIntoMaster.add(commit);
			}

			FilterSmellResult smellsCommitInitial = obterSmellsCommit(initialCommit, repo);
			//
			// HashSet<MethodDataSmelly> metodosSmellyInitialNotRefactored =
			// smellsCommitInitial.getMetodosSmell();
			// Iterator<String> itCommits =
			// commitsWithRefactoringsMergedIntoMaster.iterator();
			// FilterSmellResult smellsCommit;
			// Map<String, List<RefactoringData>> listRefactoringsByMethodNotSmelly = new
			// HashMap<String, List<RefactoringData>>();
			// Map<String, List<RefactoringData>> listRefactoringsByMethodSmelly = new
			// HashMap<String, List<RefactoringData>>();

			//
			// gitService.checkout(repo, previousCommitId);
			// logger.info("Iniciando a coleta de métricas do commit " + previousCommitId +
			// "...");
			// // ArrayList<String> projetosAnalisar = new ArrayList<String>();
			// // projetosAnalisar.add(localFolder);
			// MetricReport reportInitial =
			// executor.getMetricsFromProjects(projetosAnalisar,
			// System.getProperty("user.dir") + "\\metrics-initial\\", false);
			// // logger.info("Gerando smells com a lista de problemas de design
			// // encontrados...");
			// smellsCommit = FilterSmells.filtrar(reportInitial.all(), listaTecnicas,
			// commitAnalisar);
			//
			// // logger.info("Gerando lista de métodos smells com refatorações...");
			// for (MethodDataSmelly methodSmelly : smellsCommit.getMetodosSmell()) {
			//
			// for (RefactoringData refactoring : listRefactoringCommitAnalisado) {
			// boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
			// .contains(methodSmelly.getNomeClasse())
			// ||
			// refactoring.getInvolvedClassesAfter().contains(methodSmelly.getNomeClasse());
			//
			// boolean isMethodRefactored =
			// refactoring.getLeftSide().contains(methodSmelly.getNomeMetodo())
			// || refactoring.getRightSide().contains(methodSmelly.getNomeMetodo());
			// if (isClassInvolved && isMethodRefactored) {
			// String key = methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo()
			// + methodSmelly.getCommit() + methodSmelly.getSmell();
			// List<RefactoringData> listByMethod = listRefactoringsByMethodSmelly.get(key);
			// if (listByMethod == null)
			// listByMethod = new ArrayList<RefactoringData>();
			// RefactoringData refactoringResult = atribuir(methodSmelly, refactoring);
			// refactoringResult.setNumberOfClasses(reportInitial.getNumberOfClasses());
			// refactoringResult.setSystemLOC(reportInitial.getSystemLOC());
			// refactoringResult.setNumberOfMethods(reportInitial.getNumberOfMethods());
			// listByMethod.add(refactoringResult);
			// listRefactoringsByMethodSmelly.put(key, listByMethod);
			// metodosSmellyInitialNotRefactored.remove(methodSmelly); // ou seja, é smelly
			// e foi
			// // refatorado em algum momento da
			// // evolução
			// }

			//
			// // logger.info("Gerando lista de métodos not smells com refatorações...");
			// for (MethodDataSmelly methodNotSmelly : smellsCommit.getMetodosNotSmelly()) {
			// for (RefactoringData refactoring : listRefactoringCommitAnalisado) {
			// boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
			// .contains(methodNotSmelly.getNomeClasse())
			// ||
			// refactoring.getInvolvedClassesAfter().contains(methodNotSmelly.getNomeClasse());
			//
			// boolean isMethodRefactored =
			// refactoring.getLeftSide().contains(methodNotSmelly.getNomeMetodo())
			// || refactoring.getRightSide().contains(methodNotSmelly.getNomeMetodo());
			// if (isClassInvolved && isMethodRefactored) {
			// String key = methodNotSmelly.getNomeClasse() +
			// methodNotSmelly.getNomeMetodo()
			// + methodNotSmelly.getCommit();
			// List<RefactoringData> listByMethod =
			// listRefactoringsByMethodNotSmelly.get(key);
			// if (listByMethod == null)
			// listByMethod = new ArrayList<RefactoringData>();
			//
			// RefactoringData refactoringResult = atribuir(methodNotSmelly, refactoring);
			// refactoringResult.setNumberOfClasses(reportInitial.getNumberOfClasses());
			// refactoringResult.setSystemLOC(reportInitial.getSystemLOC());
			// refactoringResult.setNumberOfMethods(reportInitial.getNumberOfMethods());
			// listByMethod.add(refactoringResult);
			// listRefactoringsByMethodNotSmelly.put(key, listByMethod);
			// }
			// }
			// }
			// }

			result.setListRefactoring(listRefactoring);
			// result.setListRefactoringsByMethodSmelly(listRefactoringsByMethodSmelly);
			// result.setListRefactoringsByMethodNotSmelly(listRefactoringsByMethodNotSmelly);
			// result.setMethodSmellyInitialNotRefactored(metodosSmellyInitialNotRefactored);
			result.setSmellsCommitInitial(smellsCommitInitial);
			final PersistenceMechanism pmResult = new CSVFile(resultFile, true);

			pmResult.write("RELATÓRIO COMPLETO SISTEMA");

			pmResult.write("Numero Refatoracoes em Metodos e Nao Metodos:", result.getListRefactoring().size());
			pmResult.write("Numero Metodos Smell Commit Inicial:",
					result.getSmellsCommitInitial().getMetodosSmell().size());
			pmResult.write("Numero Metodos NOT Smell Commit Inicial:",
					result.getSmellsCommitInitial().getMetodosNotSmelly().size());

			evaluateLongMethod(result, pmResult, repo);

			return result;
		} catch (

		Exception e) {
			logger.error(e.getMessage());
		}
		return result;

	}

	private FilterSmellResult obterSmellsCommit(String commit, Repository repo) throws Exception {
		gitService.checkout(repo, commit);
		logger.info("Iniciando a coleta de métricas do commit " + commit + "...");
		ArrayList<String> projetosAnalisar = new ArrayList<String>();
		projetosAnalisar.add(localFolder);
		MetricReport report = executor.getMetricsFromProjects(projetosAnalisar,
				System.getProperty("user.dir") + "\\metrics\\", commit);
		logger.info("Gerando smells com a lista de problemas de design encontrados...");
		FilterSmellResult smellsCommitInitial = FilterSmells.filtrar(report.all(), listaLimiarTecnica, commit);

		return smellsCommitInitial;
	}

	// private static RefactoringData atribuir(MethodDataSmelly method,
	// RefactoringData refactoring) {
	// RefactoringData refactoringResult = new RefactoringData();
	// refactoringResult.setCommit(method.getCommit());
	// refactoringResult.setFullMessage(refactoring.getFullMessage());
	// refactoringResult.setCommitDate(refactoring.getCommitDate());
	// refactoringResult.setNumberOfClasses(refactoring.getNumberOfClasses());
	// refactoringResult.setNumberOfMethods(refactoring.getNumberOfMethods());
	// refactoringResult.setSystemLOC(refactoring.getSystemLOC());
	//
	// refactoringResult.setInvolvedClassesAfter(refactoring.getInvolvedClassesAfter());
	// refactoringResult.setInvolvedClassesBefore(refactoring.getInvolvedClassesBefore());
	// refactoringResult.setLeftSide(refactoring.getLeftSide());
	// refactoringResult.setRightSide(refactoring.getRightSide());
	// refactoringResult.setRefactoringName(refactoring.getRefactoringName());
	// refactoringResult.setRefactoringType(refactoring.getRefactoringType());
	// refactoringResult.setShortMessage(refactoring.getShortMessage());
	// refactoringResult.setNomeClasse(method.getNomeClasse());
	// refactoringResult.setNomeMetodo(method.getNomeMetodo());
	// refactoringResult.setSmell(method.getSmell());
	// refactoringResult.setClassDesignRole(method.getClassDesignRole());
	// refactoringResult.setComplexity(method.getComplexity());
	// refactoringResult.setEfferent(method.getEfferent());
	// refactoringResult.setNumberOfParameters(method.getNumberOfParameters());
	// refactoringResult.setLinesOfCode(method.getLinesOfCode());
	// refactoringResult.setListaTecnicas(method.getListaTecnicas());
	// return refactoringResult;
	// }

	public void storeResult(SmellRefactoredResult result, String fileName, boolean imprimirMensagemCommit) {
		final PersistenceMechanism pmRef = new CSVFile(fileName);
		pmRef.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas", "Commit", "Refactoring",
				"Left Side", "Right Side", "Full Message");

		if (result.getListRefactoringsByMethodSmelly() != null) {
			for (String keyMetodo : result.getListRefactoringsByMethodSmelly().keySet()) {
				List<RefactoringData> lista = result.getListRefactoringsByMethodSmelly().get(keyMetodo);
				if (lista != null) {
					for (RefactoringData ref : lista) {
						pmRef.write(ref.getNomeClasse(), ref.getNomeMetodo(), ref.getSmell(), ref.getLinesOfCode(),
								ref.getComplexity(), ref.getEfferent(), ref.getNumberOfParameters(),
								ref.getListaTecnicas(), ref.getCommit(), ref.getRefactoringType(), ref.getLeftSide(),
								ref.getRightSide(), imprimirMensagemCommit ? ref.getFullMessage() : "");
					}
				}
			}
		}

		if (result.getListRefactoringsByMethodNotSmelly() != null) {
			for (String keyMetodo : result.getListRefactoringsByMethodNotSmelly().keySet()) {
				List<RefactoringData> lista = result.getListRefactoringsByMethodNotSmelly().get(keyMetodo);
				if (lista != null) {
					for (RefactoringData ref : lista) {
						pmRef.write(ref.getNomeClasse(), ref.getNomeMetodo(), ref.getSmell(), ref.getLinesOfCode(),
								ref.getComplexity(), ref.getEfferent(), ref.getNumberOfParameters(),
								ref.getListaTecnicas(), ref.getCommit(), ref.getRefactoringType(), ref.getLeftSide(),
								ref.getRightSide(), imprimirMensagemCommit ? ref.getFullMessage() : "");
					}
				}
			}
		}

		if (result.getMethodInitialSmellyNotRefactored() != null) {
			for (MethodDataSmelly methodSmelly : result.getMethodInitialSmellyNotRefactored()) {
				pmRef.write(methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
						methodSmelly.getLinesOfCode(), methodSmelly.getComplexity(), methodSmelly.getEfferent(),
						methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(), methodSmelly.getCommit(),
						"", "", "", "");
			}
		}
	}

	private void evaluateLongMethod(SmellRefactoredResult result, final PersistenceMechanism pmResult, Repository repo)
			throws Exception {

		float falseNegative = 0;
		float trueNegative = 0;
		for (MethodDataSmelly methodNotSmelly : result.getSmellsCommitInitial().getMetodosNotSmelly()) {
			MethodDataSmelly methodNotSmellyBuscar = methodNotSmelly;
			boolean renamedMethod = false;
			boolean refactoredMethod = false;
			do {
				renamedMethod = false;
				String methodRenamedName = null;
				for (RefactoringData methodRefactored : result.getListRefactoring()) {
					boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
							.contains(methodNotSmellyBuscar.getNomeClasse())
							|| methodRefactored.getInvolvedClassesAfter()
									.contains(methodNotSmellyBuscar.getNomeClasse());

					boolean isMethodRefactored = methodRefactored.getLeftSide()
							.contains(methodNotSmellyBuscar.getNomeMetodo())
							|| methodRefactored.getRightSide().contains(methodNotSmellyBuscar.getNomeMetodo());

					if (isClassInvolved && isMethodRefactored) {
						if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
								&& methodRefactored.getLeftSide().contains(methodNotSmellyBuscar.getNomeMetodo())
								&& methodRefactored.getInvolvedClassesBefore()
										.contains(methodNotSmellyBuscar.getNomeClasse())) {
							renamedMethod = true;
							methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
						} else if (methodRefactored.getRefactoringType().contains("OPERATION")) {
							FilterSmellResult smellsCommit = obterSmellsCommit(methodRefactored.getCommit(), repo);
							falseNegative++; // olhar o commit
							refactoredMethod = true;
							break; // busca pelo menos uma refatoracao
						}
					}
				}
				if (renamedMethod) {
					methodNotSmellyBuscar.setNomeMetodo(methodRenamedName);
				}
				logger.info(methodNotSmellyBuscar.getNomeMetodo() + " " + renamedMethod + " " + refactoredMethod);

			} while (renamedMethod && !refactoredMethod);

			if (!refactoredMethod) {
				trueNegative++;
			}
		}

		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;

		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;

		for (MethodDataSmelly methodSmelly : result.getSmellsCommitInitial().getMetodosSmell()) {
			MethodDataSmelly methodSmellyBuscar = methodSmelly;
			boolean renamedMethod = false;
			boolean refactoredMethodV = false;
			boolean refactoredMethodX = false;
			boolean refactoredMethodR = false;
			boolean refactoredMethodD = false;
			do {
				renamedMethod = false;
				String methodRenamedName = null;
				for (RefactoringData methodRefactored : result.getListRefactoring()) {
					boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
							.contains(methodSmellyBuscar.getNomeClasse())
							|| methodRefactored.getInvolvedClassesAfter().contains(methodSmellyBuscar.getNomeClasse());
					boolean isMethodRefactored = methodRefactored.getLeftSide()
							.contains(methodSmellyBuscar.getNomeMetodo())
							|| methodRefactored.getRightSide().contains(methodSmellyBuscar.getNomeMetodo());

					if (isClassInvolved && isMethodRefactored) {
						if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
								&& methodRefactored.getLeftSide().contains(methodSmellyBuscar.getNomeMetodo())
								&& methodRefactored.getInvolvedClassesBefore()
										.contains(methodSmellyBuscar.getNomeClasse())) {
							renamedMethod = true;
							methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
						} else if (methodRefactored.getRefactoringType().contains("OPERATION")) {
							// olhar dentro do commit
							if (methodSmellyBuscar.getSmell().equals("Metodo Longo")) {
								if (!refactoredMethodV && methodSmellyBuscar.getListaTecnicas().contains("V")) {
									truePositiveV++;
									refactoredMethodV = true;
								}
								if (!refactoredMethodX && methodSmellyBuscar.getListaTecnicas().contains("X")) {
									truePositiveX++;
									refactoredMethodX = true;
								}
								if (!refactoredMethodR && methodSmellyBuscar.getListaTecnicas().contains("R")) {
									truePositiveR++;
									refactoredMethodR = true;
								}
								if (!refactoredMethodD && methodSmellyBuscar.getListaTecnicas().contains("D")) {
									truePositiveD++;
									refactoredMethodD = true;
								}
							}
						}
					}
				}
				if (renamedMethod) {
					methodSmellyBuscar.setNomeMetodo(methodRenamedName);
				}
			} while (renamedMethod
					&& (!refactoredMethodD || !refactoredMethodR || !refactoredMethodV || !refactoredMethodX));
			if (!refactoredMethodD) {
				falsePositiveD++;
			}
			if (!refactoredMethodV) {
				falsePositiveV++;
			}
			if (!refactoredMethodR) {
				falsePositiveR++;
			}
			if (!refactoredMethodX) {
				falsePositiveX++;
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

		float accuracyV = (falsePositiveV + truePositiveV + falseNegative + trueNegative) != 0
				? (truePositiveV + trueNegative) / (falsePositiveV + truePositiveV + falseNegative + trueNegative)
				: 0;
		float accuracyX = (falsePositiveX + truePositiveX + falseNegative + trueNegative) != 0
				? (truePositiveX + trueNegative) / (falsePositiveX + truePositiveX + falseNegative + trueNegative)
				: 0;
		float accuracyR = (falsePositiveR + truePositiveR + falseNegative + trueNegative) != 0
				? (truePositiveR + trueNegative) / (falsePositiveR + truePositiveR + falseNegative + trueNegative)
				: 0;
		float accuracyD = (falsePositiveD + truePositiveD + falseNegative + trueNegative) != 0
				? (truePositiveD + trueNegative) / (falsePositiveD + truePositiveD + falseNegative + trueNegative)
				: 0;

		pmResult.write("LONG METHODS");
		pmResult.write("True Negative", trueNegative);
		pmResult.write("False Negative = ", falseNegative);
		pmResult.write("False Positive (V) = ", falsePositiveV);
		pmResult.write("False Positive (X) = ", falsePositiveX);
		pmResult.write("False Positive (R) = ", falsePositiveR);
		pmResult.write("False Positive (D) = ", falsePositiveD);
		pmResult.write("True Positive  (V) = ", truePositiveV);
		pmResult.write("True Positive (X) = ", truePositiveX);
		pmResult.write("True Positive (R) = ", truePositiveR);
		pmResult.write("True Positive (D) = ", truePositiveD);
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
		pmResult.write("Acuracia (V) = ", accuracyV);
		pmResult.write("Acuracia (X) = ", accuracyX);
		pmResult.write("Acuracia (R) = ", accuracyR);
		pmResult.write("Acuracia (D) = ", accuracyD);

		pmResult.write("");
	}

	private static String extrairNomeMetodo(String rightSide) {
		String nomeMetodo = rightSide.substring(0, rightSide.indexOf(")") + 1);
		if (nomeMetodo.contains("public "))
			nomeMetodo = nomeMetodo.substring("public ".length());
		if (nomeMetodo.contains("private "))
			nomeMetodo = nomeMetodo.substring("private ".length());
		if (nomeMetodo.contains("protected "))
			nomeMetodo = nomeMetodo.substring("protected ".length());
		return nomeMetodo;
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
