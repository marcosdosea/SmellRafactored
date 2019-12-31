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
import org.designroleminer.smelldetector.model.ClassDataSmelly;
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
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVReader;

public class SmellRefactoredManager {

	private String[] TECHNIQUES = {"A", "V", "X", "R", "D"};
	
	public static final String[] LONG_CLASS_REFACTORIES = {
			RefactoringType.EXTRACT_CLASS.toString()
			, RefactoringType.EXTRACT_SUBCLASS.toString()
			, RefactoringType.EXTRACT_SUPERCLASS.toString()
			, RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString()
			, RefactoringType.MOVE_OPERATION.toString()
			, RefactoringType.PULL_UP_OPERATION.toString()
			, RefactoringType.PUSH_DOWN_OPERATION.toString()
			};
	public static final String[] CLASS_REFACTORIES = {
			RefactoringType.EXTRACT_CLASS.toString()
			, RefactoringType.EXTRACT_SUBCLASS.toString()
			, RefactoringType.EXTRACT_SUPERCLASS.toString()
			, RefactoringType.EXTRACT_AND_MOVE_OPERATION.toString()
			, RefactoringType.MOVE_OPERATION.toString()
			, RefactoringType.PULL_UP_OPERATION.toString()
			, RefactoringType.PUSH_DOWN_OPERATION.toString()
			, RefactoringType.RENAME_CLASS.toString()
			};
		

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
	PersistenceMechanism pmResultEvaluationMethod;
	PersistenceMechanism pmResultEvaluationClass;
	PersistenceMechanism pmResultSmellRefactoredMethods;
	PersistenceMechanism pmResultSmellRefactoredMethodsMessage;

	PersistenceMechanism pmResultSmellRefactoredClasses;
	PersistenceMechanism pmResultSmellRefactoredClassesMessage;

	PersistenceMechanism pmResultSmellRefactoredCommit;
	HashSet<String> listCommitEvaluated = new HashSet<String>();
	
	String fileRefactoringName;
	Boolean reuseRefactoringFile;

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

		pmResultSmellRefactoredCommit = new CSVFile(resultFileName + "-smellRefactored-commit.csv", false);

		pmResultEvaluationMethod = new CSVFile(resultFileName + "-evaluations-methods.csv", false);
		pmResultSmellRefactoredMethods = new CSVFile(resultFileName + "-smellRefactored.csv", false);
		pmResultSmellRefactoredMethodsMessage = new CSVFile(resultFileName + "-smellRefactored-message.csv", false);

		pmResultEvaluationClass = new CSVFile(resultFileName + "-evaluation-classes.csv", false);
		pmResultSmellRefactoredClasses = new CSVFile(resultFileName + "-smellRefactored-classes.csv", false);
		pmResultSmellRefactoredClassesMessage = new CSVFile(resultFileName + "-smellRefactored-classes-message.csv", false);
		
		fileRefactoringName = resultFileName + "-refactoring.csv";
		
