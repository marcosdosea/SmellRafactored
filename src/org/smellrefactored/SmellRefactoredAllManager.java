package org.smellrefactored;

import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.contextsmell.FilterSmellResult;
import org.contextsmell.FilterSmells;
import org.contextsmell.LimiarTecnica;
import org.contextsmell.MethodDataSmelly;
import org.designroleminer.MetricReport;
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
import org.threshold.TechniqueExecutor;

import com.opencsv.CSVReader;

/**
 * Identifies all commits that have refactorings and checks, based on the
 * previous commit, which refactorings were performed in smelly methods.
 * 
 * @author Marcos Dósea
 *
 */
public class SmellRefactoredAllManager {

	static Logger logger = LoggerFactory.getLogger(SmellRefactoredAllManager.class);

	private String urlRepository;
	private String localFolder;
	private String initialCommit;
	private String finalCommit;
	private List<LimiarTecnica> listaLimiarTecnica;
	private GitService gitService;
	private Repository repo;

	private String resultFileName;
	PersistenceMechanism pmResultEvaluation;
	PersistenceMechanism pmResultSmellRefactoredMethods;
	PersistenceMechanism pmResultSmellRefactoredMethodsMessage;
	PersistenceMechanism pmResultRefactoredMessage;

	PersistenceMechanism pmResultSmellRefactoredCommit;
	HashSet<String> listCommitEvaluated = new HashSet<String>();

	public SmellRefactoredAllManager(String urlRepository, String localFolder, String initialCommit, String finalCommit,
			List<LimiarTecnica> listaLimiarTecnica, String resultFileName) {
		this.urlRepository = urlRepository;
		this.localFolder = localFolder;
		this.initialCommit = initialCommit;
		this.finalCommit = finalCommit;
		this.listaLimiarTecnica = listaLimiarTecnica;
		this.resultFileName = resultFileName;
		gitService = new GitServiceImpl();
		pmResultEvaluation = new CSVFile(resultFileName + "-evaluations.csv", false);
		pmResultSmellRefactoredMethods = new CSVFile(resultFileName + "-smellRefactored.csv", false);
		pmResultSmellRefactoredMethodsMessage = new CSVFile(resultFileName + "-smellRefactored-message.csv", false);
		pmResultRefactoredMessage = new CSVFile(resultFileName + "-refactored-message.csv", false);
		pmResultSmellRefactoredCommit = new CSVFile(resultFileName + "-smellRefactored-commit.csv", false);
	}

