package org.smellrefactored.refactoringminer.wrapper;

import java.io.FileNotFoundException;
import java.util.List;

public interface RefactoringMinerWrapperCache {

	boolean hasCache();

	void saveRefactoringDtoListToFile(List<RefactoringMinerWrapperDto> refactoringDtoList) throws Exception;

	List<RefactoringMinerWrapperDto> getRefactoringDtoListFromFile() throws FileNotFoundException;

}