		reuseRefactoringFile = false; // Gera o arquivo apenas uma vez a cada execução do estudo, evitando problemas durante o desenvolvimento e quando houver mudança na faixa de commits
	}

	public void getSmellRefactoredMethods() {

		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		try {
			repo = gitService.cloneIfNotExists(localFolder, urlRepository);

			if (!reuseRefactoringFile) {
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
				reuseRefactoringFile = true;
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
			pmResultEvaluationMethod.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluationMethod.write("Numero Refatoracoes em Metodos e Nao Metodos:",
					listRefactoringMergedIntoMaster.size());
			pmResultEvaluationMethod.write("Numero Refatoracoes relacionadas a operacoes em Metodos:",
					countRefactoringRelatedOperations);
			pmResultEvaluationMethod.write("Numero Refatoracoes relacionadas a rename em Metodos:",
					countRefactoringRelatedRenaming);

			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");
			FilterSmellResult smellsCommitInitial = obterSmellsCommit(initialCommit, repo);

			pmResultEvaluationMethod.write("Numero Metodos Smell Commit Inicial:",
					smellsCommitInitial.getMetodosSmell().size());
			pmResultEvaluationMethod.write("Numero Metodos NOT Smell Commit Inicial:",
					smellsCommitInitial.getMetodosNotSmelly().size());

			pmResultSmellRefactoredMethodsMessage.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP",
					"Tecnicas", "Commit", "Refactoring", "Left Side", "Right Side", "Full Message");
			pmResultSmellRefactoredMethods.write("Class", "Method", "Smell", "LOC", "CC", "EC", "NOP", "Tecnicas",
					"Commit", "Refactoring", "Left Side", "Right Side");

			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.LONG_METHOD);
			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.COMPLEX_METHOD);
			evaluateSmellChangeOperation(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.HIGH_EFFERENT_COUPLING);
			evaluateSmellChangeParameters(smellsCommitInitial, listRefactoringMergedIntoMaster, repo,
					MethodDataSmelly.MANY_PARAMETERS);
		} catch (Exception e) {
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
		FilterSmells.gravarMetodosSmell(smellsCommitInitial.getMetodosSmell(), resultFileName + "-smells-commit-initial-method.csv");
		FilterSmells.gravarClassesSmell(smellsCommitInitial.getClassesSmell(), resultFileName + "-smells-commit-initial-class.csv");
		return smellsCommitInitial;
	}

	private void evaluateSmellChangeOperation(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, Repository repo, String typeSmell) throws Exception {

		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell, TECHNIQUES);

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
									confusionMatrices.incFalseNegativeForAllTechniques();
									refactoredMethod = true;
									pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
											methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
											methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
											methodNotSmell.getEfferent(), methodNotSmell.getNumberOfParameters(),
											methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
											methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
											methodRefactored.getRightSide(), methodRefactored.getFullMessage());

									pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
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
				confusionMatrices.incTrueNegativeForAllTechniques();
				pmResultSmellRefactoredMethodsMessage.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "", "");

				pmResultSmellRefactoredMethods.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "");
			}
		}

		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {

			if (methodSmelly.getSmell().equals(typeSmell)) {
				MethodDataSmelly methodSmellyBuscar = methodSmelly;
				boolean renamedMethod = false;
				confusionMatrices.resetSensibleTechniques();
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
										confusionMatrices.incTruePositiveForSensibleTechniques(methodSmellyBuscar.getListaTecnicas());
										pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
												methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
												methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
												methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
												methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
												methodRefactored.getRefactoringType(), methodRefactored.getLeftSide(),
												methodRefactored.getRightSide(), methodRefactored.getFullMessage());
										pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
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
				} while (renamedMethod && confusionMatrices.hasInsensibleTechniques());
				
				confusionMatrices.incFalsePositiveForInsensibleTechniques(methodSmellyBuscar.getListaTecnicas());
				if (confusionMatrices.hasInsensibleTechniques()) {
					pmResultSmellRefactoredMethodsMessage.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");

					pmResultSmellRefactoredMethods.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		confusionMatrices.writeToCsvFile(pmResultEvaluationMethod);
	
	}

	private void evaluateSmellChangeParameters(FilterSmellResult commitInitial,
			ArrayList<RefactoringData> listRefactoring, Repository repo, String typeSmell) throws Exception {

		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell, TECHNIQUES);

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
											confusionMatrices.incFalseNegativeForAllTechniques();
											refactoredMethod = true;
											pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
													methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
													methodNotSmell.getLinesOfCode(), methodNotSmell.getComplexity(),
													methodNotSmell.getEfferent(),
													methodNotSmell.getNumberOfParameters(),
													methodRefactored.getListaTecnicas(), methodRefactored.getCommit(),
													methodRefactored.getRefactoringType(),
													methodRefactored.getLeftSide(), methodRefactored.getRightSide(),
													methodRefactored.getFullMessage());

											pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
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
				confusionMatrices.incTrueNegativeForAllTechniques();
				pmResultSmellRefactoredMethodsMessage.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "", "");

				pmResultSmellRefactoredMethods.write(methodBuscar.getNomeClasse(), methodBuscar.getNomeMetodo(),
						methodBuscar.getSmell(), methodBuscar.getLinesOfCode(), methodBuscar.getComplexity(),
						methodBuscar.getEfferent(), methodBuscar.getNumberOfParameters(),
						methodBuscar.getListaTecnicas(), methodBuscar.getCommit(), "", "", "");
			}
		}

		for (MethodDataSmelly methodSmelly : commitInitial.getMetodosSmell()) {

			if (methodSmelly.getSmell().equals(typeSmell)) {
				MethodDataSmelly methodSmellyBuscar = methodSmelly;
				boolean renamedMethod = false;
				confusionMatrices.resetSensibleTechniques();
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
												confusionMatrices.incTruePositiveForSensibleTechniques(methodSmellyBuscar.getListaTecnicas());
												pmResultSmellRefactoredMethodsMessage.write(methodRefactored.getNomeClasse(),
														methodRefactored.getNomeMetodo(), methodRefactored.getSmell(),
														methodSmell.getLinesOfCode(), methodSmell.getComplexity(),
														methodSmell.getEfferent(), methodSmell.getNumberOfParameters(),
														methodRefactored.getListaTecnicas(),
														methodRefactored.getCommit(),
														methodRefactored.getRefactoringType(),
														methodRefactored.getLeftSide(), methodRefactored.getRightSide(),
														methodRefactored.getFullMessage());
												pmResultSmellRefactoredMethods.write(methodRefactored.getNomeClasse(),
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
				} while (renamedMethod && confusionMatrices.hasInsensibleTechniques());

				confusionMatrices.incFalsePositiveForInsensibleTechniques(methodSmellyBuscar.getListaTecnicas());
				if (confusionMatrices.hasInsensibleTechniques()) {
					pmResultSmellRefactoredMethodsMessage.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "", "");

					pmResultSmellRefactoredMethods.write(methodSmellyBuscar.getNomeClasse(),
							methodSmellyBuscar.getNomeMetodo(), methodSmellyBuscar.getSmell(),
							methodSmellyBuscar.getLinesOfCode(), methodSmellyBuscar.getComplexity(),
							methodSmellyBuscar.getEfferent(), methodSmellyBuscar.getNumberOfParameters(),
							methodSmellyBuscar.getListaTecnicas(), methodSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		confusionMatrices.writeToCsvFile(pmResultEvaluationMethod);
		
	}

	public void getSmellRefactoredClasses() {

		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();

		try {
			repo = gitService.cloneIfNotExists(localFolder, urlRepository);

			if (!reuseRefactoringFile) {
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
				reuseRefactoringFile = true;
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
				if (commitsWithRefactorings.contains(commit.getId())) {
					commitsWithRefactoringMergedIntoMaster.add(commit);
				}
			}

			int countRefactoringRelatedClasses = 0;

			ArrayList<RefactoringData> listRefactoringMergedIntoMaster = new ArrayList<RefactoringData>();
			for (RefactoringData refactoring : listRefactoring) {
				for (CommitData commit : commitsMergedIntoMaster) {
					if (refactoring.getCommit().equals(commit.getId())) {
						refactoring.setCommitDate(commit.getDate());
						refactoring.setFullMessage(commit.getFullMessage());
						refactoring.setShortMessage(commit.getShortMessage());
						listRefactoringMergedIntoMaster.add(refactoring);
						if (refactoring.getRefactoringType().equals(RefactoringType.RENAME_CLASS.toString())) {
							// countRefactoringRelatedRenaming++;
						} else {
							for (String longClassRefactoring : CLASS_REFACTORIES) {
								if (refactoring.getRefactoringType().equals(longClassRefactoring)) {
									countRefactoringRelatedClasses++;
									/// break;
								}
							}
						}
					}
				}
			}
			
			Collections.sort(listRefactoringMergedIntoMaster);
			
			pmResultEvaluationClass.write("RELATORIO COMPLETO SISTEMA");
			pmResultEvaluationClass.write("Numero Refatoracoes em Metodos e Nao Metodos:", listRefactoringMergedIntoMaster.size());
			pmResultEvaluationClass.write("Numero Refatoracoes relacionadas a Classes:", countRefactoringRelatedClasses);

			pmResultSmellRefactoredCommit.write("Commit", "NumberOfClasses", "NumberOfMethods", "SystemLOC");
			FilterSmellResult smellsCommitInitial = obterSmellsCommit(initialCommit, repo);

			pmResultEvaluationClass.write("Numero Classes Smell Commit Inicial:",
					smellsCommitInitial.getClassesSmell().size());
			pmResultEvaluationClass.write("Numero Classes NOT Smell Commit Inicial:",
					smellsCommitInitial.getClassesNotSmelly().size());

			pmResultSmellRefactoredClassesMessage.write("Class", "Smell", "CLOC", "Tecnicas", "Commit", "Refactoring",
					"Left Side", "Right Side", "Full Message");
			pmResultSmellRefactoredClasses.write("Class", "Smell", "CLOC", "Tecnicas", "Commit", "Refactoring",
					"Left Side", "Right Side");

			evaluateSmellChangeClass(smellsCommitInitial, listRefactoringMergedIntoMaster, repo, ClassDataSmelly.LONG_CLASS);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	private void evaluateSmellChangeClass(FilterSmellResult commitInitial, ArrayList<RefactoringData> listRefactoring,
			Repository repo, String typeSmell) throws Exception {

		ConfusionMatrixTechniques confusionMatrices = new ConfusionMatrixTechniques(typeSmell, TECHNIQUES);
		
		for (ClassDataSmelly classNotSmelly : commitInitial.getClassesNotSmelly()) {
			ClassDataSmelly classBuscar = classNotSmelly;
			boolean renamedClass = false;
			boolean refactoredClass = false;
			Date dateCommitRenamed = null;
			do {
				renamedClass = false;
				String classRenamedName = null;
				for (RefactoringData refactoring : listRefactoring) {
					boolean isClassInvolvedBefore = refactoring.getInvolvedClassesBefore()
							.contains(classBuscar.getNomeClasse());
					boolean isClassInvolvedAfter = refactoring.getInvolvedClassesAfter()
							.contains(classBuscar.getNomeClasse());
					boolean isClassInvolved = isClassInvolvedBefore || isClassInvolvedAfter;

					boolean isClassRefactored = refactoring.getLeftSide().contains(classBuscar.getNomeClasse())
							|| refactoring.getRightSide().contains(classBuscar.getNomeClasse());

					if (isClassInvolved && isClassRefactored) {
						if (refactoring.getRefactoringType().equals(RefactoringType.RENAME_CLASS.toString())
								&& refactoring.getLeftSide().contains(classBuscar.getNomeClasse())
								&& refactoring.getInvolvedClassesBefore().contains(classBuscar.getNomeClasse())) {

							if ((dateCommitRenamed == null) || (dateCommitRenamed != null
									&& dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) {
								renamedClass = true;
								dateCommitRenamed = refactoring.getCommitDate();
								classRenamedName = refactoring.getRightSide();
							}
						} else if (isClassInvolvedBefore) { // && !isClassInvolvedAfter
							Boolean isClassRefactoring = false; 
							for (String longClassRefactoring : CLASS_REFACTORIES) {
								if (refactoring.getRefactoringType().equals(longClassRefactoring)) {
									isClassRefactoring = true;
									break;
								}
							}
							if (isClassRefactoring) {
								FilterSmellResult smellsPreviousCommit = obterSmellsPreviousCommit(repo,
										refactoring.getCommit());
								for (ClassDataSmelly classNotSmell : smellsPreviousCommit.getClassesNotSmelly()) {
									boolean isSameClass = classNotSmell.getNomeClasse()
											.equals(classBuscar.getNomeClasse());
									if (isSameClass) {
										confusionMatrices.incFalseNegativeForAllTechniques();
										refactoredClass = true;
										pmResultSmellRefactoredClassesMessage.write(refactoring.getNomeClasse(),
												refactoring.getSmell(), classNotSmell.getLinesOfCode(),
												refactoring.getListaTecnicas(), refactoring.getCommit(),
												refactoring.getRefactoringType(), refactoring.getLeftSide(),
												refactoring.getRightSide(), refactoring.getFullMessage());
										pmResultSmellRefactoredClasses.write(refactoring.getNomeClasse(),
												refactoring.getSmell(), classNotSmell.getLinesOfCode(),
												refactoring.getListaTecnicas(), refactoring.getCommit(),
												refactoring.getRefactoringType(), refactoring.getLeftSide(),
												refactoring.getRightSide());
										break;
									}
								}
							}
						}
					}
				}
				if (renamedClass) {
					classBuscar.setNomeClasse(classRenamedName);
				} else {
					dateCommitRenamed = null;
				}
			} while (renamedClass && !refactoredClass);
			if (!refactoredClass) {
				confusionMatrices.incTrueNegativeForAllTechniques();
				pmResultSmellRefactoredClassesMessage.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
						classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
						"", "");

				pmResultSmellRefactoredClasses.write(classBuscar.getNomeClasse(), classBuscar.getSmell(),
						classBuscar.getLinesOfCode(), classBuscar.getListaTecnicas(), classBuscar.getCommit(), "", "",
						"");
			}
		}

		for (ClassDataSmelly classSmelly : commitInitial.getClassesSmell()) {

			if (classSmelly.getSmell().equals(typeSmell)) {
				ClassDataSmelly classSmellyBuscar = classSmelly;
				boolean renamedClass = false;
				confusionMatrices.resetSensibleTechniques();
				Date dateCommitRenamed = null;
				do {
					renamedClass = false;
					String classRenamedName = null;
					for (RefactoringData refactoring : listRefactoring) {
						boolean isClassInvolvedBefore = refactoring.getInvolvedClassesBefore()
								.contains(classSmellyBuscar.getNomeClasse());
						boolean isClassInvolvedAfter = refactoring.getInvolvedClassesAfter()
								.contains(classSmellyBuscar.getNomeClasse());
						boolean isClassInvolved = isClassInvolvedBefore || isClassInvolvedAfter;
						boolean isClassRefactored = refactoring.getLeftSide()
								.contains(classSmellyBuscar.getNomeClasse())
								|| refactoring.getRightSide().contains(classSmellyBuscar.getNomeClasse());
						
						
						/*
						 * logger.info( "DEBUG: classSmellyBuscar: " + classSmellyBuscar.getNomeClasse()
						 * + " " + refactoring.getNomeClasse() + " " +
						 * refactoring.getInvolvedClassesBefore() + " " +
						 * refactoring.getInvolvedClassesAfter() + " " + classSmellyBuscar.getSmell() +
						 * " " + refactoring.getRefactoringType() + " " + isClassInvolvedBefore + " " +
						 * isClassInvolvedAfter + " " + isClassInvolved + " " + isClassRefactored );
						 */						
						if (isClassInvolved && isClassRefactored) {
							if (refactoring.getRefactoringType().equals(RefactoringType.RENAME_CLASS.toString())
									&& refactoring.getLeftSide().contains(classSmellyBuscar.getNomeClasse())
									&& refactoring.getInvolvedClassesBefore()
											.contains(classSmellyBuscar.getNomeClasse())) {
								if ((dateCommitRenamed == null) || (dateCommitRenamed != null
										&& dateCommitRenamed.compareTo(refactoring.getCommitDate()) < 0)) {
									renamedClass = true;
									dateCommitRenamed = refactoring.getCommitDate();
									classRenamedName = refactoring.getRightSide();
								}
							} else if (isClassInvolvedBefore) {
								Boolean isLongClassRefactoring = false; 
								for (String longClassRefactoring : LONG_CLASS_REFACTORIES) {
									if (refactoring.getRefactoringType().equals(longClassRefactoring)) {
										// logger.info("DEBUG: isLongClassRefactoring: " + longClassRefactoring + " " + classSmellyBuscar.getNomeClasse());
										isLongClassRefactoring = true;
										break;
									}
								}
								if (isLongClassRefactoring) {
									FilterSmellResult smellsPreviousCommit = obterSmellsPreviousCommit(repo,
											refactoring.getCommit());
									// logger.info("DEBUG: isLongClassRefactoring: " + refactoring.getCommit());
									for (ClassDataSmelly classSmell : smellsPreviousCommit.getClassesSmell()) {
										boolean isSameClass = classSmell.getNomeClasse()
												.equals(classSmellyBuscar.getNomeClasse());
										// logger.info("DEBUG: Class smell: " + classSmell.getSmell());
										if ((isSameClass) && classSmell.getSmell().equals(typeSmell)) {
											confusionMatrices.incTruePositiveForSensibleTechniques(classSmellyBuscar.getListaTecnicas());
											pmResultSmellRefactoredClassesMessage.write(refactoring.getNomeClasse(),
													refactoring.getSmell(), classSmell.getLinesOfCode(),
													refactoring.getListaTecnicas(), refactoring.getCommit(),
													refactoring.getRefactoringType(), refactoring.getLeftSide(),
													refactoring.getRightSide(), refactoring.getFullMessage());
											pmResultSmellRefactoredClasses.write(refactoring.getNomeClasse(),
													refactoring.getSmell(), classSmell.getLinesOfCode(),
													refactoring.getListaTecnicas(), refactoring.getCommit(),
													refactoring.getRefactoringType(), refactoring.getLeftSide(),
													refactoring.getRightSide());
										}
									}
								}
							}
						}
					}
					if (renamedClass) {
						classSmellyBuscar.setNomeClasse(classRenamedName);
					} else {
						dateCommitRenamed = null;
					}
				} while (renamedClass && confusionMatrices.hasInsensibleTechniques());
				
				confusionMatrices.incFalsePositiveForInsensibleTechniques(classSmellyBuscar.getListaTecnicas());
				if (confusionMatrices.hasInsensibleTechniques()) {
					pmResultSmellRefactoredClassesMessage.write(classSmellyBuscar.getNomeClasse(),
							classSmellyBuscar.getSmell(), classSmellyBuscar.getLinesOfCode(),
							classSmellyBuscar.getListaTecnicas(), classSmellyBuscar.getCommit(), "", "", "", "");

					pmResultSmellRefactoredClasses.write(classSmellyBuscar.getNomeClasse(),
							classSmellyBuscar.getSmell(), classSmellyBuscar.getLinesOfCode(),
							classSmellyBuscar.getListaTecnicas(), classSmellyBuscar.getCommit(), "", "", "");
				}

			}
		}

		confusionMatrices.writeToCsvFile(pmResultEvaluationClass);
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
		if (achouCommit) {
			smellsPreviousCommit = obterSmellsCommit(previousCommit.getId(), repo);
		} else {
			smellsPreviousCommit = obterSmellsCommit(commitId, repo);
		}
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