	public void getSmellRefactoredMethods() {

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
			logger.info("Total de refactorings encontrados: " + listRefactoring.size());

			// Filtra os commits com refactoring cujo commit feito no master ou pull request
			// realizados no master
			ArrayList<CommitData> commitsMergedIntoMaster = new ArrayList<CommitData>();
			ArrayList<CommitData> commitsNotMergedIntoMaster = new ArrayList<CommitData>();
			Git git = new Git(repo);
			gitService.checkout(repo, finalCommit);

			Iterable<RevCommit> log = git.log().call();
			Iterator<RevCommit> logIterator = log.iterator();
			RevWalk revWalk = new RevWalk(repo, 3);
			RevCommit masterHead = revWalk.parseCommit(repo.resolve("HEAD"));
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

			ArrayList<RefactoringData> listRefactoringRelatedOperation = new ArrayList<RefactoringData>();
			ArrayList<RefactoringData> listRefactoringRelatedRenaming = new ArrayList<RefactoringData>();

			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			HashMap<String, CommitData> listCommitAnalisados = new HashMap<String, CommitData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						// if (refactoring.getRefactoringType().contains("OPERATION") &&
						// (refactoring.getRefactoringType().contains("EXTRACT")||refactoring.getRefactoringType().contains("MOVE")))
						if (refactoring.getRefactoringType().contains("OPERATION")
								&& refactoring.getRefactoringType().contains("EXTRACT")) {
							listRefactoringRelatedOperation.add(refactoring);
							listCommitAnalisados.put(commit.getId(), commit);
						}
						if (refactoring.getRefactoringType().equals("RENAME_METHOD")) {
							listRefactoringRelatedRenaming.add(refactoring);
							listCommitAnalisados.put(commit.getId(), commit);
						}
					}
				}
			}
			Collections.sort(listRefactoringMergedIntoMaster);
			pmResultEvaluation.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluation.write("Numero Refatoracoes em Metodos e Nao Metodos:",
					listRefactoringMergedIntoMaster.size());
			pmResultEvaluation.write("Numero Refatoracoes relacionadas a operacoes em Metodos:",
					listRefactoringRelatedOperation.size());
			pmResultEvaluation.write("Numero Refatoracoes relacionadas a rename em Metodos:",
					listRefactoringRelatedRenaming.size());

			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");

			pmResultSmellRefactoredMethodsMessage.write("Class", "Method", "Smell", "DR", "LOC", "CC", "EC", "NOP",
					"Tecnicas", "Commit", "Refactoring", "Left Side", "Right Side", "Short Message", "Full Message");
			pmResultSmellRefactoredMethods.write("Class", "Method", "Smell", "DR", "LOC", "CC", "EC", "NOP", "Tecnicas",
					"Commit", "Refactoring", "Left Side", "Right Side");
			pmResultRefactoredMessage.write("Class", "Method", "Smell", "DR", "LOC", "CC", "EC", "NOP", "Tecnicas",
					"Commit", "Refactoring", "Left Side", "Right Side", "Short Message", "Full Message");

			evaluateSmellChangeOperation(commitsWithRefactoringMergedIntoMaster, listRefactoringRelatedOperation, repo,
					MethodDataSmelly.LONG_METHOD);
			evaluateSmellChangeOperation(commitsWithRefactoringMergedIntoMaster, listRefactoringRelatedOperation, repo,
					MethodDataSmelly.COMPLEX_METHOD);
			evaluateSmellChangeOperation(commitsWithRefactoringMergedIntoMaster, listRefactoringRelatedOperation, repo,
					MethodDataSmelly.HIGH_EFFERENT_COUPLING);
			evaluateSmellChangeParameters(commitsWithRefactoringMergedIntoMaster, listRefactoringRelatedRenaming, repo,
					MethodDataSmelly.MANY_PARAMETERS);
			pmResultEvaluation.write("DADOS COMMITS");
			pmResultEvaluation.write("ID", "Data", "Short Message", "Full Message");
			for (CommitData commit : listCommitAnalisados.values()) {
				pmResultEvaluation.write(commit.getId(), commit.getDate(), commit.getShortMessage(),
						commit.getFullMessage());
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void evaluateSmellChangeOperation(ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster,
			ArrayList<RefactoringData> listRefactoring, Repository repo, String typeSmell) throws Exception {

		int truePositiveA = 0;
		int truePositiveV = 0;
		int truePositiveX = 0;
		int truePositiveR = 0;
		int truePositiveD = 0;

		int falseNegativeNonSmelly = 0;

		HashSet<String> setTruePositiveA = new HashSet<String>();
		HashSet<String> setTruePositiveV = new HashSet<String>();
		HashSet<String> setTruePositiveX = new HashSet<String>();
		HashSet<String> setTruePositiveR = new HashSet<String>();
		HashSet<String> setTruePositiveD = new HashSet<String>();

		HashSet<String> falsePositiveA = new HashSet<String>();
		HashSet<String> falsePositiveV = new HashSet<String>();
		HashSet<String> falsePositiveX = new HashSet<String>();
		HashSet<String> falsePositiveR = new HashSet<String>();
		HashSet<String> falsePositiveD = new HashSet<String>();

		HashSet<String> falseNegativeA = new HashSet<String>();
		HashSet<String> falseNegativeV = new HashSet<String>();
		HashSet<String> falseNegativeX = new HashSet<String>();
		HashSet<String> falseNegativeR = new HashSet<String>();
		HashSet<String> falseNegativeD = new HashSet<String>();

		HashSet<String> trueNegativeA = new HashSet<String>();
		HashSet<String> trueNegativeV = new HashSet<String>();
		HashSet<String> trueNegativeX = new HashSet<String>();
		HashSet<String> trueNegativeR = new HashSet<String>();
		HashSet<String> trueNegativeD = new HashSet<String>();

		// 1) Looking for true positives and false negatives
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			boolean hasRafactoringRelatedToSmell = false;
			for (RefactoringData refactoring : listRefactoring) {
				if (commit.getId().equals(refactoring.getCommit())) {
					hasRafactoringRelatedToSmell = true;
					break;
				}
			}
			if (hasRafactoringRelatedToSmell) {
				FilterSmellResult methodsCommit = obterSmellsPreviousCommit(repo, commit);
				for (MethodDataSmelly methodSmelly : methodsCommit.getMetodosSmell()) {
					for (RefactoringData refactoring : listRefactoring) {

						boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
								.contains(methodSmelly.getNomeClasse())
								|| refactoring.getInvolvedClassesAfter().contains(methodSmelly.getNomeClasse());

						boolean isMethodInvolved = refactoring.getLeftSide().contains(methodSmelly.getNomeMetodo())
								|| refactoring.getRightSide().contains(methodSmelly.getNomeMetodo());

						boolean isClassMethodCommitSmellInvolved = isClassInvolved && isMethodInvolved
								&& refactoring.getCommit().equals(commit.getId())
								&& methodSmelly.getSmell().equals(typeSmell);
						if (isClassMethodCommitSmellInvolved) {
							if (methodSmelly.getListaTecnicas().contains("A")) {
								setTruePositiveA
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveA++;
							} else
								falseNegativeA.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("V")) {
								setTruePositiveV
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveV++;
							} else
								falseNegativeV.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("X")) {
								setTruePositiveX
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveX++;
							} else
								falseNegativeX.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("R")) {
								setTruePositiveR
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveR++;
							} else
								falseNegativeR.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("D")) {
								setTruePositiveD
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveD++;
							} else
								falseNegativeD.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
//							pmResultSmellRefactoredMethodsMessage.write(methodSmelly.getNomeClasse(),
//									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
//									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
//									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
//									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
//									refactoring.getCommit(), refactoring.getRefactoringType(),
//									refactoring.getLeftSide(), refactoring.getRightSide(),
//									refactoring.getShortMessage(), refactoring.getFullMessage());

							pmResultSmellRefactoredMethods.write(methodSmelly.getNomeClasse(),
									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
									refactoring.getCommit(), refactoring.getRefactoringType(),
									refactoring.getLeftSide(), refactoring.getRightSide());

							pmResultRefactoredMessage.write(methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(),
									methodSmelly.getSmell(), methodSmelly.getClassDesignRole(),
									methodSmelly.getLinesOfCode(), methodSmelly.getComplexity(),
									methodSmelly.getEfferent(), methodSmelly.getNumberOfParameters(),
									methodSmelly.getListaTecnicas(), refactoring.getCommit(),
									refactoring.getRefactoringType(), refactoring.getLeftSide(),
									refactoring.getRightSide(), refactoring.getShortMessage(),
									refactoring.getFullMessage());
							// break;
						}
					}
				}

				for (MethodDataSmelly methodNotSmelly : methodsCommit.getMetodosNotSmelly()) {
					for (RefactoringData refactoring : listRefactoring) {

						boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
								.contains(methodNotSmelly.getNomeClasse())
								|| refactoring.getInvolvedClassesAfter().contains(methodNotSmelly.getNomeClasse());

						boolean isMethodInvolved = refactoring.getLeftSide().contains(methodNotSmelly.getNomeMetodo())
								|| refactoring.getRightSide().contains(methodNotSmelly.getNomeMetodo());

						boolean isClassMethodCommitSmellInvolved = isClassInvolved && isMethodInvolved
								&& refactoring.getCommit().equals(commit.getId()) && countParameters(
										refactoring.getRightSide()) < countParameters(refactoring.getLeftSide());
						;
						if (isClassMethodCommitSmellInvolved) {
							falseNegativeNonSmelly++;
							falseNegativeA.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeX.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeV.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeR.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeD.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());

							// imprime as refatorações associadas a métodos apenas para long methods para
							// não repetir
							if (typeSmell.equals(MethodDataSmelly.LONG_METHOD)) {
								pmResultRefactoredMessage.write(methodNotSmelly.getNomeClasse(),
										methodNotSmelly.getNomeMetodo(), "", methodNotSmelly.getClassDesignRole(),
										methodNotSmelly.getLinesOfCode(), methodNotSmelly.getComplexity(),
										methodNotSmelly.getEfferent(), methodNotSmelly.getNumberOfParameters(), "",
										refactoring.getCommit(), refactoring.getRefactoringType(),
										refactoring.getLeftSide(), refactoring.getRightSide(),
										refactoring.getShortMessage(), refactoring.getFullMessage());
							}
						}
					}
				}

			}
		}

		// 2) Looking for false positives and true negatives
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			boolean hasRafactoringRelatedToSmell = false;
			for (RefactoringData refactoring : listRefactoring) {
				if (commit.getId().equals(refactoring.getCommit())) {
					hasRafactoringRelatedToSmell = true;
					break;
				}
			}
			if (hasRafactoringRelatedToSmell) {
				FilterSmellResult methodsCommit = obterSmellsPreviousCommit(repo, commit);
				for (MethodDataSmelly methodSmelly : methodsCommit.getMetodosSmell()) {
					if (methodSmelly.getSmell().equals(typeSmell)) {
						boolean hasFalsePositive = false;
						if (methodSmelly.getListaTecnicas().contains("A") && !setTruePositiveA
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveA.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						} else {
							trueNegativeA.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
						}
						if (methodSmelly.getListaTecnicas().contains("V") && !setTruePositiveV
								.contains(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveV.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						} else {
							trueNegativeV.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
						}
						if (methodSmelly.getListaTecnicas().contains("X") && !setTruePositiveX
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveX.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						} else {
							trueNegativeX.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
						}
						if (methodSmelly.getListaTecnicas().contains("R") && !setTruePositiveR
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveR.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						} else {
							trueNegativeR.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
						}
						if (methodSmelly.getListaTecnicas().contains("D") && !setTruePositiveD
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveD.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						} else {
							trueNegativeD.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
						}

						if (hasFalsePositive) {
							pmResultSmellRefactoredMethodsMessage.write(methodSmelly.getNomeClasse(),
									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
									methodSmelly.getCommit(), "", "", "", "", "");

							pmResultSmellRefactoredMethods.write(methodSmelly.getNomeClasse(),
									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
									methodSmelly.getCommit(), "", "", "");
						}
					}
				}

				for (MethodDataSmelly methodNotSmelly : methodsCommit.getMetodosNotSmelly()) {
					boolean hasTrueNegative = false;
					if (!falseNegativeA.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeA.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeV.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeV.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeX.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeX.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeR.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeR.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeD.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeD.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (hasTrueNegative) {
						pmResultSmellRefactoredMethodsMessage.write(methodNotSmelly.getNomeClasse(),
								methodNotSmelly.getNomeMetodo(), "", methodNotSmelly.getClassDesignRole(),
								methodNotSmelly.getLinesOfCode(), methodNotSmelly.getComplexity(),
								methodNotSmelly.getEfferent(), methodNotSmelly.getNumberOfParameters(), "",
								methodNotSmelly.getCommit(), "", "", "", "", "");

						pmResultSmellRefactoredMethods.write(methodNotSmelly.getNomeClasse(),
								methodNotSmelly.getNomeMetodo(), "", methodNotSmelly.getClassDesignRole(),
								methodNotSmelly.getLinesOfCode(), methodNotSmelly.getComplexity(),
								methodNotSmelly.getEfferent(), methodNotSmelly.getNumberOfParameters(), "",
								methodNotSmelly.getCommit(), "", "", "");
					}
				}

			}
		}

		float precisionA = (truePositiveA + falsePositiveA.size()) != 0
				? (float) truePositiveA / (truePositiveA + falsePositiveA.size())
				: 0;
		float precisionV = (truePositiveV + falsePositiveV.size()) != 0
				? (float) truePositiveV / (truePositiveV + falsePositiveV.size())
				: 0;
		float precisionX = (truePositiveX + falsePositiveX.size()) != 0
				? (float) truePositiveX / (truePositiveX + falsePositiveX.size())
				: 0;
		float precisionR = (truePositiveR + falsePositiveR.size()) != 0
				? (float) truePositiveR / (truePositiveR + falsePositiveR.size())
				: 0;
		float precisionD = (truePositiveD + falsePositiveD.size()) != 0
				? (float) truePositiveD / (truePositiveD + falsePositiveD.size())
				: 0;

		float recallA = (truePositiveA + falseNegativeA.size()) != 0
				? (float) truePositiveA / (truePositiveA + falseNegativeA.size())
				: 0;
		float recallV = (truePositiveV + falseNegativeV.size()) != 0
				? (float) truePositiveV / (truePositiveV + falseNegativeV.size())
				: 0;
		float recallX = (truePositiveX + falseNegativeX.size()) != 0
				? (float) truePositiveX / (truePositiveX + falseNegativeX.size())
				: 0;
		float recallR = (truePositiveR + falseNegativeR.size()) != 0
				? (float) truePositiveR / (truePositiveR + falseNegativeR.size())
				: 0;
		float recallD = (truePositiveD + falseNegativeD.size()) != 0
				? (float) truePositiveD / (truePositiveD + falseNegativeD.size())
				: 0;

		float fMeasureA = (precisionA + recallA) != 0 ? (2 * precisionA * recallA) / (precisionA + recallA) : 0;
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;

		float accuracyA = (falsePositiveA.size() + truePositiveA + falseNegativeA.size() + trueNegativeA.size()) != 0
				? (float) (truePositiveA + trueNegativeA.size())
						/ (falsePositiveA.size() + truePositiveA + falseNegativeA.size() + trueNegativeA.size())
				: 0;
		float accuracyV = (falsePositiveV.size() + truePositiveV + falseNegativeV.size() + trueNegativeV.size()) != 0
				? (float) (truePositiveV + trueNegativeV.size())
						/ (falsePositiveV.size() + truePositiveV + falseNegativeV.size() + trueNegativeV.size())
				: 0;
		float accuracyX = (falsePositiveX.size() + truePositiveX + falseNegativeX.size() + trueNegativeX.size()) != 0
				? (float) (truePositiveX + trueNegativeX.size())
						/ (falsePositiveX.size() + truePositiveX + falseNegativeX.size() + trueNegativeX.size())
				: 0;
		float accuracyR = (falsePositiveR.size() + truePositiveR + falseNegativeR.size() + trueNegativeR.size()) != 0
				? (float) (truePositiveR + trueNegativeR.size())
						/ (falsePositiveR.size() + truePositiveR + falseNegativeR.size() + trueNegativeR.size())
				: 0;
		float accuracyD = (falsePositiveD.size() + truePositiveD + falseNegativeD.size() + trueNegativeD.size()) != 0
				? (float) (truePositiveD + trueNegativeD.size())
						/ (falsePositiveD.size() + truePositiveD + falseNegativeD.size() + trueNegativeD.size())
				: 0;

		pmResultEvaluation.write(typeSmell.toUpperCase());
		pmResultEvaluation.write("True Negative (A) = ", trueNegativeA.size());
		pmResultEvaluation.write("True Negative (V) = ", trueNegativeV.size());
		pmResultEvaluation.write("True Negative (X) = ", trueNegativeX.size());
		pmResultEvaluation.write("True Negative (R) = ", trueNegativeR.size());
		pmResultEvaluation.write("True Negative (D) = ", trueNegativeD.size());

		pmResultEvaluation.write("False Negative (Non-Smelly) = ", falseNegativeNonSmelly);
		pmResultEvaluation.write("False Negative (A) = ", falseNegativeA.size());
		pmResultEvaluation.write("False Negative (V) = ", falseNegativeV.size());
		pmResultEvaluation.write("False Negative (X) = ", falseNegativeX.size());
		pmResultEvaluation.write("False Negative (R) = ", falseNegativeR.size());
		pmResultEvaluation.write("False Negative (D) = ", falseNegativeD.size());

		pmResultEvaluation.write("False Positive (A) = ", falsePositiveA.size());
		pmResultEvaluation.write("False Positive (V) = ", falsePositiveV.size());
		pmResultEvaluation.write("False Positive (X) = ", falsePositiveX.size());
		pmResultEvaluation.write("False Positive (R) = ", falsePositiveR.size());
		pmResultEvaluation.write("False Positive (D) = ", falsePositiveD.size());

		pmResultEvaluation.write("True Positive  (A) = ", truePositiveA);
		pmResultEvaluation.write("True Positive  (V) = ", truePositiveV);
		pmResultEvaluation.write("True Positive (X) = ", truePositiveX);
		pmResultEvaluation.write("True Positive (R) = ", truePositiveR);
		pmResultEvaluation.write("True Positive (D) = ", truePositiveD);

		DecimalFormat df = new DecimalFormat("#.000");
		pmResultEvaluation.write("Precision (A) = ", df.format(precisionA));
		pmResultEvaluation.write("Precision (V) = ", df.format(precisionV));
		pmResultEvaluation.write("Precision (X) = ", df.format(precisionX));
		pmResultEvaluation.write("Precision (R) = ", df.format(precisionR));
		pmResultEvaluation.write("Precision (D) = ", df.format(precisionD));
		pmResultEvaluation.write("Recall (A) = ", df.format(recallA));
		pmResultEvaluation.write("Recall (V) = ", df.format(recallV));
		pmResultEvaluation.write("Recall (X) = ", df.format(recallX));
		pmResultEvaluation.write("Recall (R) = ", df.format(recallR));
		pmResultEvaluation.write("Recall (D) = ", df.format(recallD));
		pmResultEvaluation.write("F-measure (A) = ", df.format(fMeasureA));
		pmResultEvaluation.write("F-measure (V) = ", df.format(fMeasureV));
		pmResultEvaluation.write("F-measure (X) = ", df.format(fMeasureX));
		pmResultEvaluation.write("F-measure (R) = ", df.format(fMeasureR));
		pmResultEvaluation.write("F-measure (D) = ", df.format(fMeasureD));
		pmResultEvaluation.write("Acuracia (A) = ", df.format(accuracyA));
		pmResultEvaluation.write("Acuracia (V) = ", df.format(accuracyV));
		pmResultEvaluation.write("Acuracia (X) = ", df.format(accuracyX));
		pmResultEvaluation.write("Acuracia (R) = ", df.format(accuracyR));
		pmResultEvaluation.write("Acuracia (D) = ", df.format(accuracyD));

		pmResultEvaluation.write("");
	}

	private void evaluateSmellChangeParameters(ArrayList<CommitData> commitsWithRefactoringMergedIntoMaster,
			ArrayList<RefactoringData> listRefactoring, Repository repo, String typeSmell) throws Exception {

		int truePositiveA = 0;
		int truePositiveV = 0;
		int truePositiveX = 0;
		int truePositiveR = 0;
		int truePositiveD = 0;

		int falseNegativeNonSmelly = 0;

		HashSet<String> setTruePositiveA = new HashSet<String>();
		HashSet<String> setTruePositiveV = new HashSet<String>();
		HashSet<String> setTruePositiveX = new HashSet<String>();
		HashSet<String> setTruePositiveR = new HashSet<String>();
		HashSet<String> setTruePositiveD = new HashSet<String>();

		HashSet<String> falsePositiveA = new HashSet<String>();
		HashSet<String> falsePositiveV = new HashSet<String>();
		HashSet<String> falsePositiveX = new HashSet<String>();
		HashSet<String> falsePositiveR = new HashSet<String>();
		HashSet<String> falsePositiveD = new HashSet<String>();

		HashSet<String> falseNegativeA = new HashSet<String>();
		HashSet<String> falseNegativeV = new HashSet<String>();
		HashSet<String> falseNegativeX = new HashSet<String>();
		HashSet<String> falseNegativeR = new HashSet<String>();
		HashSet<String> falseNegativeD = new HashSet<String>();

		HashSet<String> trueNegativeA = new HashSet<String>();
		HashSet<String> trueNegativeV = new HashSet<String>();
		HashSet<String> trueNegativeX = new HashSet<String>();
		HashSet<String> trueNegativeR = new HashSet<String>();
		HashSet<String> trueNegativeD = new HashSet<String>();

		// 1) Looking for true positives and false negatives
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			boolean hasRafactoringRelatedToSmell = false;
			for (RefactoringData refactoring : listRefactoring) {
				if (commit.getId().equals(refactoring.getCommit())) {
					hasRafactoringRelatedToSmell = true;
					break;
				}
			}
			if (hasRafactoringRelatedToSmell) {
				FilterSmellResult methodsCommit = obterSmellsPreviousCommit(repo, commit);
				for (MethodDataSmelly methodSmelly : methodsCommit.getMetodosSmell()) {
					for (RefactoringData refactoring : listRefactoring) {

						boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
								.contains(methodSmelly.getNomeClasse())
								|| refactoring.getInvolvedClassesAfter().contains(methodSmelly.getNomeClasse());

						boolean isMethodInvolved = refactoring.getLeftSide().contains(methodSmelly.getNomeMetodo())
								|| refactoring.getRightSide().contains(methodSmelly.getNomeMetodo());

						boolean isClassMethodCommitSmellInvolved = isClassInvolved && isMethodInvolved
								&& refactoring.getCommit().equals(commit.getId())
								&& methodSmelly.getSmell().equals(typeSmell) && countParameters(
										refactoring.getRightSide()) < countParameters(refactoring.getLeftSide());
						if (isClassMethodCommitSmellInvolved) {
							if (methodSmelly.getListaTecnicas().contains("A")) {
								setTruePositiveA
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveA++;
							} else
								falseNegativeA.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("V")) {
								setTruePositiveV
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveV++;
							} else
								falseNegativeV.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("X")) {
								setTruePositiveX
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveX++;
							} else
								falseNegativeX.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("R")) {
								setTruePositiveR
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveR++;
							} else
								falseNegativeR.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							if (methodSmelly.getListaTecnicas().contains("D")) {
								setTruePositiveD
										.add(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
								truePositiveD++;
							} else
								falseNegativeD.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
//							pmResultSmellRefactoredMethodsMessage.write(methodSmelly.getNomeClasse(),
//									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
//									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
//									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
//									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
//									refactoring.getCommit(), refactoring.getRefactoringType(),
//									refactoring.getLeftSide(), refactoring.getRightSide(),
//									refactoring.getShortMessage(), refactoring.getFullMessage());

							pmResultSmellRefactoredMethods.write(methodSmelly.getNomeClasse(),
									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
									refactoring.getCommit(), refactoring.getRefactoringType(),
									refactoring.getLeftSide(), refactoring.getRightSide());

							pmResultRefactoredMessage.write(methodSmelly.getNomeClasse(), methodSmelly.getNomeMetodo(),
									methodSmelly.getSmell(), methodSmelly.getClassDesignRole(),
									methodSmelly.getLinesOfCode(), methodSmelly.getComplexity(),
									methodSmelly.getEfferent(), methodSmelly.getNumberOfParameters(),
									methodSmelly.getListaTecnicas(), refactoring.getCommit(),
									refactoring.getRefactoringType(), refactoring.getLeftSide(),
									refactoring.getRightSide(), refactoring.getShortMessage(),
									refactoring.getFullMessage());
							// break;
						}
					}
				}
				for (MethodDataSmelly methodNotSmelly : methodsCommit.getMetodosNotSmelly()) {
					for (RefactoringData refactoring : listRefactoring) {

						boolean isClassInvolved = refactoring.getInvolvedClassesBefore()
								.contains(methodNotSmelly.getNomeClasse())
								|| refactoring.getInvolvedClassesAfter().contains(methodNotSmelly.getNomeClasse());

						boolean isMethodInvolved = refactoring.getLeftSide().contains(methodNotSmelly.getNomeMetodo())
								|| refactoring.getRightSide().contains(methodNotSmelly.getNomeMetodo());

						boolean isClassMethodCommitSmellInvolved = isClassInvolved && isMethodInvolved
								&& refactoring.getCommit().equals(commit.getId()) && countParameters(
										refactoring.getRightSide()) < countParameters(refactoring.getLeftSide());
						if (isClassMethodCommitSmellInvolved) {
							falseNegativeNonSmelly++;
							falseNegativeA.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeX.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeV.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeR.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
							falseNegativeD.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());

							pmResultRefactoredMessage.write(methodNotSmelly.getNomeClasse(),
									methodNotSmelly.getNomeMetodo(), "", methodNotSmelly.getClassDesignRole(),
									methodNotSmelly.getLinesOfCode(), methodNotSmelly.getComplexity(),
									methodNotSmelly.getEfferent(), methodNotSmelly.getNumberOfParameters(), "",
									refactoring.getCommit(), refactoring.getRefactoringType(),
									refactoring.getLeftSide(), refactoring.getRightSide(),
									refactoring.getShortMessage(), refactoring.getFullMessage());
						}
					}
				}
			}
		}

		// 2) Looking for false positives and true negatives
		for (CommitData commit : commitsWithRefactoringMergedIntoMaster) {
			boolean hasRafactoringRelatedToSmell = false;
			for (RefactoringData refactoring : listRefactoring) {
				if (commit.getId().equals(refactoring.getCommit())) {
					hasRafactoringRelatedToSmell = true;
					break;
				}
			}
			if (hasRafactoringRelatedToSmell) {
				FilterSmellResult methodsCommit = obterSmellsPreviousCommit(repo, commit);
				for (MethodDataSmelly methodSmelly : methodsCommit.getMetodosSmell()) {
					if (methodSmelly.getSmell().equals(typeSmell)) {
						boolean hasFalsePositive = false;
						if (methodSmelly.getListaTecnicas().contains("A") && !setTruePositiveA
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveA.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						}
						if (methodSmelly.getListaTecnicas().contains("V") && !setTruePositiveV
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveV.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						}
						if (methodSmelly.getListaTecnicas().contains("X") && !setTruePositiveX
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveX.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						}
						if (methodSmelly.getListaTecnicas().contains("R") && !setTruePositiveR
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveR.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						}
						if (methodSmelly.getListaTecnicas().contains("D") && !setTruePositiveD
								.contains(commit + methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo())) {
							falsePositiveD.add(methodSmelly.getNomeClasse() + methodSmelly.getNomeMetodo());
							hasFalsePositive = true;
						}

						if (hasFalsePositive) {
							pmResultSmellRefactoredMethodsMessage.write(methodSmelly.getNomeClasse(),
									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
									methodSmelly.getCommit(), "", "", "", "", "");

							pmResultSmellRefactoredMethods.write(methodSmelly.getNomeClasse(),
									methodSmelly.getNomeMetodo(), methodSmelly.getSmell(),
									methodSmelly.getClassDesignRole(), methodSmelly.getLinesOfCode(),
									methodSmelly.getComplexity(), methodSmelly.getEfferent(),
									methodSmelly.getNumberOfParameters(), methodSmelly.getListaTecnicas(),
									methodSmelly.getCommit(), "", "", "");
						}
					}
				}

				for (MethodDataSmelly methodNotSmelly : methodsCommit.getMetodosNotSmelly()) {
					boolean hasTrueNegative = false;
					if (!falseNegativeA.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeA.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeV.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeV.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeX.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeX.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeR.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeR.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (!falseNegativeD.contains(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo())) {
						trueNegativeD.add(methodNotSmelly.getNomeClasse() + methodNotSmelly.getNomeMetodo());
						hasTrueNegative = true;
					}
					if (hasTrueNegative) {
						pmResultSmellRefactoredMethodsMessage.write(methodNotSmelly.getNomeClasse(),
								methodNotSmelly.getNomeMetodo(), "", methodNotSmelly.getClassDesignRole(),
								methodNotSmelly.getLinesOfCode(), methodNotSmelly.getComplexity(),
								methodNotSmelly.getEfferent(), methodNotSmelly.getNumberOfParameters(), "",
								methodNotSmelly.getCommit(), "", "", "", "", "");

						pmResultSmellRefactoredMethods.write(methodNotSmelly.getNomeClasse(),
								methodNotSmelly.getNomeMetodo(), "", methodNotSmelly.getClassDesignRole(),
								methodNotSmelly.getLinesOfCode(), methodNotSmelly.getComplexity(),
								methodNotSmelly.getEfferent(), methodNotSmelly.getNumberOfParameters(), "",
								methodNotSmelly.getCommit(), "", "", "");
					}
				}

			}
		}

		float precisionA = (truePositiveA + falsePositiveA.size()) != 0
				? (float) truePositiveA / (truePositiveA + falsePositiveA.size())
				: 0;
		float precisionV = (truePositiveV + falsePositiveV.size()) != 0
				? (float) truePositiveV / (truePositiveV + falsePositiveV.size())
				: 0;
		float precisionX = (truePositiveX + falsePositiveX.size()) != 0
				? (float) truePositiveX / (truePositiveX + falsePositiveX.size())
				: 0;
		float precisionR = (truePositiveR + falsePositiveR.size()) != 0
				? (float) truePositiveR / (truePositiveR + falsePositiveR.size())
				: 0;
		float precisionD = (truePositiveD + falsePositiveD.size()) != 0
				? (float) truePositiveD / (truePositiveD + falsePositiveD.size())
				: 0;

		float recallA = (truePositiveA + falseNegativeA.size()) != 0
				? (float) truePositiveA / (truePositiveA + falseNegativeA.size())
				: 0;
		float recallV = (truePositiveV + falseNegativeV.size()) != 0
				? (float) truePositiveV / (truePositiveV + falseNegativeV.size())
				: 0;
		float recallX = (truePositiveX + falseNegativeX.size()) != 0
				? (float) truePositiveX / (truePositiveX + falseNegativeX.size())
				: 0;
		float recallR = (truePositiveR + falseNegativeR.size()) != 0
				? (float) truePositiveR / (truePositiveR + falseNegativeR.size())
				: 0;
		float recallD = (truePositiveD + falseNegativeD.size()) != 0
				? (float) truePositiveD / (truePositiveD + falseNegativeD.size())
				: 0;

		float fMeasureA = (precisionA + recallA) != 0 ? (2 * precisionA * recallA) / (precisionA + recallA) : 0;
		float fMeasureV = (precisionV + recallV) != 0 ? (2 * precisionV * recallV) / (precisionV + recallV) : 0;
		float fMeasureX = (precisionX + recallX) != 0 ? (2 * precisionX * recallX) / (precisionX + recallX) : 0;
		float fMeasureR = (precisionR + recallR) != 0 ? (2 * precisionR * recallR) / (precisionR + recallR) : 0;
		float fMeasureD = (precisionD + recallD) != 0 ? (2 * precisionD * recallD) / (precisionD + recallD) : 0;

		float accuracyA = (falsePositiveA.size() + truePositiveA + falseNegativeA.size() + trueNegativeA.size()) != 0
				? (float) (truePositiveA + trueNegativeA.size())
						/ (falsePositiveA.size() + truePositiveA + falseNegativeA.size() + trueNegativeA.size())
				: 0;
		float accuracyV = (falsePositiveV.size() + truePositiveV + falseNegativeV.size() + trueNegativeV.size()) != 0
				? (float) (truePositiveV + trueNegativeV.size())
						/ (falsePositiveV.size() + truePositiveV + falseNegativeV.size() + trueNegativeV.size())
				: 0;
		float accuracyX = (falsePositiveX.size() + truePositiveX + falseNegativeX.size() + trueNegativeX.size()) != 0
				? (float) (truePositiveX + trueNegativeX.size())
						/ (falsePositiveX.size() + truePositiveX + falseNegativeX.size() + trueNegativeX.size())
				: 0;
		float accuracyR = (falsePositiveR.size() + truePositiveR + falseNegativeR.size() + trueNegativeR.size()) != 0
				? (float) (truePositiveR + trueNegativeR.size())
						/ (falsePositiveR.size() + truePositiveR + falseNegativeR.size() + trueNegativeR.size())
				: 0;
		float accuracyD = (falsePositiveD.size() + truePositiveD + falseNegativeD.size() + trueNegativeD.size()) != 0
				? (float) (truePositiveD + trueNegativeD.size())
						/ (falsePositiveD.size() + truePositiveD + falseNegativeD.size() + trueNegativeD.size())
				: 0;

		pmResultEvaluation.write(typeSmell.toUpperCase());
		pmResultEvaluation.write("True Negative (A) = ", trueNegativeA.size());
		pmResultEvaluation.write("True Negative (V) = ", trueNegativeV.size());
		pmResultEvaluation.write("True Negative (X) = ", trueNegativeX.size());
		pmResultEvaluation.write("True Negative (R) = ", trueNegativeR.size());
		pmResultEvaluation.write("True Negative (D) = ", trueNegativeD.size());

		pmResultEvaluation.write("False Negative (non-smelly): ", falseNegativeNonSmelly);
		pmResultEvaluation.write("False Negative (A) = ", falseNegativeA.size());
		pmResultEvaluation.write("False Negative (V) = ", falseNegativeV.size());
		pmResultEvaluation.write("False Negative (X) = ", falseNegativeX.size());
		pmResultEvaluation.write("False Negative (R) = ", falseNegativeR.size());
		pmResultEvaluation.write("False Negative (D) = ", falseNegativeD.size());

		pmResultEvaluation.write("False Positive (A) = ", falsePositiveA.size());
		pmResultEvaluation.write("False Positive (V) = ", falsePositiveV.size());
		pmResultEvaluation.write("False Positive (X) = ", falsePositiveX.size());
		pmResultEvaluation.write("False Positive (R) = ", falsePositiveR.size());
		pmResultEvaluation.write("False Positive (D) = ", falsePositiveD.size());
		pmResultEvaluation.write("True Positive  (A) = ", truePositiveA);
		pmResultEvaluation.write("True Positive  (V) = ", truePositiveV);
		pmResultEvaluation.write("True Positive (X) = ", truePositiveX);
		pmResultEvaluation.write("True Positive (R) = ", truePositiveR);
		pmResultEvaluation.write("True Positive (D) = ", truePositiveD);

		DecimalFormat df = new DecimalFormat("#.000");
		pmResultEvaluation.write("Precision (A) = ", df.format(precisionA));
		pmResultEvaluation.write("Precision (V) = ", df.format(precisionV));
		pmResultEvaluation.write("Precision (X) = ", df.format(precisionX));
		pmResultEvaluation.write("Precision (R) = ", df.format(precisionR));
		pmResultEvaluation.write("Precision (D) = ", df.format(precisionD));
		pmResultEvaluation.write("Recall (A) = ", df.format(recallA));
		pmResultEvaluation.write("Recall (V) = ", df.format(recallV));
		pmResultEvaluation.write("Recall (X) = ", df.format(recallX));
		pmResultEvaluation.write("Recall (R) = ", df.format(recallR));
		pmResultEvaluation.write("Recall (D) = ", df.format(recallD));
		pmResultEvaluation.write("F-measure (A) = ", df.format(fMeasureA));
		pmResultEvaluation.write("F-measure (V) = ", df.format(fMeasureV));
		pmResultEvaluation.write("F-measure (X) = ", df.format(fMeasureX));
		pmResultEvaluation.write("F-measure (R) = ", df.format(fMeasureR));
		pmResultEvaluation.write("F-measure (D) = ", df.format(fMeasureD));
		pmResultEvaluation.write("Acuracia (A) = ", df.format(accuracyA));
		pmResultEvaluation.write("Acuracia (V) = ", df.format(accuracyV));
		pmResultEvaluation.write("Acuracia (X) = ", df.format(accuracyX));
		pmResultEvaluation.write("Acuracia (R) = ", df.format(accuracyR));
		pmResultEvaluation.write("Acuracia (D) = ", df.format(accuracyD));

		pmResultEvaluation.write("");
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
		FilterSmellResult smellsCommit = FilterSmells.filtrar(report, listaLimiarTecnica, commit);
		// FilterSmells.gravarMetodosSmell(smellsCommitInitial.getMetodosSmell(),
		// resultFileName + "-smells-commit-initial.csv");
		return smellsCommit;
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
			for (int i = posPrimeiroParenteses + 1; i < posUltimoParenteses - 1; i++) {
				if (metodo.charAt(i) != ' ' && metodo.charAt(i) != ')') {
					countParams++;
					break;
				}
			}
		}
		return countParams;
	}

	private FilterSmellResult obterSmellsPreviousCommit(Repository repo, CommitData commit) throws Exception {
		CommitData previousCommit = commit.getPrevious();
		FilterSmellResult smellsPreviousCommit;
		if (previousCommit != null)
			smellsPreviousCommit = obterSmellsCommit(previousCommit.getId(), repo);
		else
			smellsPreviousCommit = obterSmellsCommit(commit.getId(), repo);
		return smellsPreviousCommit;
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
