package org.smellrefactored;

import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.junit.Assert;
import org.junit.Test;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.GitService;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;
import org.refactoringminer.util.GitServiceImpl;

public class SmellRefactoredManagerTest {

	@Test
	public void testExecute() {
		
		// String dir = System.getProperty("java.io.tmpdir");
		// String directory = FileUtils.getTempDirectoryPath();
		
		GitService gitService = new GitServiceImpl();
		GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
		try {
			Repository repo = gitService.cloneIfNotExists("D:\\Projetos_Android\\refactoring-toy-example",
					"https://github.com/danilofes/refactoring-toy-example.git");

			miner.detectAll(repo, "master", new RefactoringHandler() {
				@Override
				public void handle(String commitId, List<Refactoring> refactorings) {
					System.out.println("Refactorings at " + commitId);
					for (Refactoring ref : refactorings) {
						System.out.println(ref.toString());
					}
				}
			});

			//SmellRefactoredManager manager = new SmellRefactoredManager();
			// manager.execute();
			Assert.assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}

	}

}
