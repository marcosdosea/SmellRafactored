package org.smellrefactored;

import java.io.File;
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

	private String resultFileName;
	ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster;
	PersistenceMechanism pmResultEvaluation;
	PersistenceMechanism pmResultSmellRefactored;
	PersistenceMechanism pmResultSmellRefactoredMessage;
	PersistenceMechanism pmResultSmellRefactoredCommit;
	HashSet<String> listCommitEvaluated = new HashSet<String>();

	public SmellRefactoredManager(String urlRepository, String localFolder, String initialCommit, String finalCommit,
			List<LimiarTecnica> listaLimiarTecnica, String resultFileName) {
		this.urlRepository = urlRepository;
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultFileName = resultFileName;
		gitService = new GitServiceImpl();
		commitsWithRefactoringMergedIntoMaster = new ArrayList<CommitData>();
		pmResultEvaluation = new CSVFile(resultFileName + "-evaluations.csv", false);
		pmResultSmellRefactored = new CSVFile(resultFileName + "-smellRefactored.csv", false);
		pmResultSmellRefactoredMessage = new CSVFile(resultFileName + "-smellRefactored-message.csv", false);
		pmResultSmellRefactoredCommit = new CSVFile(resultFileName + "-smellRefactored-commit.csv", false);
	}

	public void getSmellRefactoredBetweenCommit() {

		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		try {
			repo = gitService.cloneIfNotExists(localFolder, urlRepository);

			String fileRefactoringName = resultFileName + "-refactoring.csv";
			File fileRefactoring = new File(fileRefactoringName);
			if (!fileRefactoring.exists()) {
				final PersistenceMechanism pmRefactoring = new CSVFile(fileRefactoringName, false);
				pmRefactoring.write("Commmit-id", "Refactring-name", "Refactoring-Type", "Code Element Left",
						"Code Element Right", "Class Before", "Class After");

				// detect list of refactoring between commits
				miner.detectBetweenCommits(repo, initialCommit, finalCommit, new RefactoringHandler() {
					@Override
					public void handle(String idCommit, List<Refactoring> refactorings) {

						for (Refactoring ref : refactorings) {

							pmRefactoring.write(idCommit, ref.getName(), ref.getRefactoringType(),
									ref.leftSide().size() > 0 ? ref.leftSide().get(0).getCodeElement() : 0,
									ref.rightSide().size() > 0 ? ref.rightSide().get(0).getCodeElement() : 0,
									ref.getInvolvedClassesBeforeRefactoring(),
									ref.getInvolvedClassesAfterRefactoring());
						}
					}
				});
			}

			// Obter lista de commits que possuem refatorações
			ArrayList<RefactoringData> listRefactoring = new ArrayList<RefactoringData>();
			HashSet<String> commitsWithRefactorings = new HashSet<String>();
			try {
				CSVReader reader = new CSVReader(new FileReader(fileRefactoringName));
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
			logger.info("Tota de refactorings encontrados: " + listRefactoring.size());

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
					if ((otherHead.getParentCount() == 1)
							|| (otherHead.getShortMessage().toUpperCase().contains("MERGE")
									&& otherHead.getShortMessage().toUpperCase().contains("PULL"))) {
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

			int countRefactoringRelatedOperations = 0;
			int countRefactoringRelatedRenaming = 0;

			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (refactoring.getRefactoringType().contains("OPERATION"))
							countRefactoringRelatedOperations++;
						if (refactoring.getRefactoringType().equals("RENAME_METHOD"))
							countRefactoringRelatedRenaming++;
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);
			pmResultEvaluation.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluation.write("Numero Refatoracoes em Metodos e Nao Metodos:",
					listRefactoringMergedIntoMaster.size());
			pmResultEvaluation.write("Numero Refatoracoes relacionadas a operacoes em Metodos:",
					countRefactoringRelatedOperations);
			pmResultEvaluation.write("Numero Refatoracoes relacionadas a rename em Metodos:",
					countRefactoringRelatedRenaming);

			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");
			FilterSmellResult smellsCommitInitial = obterSmellsCommit(initialCommit, repo);

			pmResultEvaluation.write("Numero Metodos Smell Commit Inicial:",
					smellsCommitInitial.getMetodosSmell().size());
			pmResultEvaluation.write("Numero Metodos NOT Smell Commit Inicial:",
					smellsCommitInitial.getMetodosNotSmelly().size());

			pmResultSmellRefactoredMessage.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas",
					"Commit", "Refactoring", "Left Side", "Right Side", "Full Message");
			pmResultSmellRefactored.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas", "Commit",
					"Refactoring", "Left Side", "Right Side");

			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.LONG_METHOD);
			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.COMPLEX_METHOD);
			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.HIGH_EFFERENT_COUPLING);
			evaluateSmellChangeParameters(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.MANY_PARAMETERS);
		} catch (

		Exception e) {
			logger.error(e.getMessage());
		}
	}

	private FilterSmellResult obterSmellsCommit(String commit, Repository repo) throws Exception {
		logger.info("Iniciando a coleta de métricas do commit " + commit + "...");
		ArrayList<String> projetosAnalisar = new ArrayList<String>();
		projetosAnalisar.add(localFolder);
		TechniqueExecutor executor = new TechniqueExecutor(repo, gitService);
		MetricReport report = executor.getMetricsFromProjects(projetosAnalisar,
				System.getProperty("user.dir") + "\\metrics\\", commit);

		if (!listCommitEvaluated.contains(commit)) {
			listCommitEvaluated.add(commit);
			pmResultSmellRefactoredCommit.write(commit, report.getNumberOfClasses(), report.getNumberOfMethods(),
					report.getSystemLOC());
		}
		logger.info("Gerando smells com a lista de problemas de design encontrados...");
		FilterSmellResult smellsCommitInitial = FilterSmells.filtrar(report.all(), listaLimiarTecnica, commit);
		FilterSmells.gravarMetodosSmell(smellsCommitInitial.getMetodosSmell(),
				resultFileName + "-smells-commit-initial.csv");
		return smellsCommitInitial;
	}

	private void evaluateSmellChangeOperation(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, Repository repo, String typeSmell) throws Exception {

		float falseNegative = 0;
		float trueNegative = 0;

		for (MethodDataSmelly methodNotSmelly : commitInitial.getMetodosNotSmelly()) {
			MethodDataSmelly methodBuscar = methodNotSmelly;
			boolean renamedMethod = false;
			boolean refactoredMethod = false;
			Date dateCommitRenamed = null;
			do {
				renamedMethod = false;
				String methodRenamedName = null;
				for (RefactoringData methodRefactored : listRefactoring) {
					boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
							.contains(methodBuscar.getNomeClasse())
							|| methodRefactored.getInvolvedClassesAfter().contains(methodBuscar.getNomeClasse());

					boolean isMethodRefactored = methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
							|| methodRefactored.getRightSide().contains(methodBuscar.getNomeMetodo());

					if (isClassInvolved && isMethodRefactored) {
						if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
								&& methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
								&& methodRefactored.getInvolvedClassesBefore().contains(methodBuscar.getNomeClasse())) {

							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
								renamedMethod = true;
								dateCommitRenamed = methodRefactored.getCommitDate();
								methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
							}
						} else if (methodRefactored.getRefactoringType().contains("OPERATION")) {
							FilterSmellResult smellsPreviousCommit = obterSmellsPreviousCommit(repo,
									methodRefactored.getCommit());
							for (MethodDataSmelly methodNotSmell : smellsPreviousCommit.getMetodosNotSmelly()) {
								boolean isSameClassMethod = methodNotSmell.getNomeClasse()
										.equals(methodBuscar.getNomeClasse())
										&& methodNotSmell.getNomeMetodo().equals(methodBuscar.getNomeMetodo());
								if (isSameClassMethod) {
									falseNegative++;
									refactoredMethod = true;
									pmResultSmellRefactoredMessage.write(methodRefactored.getNomeClasse(),
											methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
											methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
											methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
											methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
											methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
											methodRefactored.getRightSide(), methodRefactored.getFullMessage());

									pmResultSmellRefactored.write(methodRefactored.getNomeClasse(),
											methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
											methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
											methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
											methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
											methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
											methodRefactored.getRightSide());

									break;
								}
							}
						}
					}
				}
				if (renamedMethod)
					methodBuscar.setNomeMetodo(methodRenamedName);
				else
					dateCommitRenamed = null;
			} while (renamedMethod && !refactoredMethod);
			if (!refactoredMethod) {
				trueNegative++;
				pmResultSmellRefactoredMessage.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "", "");

				pmResultSmellRefactored.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "");
			}
		}

		float truePositiveA = 0;
		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;

		float falsePositiveA = 0;
		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;

		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {

			if (methodSmelly.getSmell().equals(typeSmell)) {
				MethodDataSmelly methodSmellyBuscar = methodSmelly;
				boolean renamedMethod = false;
				boolean refactoredMethodA = false;
				boolean refactoredMethodV = false;
				boolean refactoredMethodX = false;
				boolean refactoredMethodR = false;
				boolean refactoredMethodD = false;
				Date dateCommitRenamed = null;
				do {
					renamedMethod = false;
					String methodRenamedName = null;
					for (RefactoringData methodRefactored : listRefactoring) {
						boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
								.contains(methodSmellyBuscar.getNomeClasse())
								|| methodRefactored.getInvolvedClassesAfter()
										.contains(methodSmellyBuscar.getNomeClasse());
						boolean isMethodRefactored = methodRefactored.getLeftSide()
								.contains(methodSmellyBuscar.getNomeMetodo())
								|| methodRefactored.getRightSide().contains(methodSmellyBuscar.getNomeMetodo());

						if (isClassInvolved && isMethodRefactored) {
							if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
									&& methodRefactored.getLeftSide().contains(methodSmellyBuscar.getNomeMetodo())
									&& methodRefactored.getInvolvedClassesBefore()
											.contains(methodSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
									renamedMethod = true;
									dateCommitRenamed = methodRefactored.getCommitDate();
									methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
								}
							} else if (methodRefactored.getRefactoringType().contains("OPERATION")) {
								FilterSmellResult smellsPreviousCommit = obterSmellsPreviousCommit(repo,
										methodRefactored.getCommit());
								for (MethodDataSmelly methodSmell : smellsPreviousCommit.getMetodosSmell()) {
									boolean isSameClassMethod = methodSmell.getNomeClasse()
											.equals(methodSmellyBuscar.getNomeClasse())
											&& methodSmell.getNomeMetodo().equals(methodSmellyBuscar.getNomeMetodo());
									if ((isSameClassMethod) && methodSmell.getSmell().equals(typeSmell)) {
										if (!refactoredMethodA && methodSmellyBuscar.getListaTecnicas().contains("A")) {
											truePositiveA++;
											refactoredMethodA = true;
										}
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
										pmResultSmellRefactoredMessage.write(methodRefactored.getNomeClasse(),
												methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
												methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
												methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
												methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
												methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
												methodRefactored.getRightSide(), methodRefactored.getFullMessage());
										pmResultSmellRefactored.write(methodRefactored.getNomeClasse(),
												methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
												methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
												methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
												methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
												methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
												methodRefactored.getRightSide());
									}
								}
							}
						}
					}
					if (renamedMethod)
						methodSmellyBuscar.setNomeMetodo(methodRenamedName);
					else
						dateCommitRenamed = null;
				} while (renamedMethod
						&& (!refactoredMethodA || !refactoredMethodD || !refactoredMethodR || !refactoredMethodV || !refactoredMethodX));
				if (!refactoredMethodA && methodSmellyBuscar.getListaTecnicas().contains("A"))
					falsePositiveA++;
				if (!refactoredMethodD && methodSmellyBuscar.getListaTecnicas().contains("D"))
					falsePositiveD++;
				if (!refactoredMethodV && methodSmellyBuscar.getListaTecnicas().contains("V"))
					falsePositiveV++;
				if (!refactoredMethodR && methodSmellyBuscar.getListaTecnicas().contains("R"))
					falsePositiveR++;
				if (!refactoredMethodX && methodSmellyBuscar.getListaTecnicas().contains("X"))
					falsePositiveX++;
				if (!refactoredMethodA || !refactoredMethodD || !refactoredMethodR || !refactoredMethodV || !refactoredMethodX) {
					pmResultSmellRefactoredMessage.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");

					pmResultSmellRefactored.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		float precisionA = (truePositiveA + falsePositiveA) != 0 ? truePositiveA / (truePositiveA + falsePositiveA) : 0;
		float precisionV = (truePositiveV + falsePositiveV) != 0 ? truePositiveV / (truePositiveV + falsePositiveV) : 0;
		float precisionX = (truePositiveX + falsePositiveX) != 0 ? truePositiveX / (truePositiveX + falsePositiveX) : 0;
		float precisionR = (truePositiveR + falsePositiveR) != 0 ? truePositiveR / (truePositiveR + falsePositiveR) : 0;
		float precisionD = (truePositiveD + falsePositiveD) != 0 ? truePositiveD / (truePositiveD + falsePositiveD) : 0;

		float recallA = (truePositiveA + falseNegative) != 0 ? truePositiveA / (truePositiveA + falseNegative) : 0;
		float recallV = (truePositiveV + falseNegative) != 0 ? truePositiveV / (truePositiveV + falseNegative) : 0;
		float recallX = (truePositiveX + falseNegative) != 0 ? truePositiveX / (truePositiveX + falseNegative) : 0;
		float recallR = (truePositiveR + falseNegative) != 0 ? truePositiveR / (truePositiveR + falseNegative) : 0;
		float recallD = (truePositiveD + falseNegative) != 0 ? truePositiveD / (truePositiveD + falseNegative) : 0;

		float fMeasureA = (precisionA + recallA) != 0 ? (2 * precisionA * recallA) / (precisionA + recallA) : 0;
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;

		float accuracyA = (falsePositiveA + truePositiveA + falseNegative + trueNegative) != 0
				? (truePositiveA + trueNegative) / (falsePositiveA + truePositiveA + falseNegative + trueNegative)
				: 0;
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

		pmResultEvaluation.write(typeSmell.toUpperCase());
		pmResultEvaluation.write("True Negative", trueNegative);
		pmResultEvaluation.write("False Negative = ", falseNegative);
		pmResultEvaluation.write("False Positive (A) = ", falsePositiveA);
		pmResultEvaluation.write("False Positive (V) = ", falsePositiveV);
		pmResultEvaluation.write("False Positive (X) = ", falsePositiveX);
		pmResultEvaluation.write("False Positive (R) = ", falsePositiveR);
		pmResultEvaluation.write("False Positive (D) = ", falsePositiveD);
		pmResultEvaluation.write("True Positive  (A) = ", truePositiveA);
		pmResultEvaluation.write("True Positive  (V) = ", truePositiveV);
		pmResultEvaluation.write("True Positive (X) = ", truePositiveX);
		pmResultEvaluation.write("True Positive (R) = ", truePositiveR);
		pmResultEvaluation.write("True Positive (D) = ", truePositiveD);
		pmResultEvaluation.write("Precision (A) = ", precisionA);
		pmResultEvaluation.write("Precision (V) = ", precisionV);
		pmResultEvaluation.write("Precision (X) = ", precisionX);
		pmResultEvaluation.write("Precision (R) = ", precisionR);
		pmResultEvaluation.write("Precision (D) = ", precisionD);
		pmResultEvaluation.write("Recall (A) = ", recallA);
		pmResultEvaluation.write("Recall (V) = ", recallV);
		pmResultEvaluation.write("Recall (X) = ", recallX);
		pmResultEvaluation.write("Recall (R) = ", recallR);
		pmResultEvaluation.write("Recall (D) = ", recallD);
		pmResultEvaluation.write("F-measure (A) = ", fMeasureA);
		pmResultEvaluation.write("F-measure (V) = ", fMeasureV);
		pmResultEvaluation.write("F-measure (X) = ", fMeasureX);
		pmResultEvaluation.write("F-measure (R) = ", fMeasureR);
		pmResultEvaluation.write("F-measure (D) = ", fMeasureD);
		pmResultEvaluation.write("Acuracia (A) = ", accuracyA);
		pmResultEvaluation.write("Acuracia (V) = ", accuracyV);
		pmResultEvaluation.write("Acuracia (X) = ", accuracyX);
		pmResultEvaluation.write("Acuracia (R) = ", accuracyR);
		pmResultEvaluation.write("Acuracia (D) = ", accuracyD);

		pmResultEvaluation.write("");
	}

	private void evaluateSmellChangeParameters(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, Repository repo, String typeSmell) throws Exception {

		float falseNegative = 0;
		float trueNegative = 0;

		for (MethodDataSmelly methodNotSmelly : commitInitial.getMetodosNotSmelly()) {
			MethodDataSmelly methodBuscar = methodNotSmelly;
			boolean renamedMethod = false;
			boolean refactoredMethod = false;
			Date dateCommitRenamed = null;
			do {
				renamedMethod = false;
				String methodRenamedName = null;
				for (RefactoringData methodRefactored : listRefactoring) {
					boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
							.contains(methodBuscar.getNomeClasse())
							|| methodRefactored.getInvolvedClassesAfter().contains(methodBuscar.getNomeClasse());

					boolean isMethodRefactored = methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
							|| methodRefactored.getRightSide().contains(methodBuscar.getNomeMetodo());

					if (isClassInvolved && isMethodRefactored) {
						if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
								&& methodRefactored.getLeftSide().contains(methodBuscar.getNomeMetodo())
								&& methodRefactored.getInvolvedClassesBefore().contains(methodBuscar.getNomeClasse())) {

							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
								renamedMethod = true;
								dateCommitRenamed = methodRefactored.getCommitDate();
								methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());
								if (countParameters(methodRefactored.getRightSide()) < countParameters(
										methodRefactored.getLeftSide())) {
									FilterSmellResult smellsPreviousCommit = obterSmellsPreviousCommit(repo,
											methodRefactored.getCommit());
									for (MethodDataSmelly methodNotSmell : smellsPreviousCommit.getMetodosNotSmelly()) {
										boolean isSameClassMethod = methodNotSmell.getNomeClasse()
												.equals(methodBuscar.getNomeClasse())
												&& methodNotSmell.getNomeMetodo().equals(methodBuscar.getNomeMetodo());
										if (isSameClassMethod) {
											falseNegative++;
											refactoredMethod = true;
											pmResultSmellRefactoredMessage.write(methodRefactored.getNomeClasse(),
													methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
													methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
													methodNotSmell.getEfferent(),
													methodNotSmell.getNumberOfParameters(),
													methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
													methodRefactored.getRefactoringType(),
													methodRefactored.getLeftSide(), methodRefactored.getRightSide(),
													methodRefactored.getFullMessage());

											pmResultSmellRefactored.write(methodRefactored.getNomeClasse(),
													methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
													methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
													methodNotSmell.getEfferent(),
													methodNotSmell.getNumberOfParameters(),
													methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
													methodRefactored.getRefactoringType(),
													methodRefactored.getLeftSide(), methodRefactored.getRightSide());

											break;
										}
									}
								}
							}
						}
					}
				}
				if (renamedMethod)
					methodBuscar.setNomeMetodo(methodRenamedName);
				else
					dateCommitRenamed = null;
			} while (renamedMethod && !refactoredMethod);
			if (!refactoredMethod) {
				trueNegative++;
				pmResultSmellRefactoredMessage.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "", "");

				pmResultSmellRefactored.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "");
			}
		}

		float truePositiveA = 0;
		float truePositiveV = 0;
		float truePositiveX = 0;
		float truePositiveR = 0;
		float truePositiveD = 0;

		float falsePositiveA = 0;
		float falsePositiveV = 0;
		float falsePositiveX = 0;
		float falsePositiveR = 0;
		float falsePositiveD = 0;

		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {

			if (methodSmelly.getSmell().equals(typeSmell)) {
				MethodDataSmelly methodSmellyBuscar = methodSmelly;
				boolean renamedMethod = false;
				boolean refactoredMethodA = false;
				boolean refactoredMethodV = false;
				boolean refactoredMethodX = false;
				boolean refactoredMethodR = false;
				boolean refactoredMethodD = false;
				Date dateCommitRenamed = null;
				do {
					renamedMethod = false;
					String methodRenamedName = null;
					for (RefactoringData methodRefactored : listRefactoring) {
						boolean isClassInvolved = methodRefactored.getInvolvedClassesBefore()
								.contains(methodSmellyBuscar.getNomeClasse())
								|| methodRefactored.getInvolvedClassesAfter()
										.contains(methodSmellyBuscar.getNomeClasse());
						boolean isMethodRefactored = methodRefactored.getLeftSide()
								.contains(methodSmellyBuscar.getNomeMetodo())
								|| methodRefactored.getRightSide().contains(methodSmellyBuscar.getNomeMetodo());

						if (isClassInvolved && isMethodRefactored) {
							if (methodRefactored.getRefactoringType().equals("RENAME_METHOD")
									&& methodRefactored.getLeftSide().contains(methodSmellyBuscar.getNomeMetodo())
									&& methodRefactored.getInvolvedClassesBefore()
											.contains(methodSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(methodRefactored.getCommitDate()) < 0)) {
									renamedMethod = true;
									dateCommitRenamed = methodRefactored.getCommitDate();
									methodRenamedName = extrairNomeMetodo(methodRefactored.getRightSide());

									if (countParameters(methodRefactored.getRightSide()) < countParameters(
											methodRefactored.getLeftSide())) {
										FilterSmellResult smellsPreviousCommit = obterSmellsPreviousCommit(repo,
												methodRefactored.getCommit());
										for (MethodDataSmelly methodSmell : smellsPreviousCommit.getMetodosSmell()) {
											boolean isSameClassMethod = methodSmell.getNomeClasse()
													.equals(methodSmellyBuscar.getNomeClasse())
													&& methodSmell.getNomeMetodo()
															.equals(methodSmellyBuscar.getNomeMetodo());
											if ((isSameClassMethod) && methodSmell.getSmell().equals(typeSmell)) {
												if (!refactoredMethodA
														&& methodSmellyBuscar.getListaTecnicas().contains("A")) {
													truePositiveA++;
													refactoredMethodA = true;
												}
												if (!refactoredMethodV
														&& methodSmellyBuscar.getListaTecnicas().contains("V")) {
													truePositiveV++;
													refactoredMethodV = true;
												}
												if (!refactoredMethodX
														&& methodSmellyBuscar.getListaTecnicas().contains("X")) {
													truePositiveX++;
													refactoredMethodX = true;
												}
												if (!refactoredMethodR
														&& methodSmellyBuscar.getListaTecnicas().contains("R")) {
													truePositiveR++;
													refactoredMethodR = true;
												}
												if (!refactoredMethodD
														&& methodSmellyBuscar.getListaTecnicas().contains("D")) {
													truePositiveD++;
													refactoredMethodD = true;
												}
												pmResultSmellRefactoredMessage.write(methodRefactored.getNomeClasse(),
														methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
														methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
														methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
														methodRefactored.getListaTecnicas(),
														methodRefactored.getCommit(),
														methodRefactored.getRefactoringType(),
														methodRefactored.getLeftSide(), methodRefactored.getRightSide(),
														methodRefactored.getFullMessage());
												pmResultSmellRefactored.write(methodRefactored.getNomeClasse(),
														methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
														methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
														methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
														methodRefactored.getListaTecnicas(),
														methodRefactored.getCommit(),
														methodRefactored.getRefactoringType(),
														methodRefactored.getLeftSide(),
														methodRefactored.getRightSide());
											}
										}
									}
								}
							}
						}
					}
					if (renamedMethod)
						methodSmellyBuscar.setNomeMetodo(methodRenamedName);
					else
						dateCommitRenamed = null;
				} while (renamedMethod
						&& (!refactoredMethodA || !refactoredMethodD || !refactoredMethodR || !refactoredMethodV || !refactoredMethodX));
				if (!refactoredMethodA && methodSmellyBuscar.getListaTecnicas().contains("A"))
					falsePositiveA++;
				if (!refactoredMethodD && methodSmellyBuscar.getListaTecnicas().contains("D"))
					falsePositiveD++;
				if (!refactoredMethodV && methodSmellyBuscar.getListaTecnicas().contains("V"))
					falsePositiveV++;
				if (!refactoredMethodR && methodSmellyBuscar.getListaTecnicas().contains("R"))
					falsePositiveR++;
				if (!refactoredMethodX && methodSmellyBuscar.getListaTecnicas().contains("X"))
					falsePositiveX++;
				if (!refactoredMethodA || !refactoredMethodD || !refactoredMethodR || !refactoredMethodV || !refactoredMethodX) {
					pmResultSmellRefactoredMessage.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");

					pmResultSmellRefactored.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		float precisionA = (truePositiveA + falsePositiveA) != 0 ? truePositiveA / (truePositiveA + falsePositiveA) : 0;
		float precisionV = (truePositiveV + falsePositiveV) != 0 ? truePositiveV / (truePositiveV + falsePositiveV) : 0;
		float precisionX = (truePositiveX + falsePositiveX) != 0 ? truePositiveX / (truePositiveX + falsePositiveX) : 0;
		float precisionR = (truePositiveR + falsePositiveR) != 0 ? truePositiveR / (truePositiveR + falsePositiveR) : 0;
		float precisionD = (truePositiveD + falsePositiveD) != 0 ? truePositiveD / (truePositiveD + falsePositiveD) : 0;

		float recallA = (truePositiveA + falseNegative) != 0 ? truePositiveA / (truePositiveA + falseNegative) : 0;
		float recallV = (truePositiveV + falseNegative) != 0 ? truePositiveV / (truePositiveV + falseNegative) : 0;
		float recallX = (truePositiveX + falseNegative) != 0 ? truePositiveX / (truePositiveX + falseNegative) : 0;
		float recallR = (truePositiveR + falseNegative) != 0 ? truePositiveR / (truePositiveR + falseNegative) : 0;
		float recallD = (truePositiveD + falseNegative) != 0 ? truePositiveD / (truePositiveD + falseNegative) : 0;

		float fMeasureA = (precisionA + recallA) != 0 ? (2 * precisionA * recallA) / (precisionA + recallA) : 0;
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;

		float accuracyA = (falsePositiveA + truePositiveA + falseNegative + trueNegative) != 0
				? (truePositiveA + trueNegative) / (falsePositiveA + truePositiveA + falseNegative + trueNegative)
				: 0;
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

		pmResultEvaluation.write(typeSmell.toUpperCase());
		pmResultEvaluation.write("True Negative", trueNegative);
		pmResultEvaluation.write("False Negative = ", falseNegative);
		pmResultEvaluation.write("False Positive (A) = ", falsePositiveA);
		pmResultEvaluation.write("False Positive (V) = ", falsePositiveV);
		pmResultEvaluation.write("False Positive (X) = ", falsePositiveX);
		pmResultEvaluation.write("False Positive (R) = ", falsePositiveR);
		pmResultEvaluation.write("False Positive (D) = ", falsePositiveD);
		pmResultEvaluation.write("True Positive  (A) = ", truePositiveA);
		pmResultEvaluation.write("True Positive  (V) = ", truePositiveV);
		pmResultEvaluation.write("True Positive (X) = ", truePositiveX);
		pmResultEvaluation.write("True Positive (R) = ", truePositiveR);
		pmResultEvaluation.write("True Positive (D) = ", truePositiveD);
		pmResultEvaluation.write("Precision (A) = ", precisionA);
		pmResultEvaluation.write("Precision (V) = ", precisionV);
		pmResultEvaluation.write("Precision (X) = ", precisionX);
		pmResultEvaluation.write("Precision (R) = ", precisionR);
		pmResultEvaluation.write("Precision (D) = ", precisionD);
		pmResultEvaluation.write("Recall (A) = ", recallA);
		pmResultEvaluation.write("Recall (V) = ", recallV);
		pmResultEvaluation.write("Recall (X) = ", recallX);
		pmResultEvaluation.write("Recall (R) = ", recallR);
		pmResultEvaluation.write("Recall (D) = ", recallD);
		pmResultEvaluation.write("F-measure (A) = ", fMeasureA);
		pmResultEvaluation.write("F-measure (V) = ", fMeasureV);
		pmResultEvaluation.write("F-measure (X) = ", fMeasureX);
		pmResultEvaluation.write("F-measure (R) = ", fMeasureR);
		pmResultEvaluation.write("F-measure (D) = ", fMeasureD);
		pmResultEvaluation.write("Acuracia (A) = ", accuracyA);
		pmResultEvaluation.write("Acuracia (V) = ", accuracyV);
		pmResultEvaluation.write("Acuracia (X) = ", accuracyX);
		pmResultEvaluation.write("Acuracia (R) = ", accuracyR);
		pmResultEvaluation.write("Acuracia (D) = ", accuracyD);

		pmResultEvaluation.write("");
	}

	private int countParameters(String metodo) {
		int countParams = 0;
		if (metodo.contains(",")) {
			countParams = 1;
			for (int i = 0; i < metodo.length(); i++) {
				if (metodo.charAt(i) == ',')
					countParams++;
			}
		} else {
			int posPrimeiroParenteses = metodo.indexOf("(");
			int posUltimoParenteses = metodo.indexOf(")");
			for(int i = posPrimeiroParenteses+1; i < posUltimoParenteses-1; i++) {
				if (metodo.charAt(i) != ' ' && metodo.charAt(i) !=  ')') {
					countParams++;
					break;
				}
			}
		}
		return countParams;
	}

	private FilterSmellResult obterSmellsPreviousCommit(Repository repo, String commitId) throws Exception {
		CommitData previousCommit = null;
		boolean achouCommit = false;
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			if (commit.getId().equals(commitId)) {
				previousCommit = commit.getPrevious();
				achouCommit = true;
				break;
			}
		}
		FilterSmellResult smellsPreviousCommit;
		if (achouCommit)
			smellsPreviousCommit = obterSmellsCommit(previousCommit.getId(), repo);
		else
			smellsPreviousCommit = obterSmellsCommit(commitId, repo);
		return smellsPreviousCommit;
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
