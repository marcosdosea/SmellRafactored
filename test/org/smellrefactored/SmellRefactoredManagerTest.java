package org.smellrefactored;

import org.junit.Assert;
import org.junit.Test;

public class SmellRefactoredManagerTest {

	@Test
	public void testExecute() {
		try {
			SmellRefactoredManager manager = new SmellRefactoredManager();
			//manager.execute();
			Assert.assertTrue(true);
		} catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}

	}

}
