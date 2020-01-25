package org.smellrefactored.refactoringminer.wrapper;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.refactoringminer.api.RefactoringType;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.persistence.csv.CSVFile;

import com.opencsv.CSVReader;

import gr.uom.java.xmi.diff.CodeRange;

public class RefactoringMinerWrapperCacheCsv implements RefactoringMinerWrapperCache {
	
	private String cacheFileName;

	public RefactoringMinerWrapperCacheCsv(String resultBaseFileName) {
		String fileRefactoringVersion = "1";
		this.cacheFileName = resultBaseFileName + "-v" + fileRefactoringVersion + ".csv";
	}
	

	public boolean hasCache() {
		File cacheFile = new File(this.cacheFileName);
		return (cacheFile.exists());
	}
	

	public void saveRefactoringDtoListToFile(List<RefactoringMinerWrapperDto> refactoringDtoList) {
		String fileRefactoringNameTemp = this.cacheFileName.replace(".csv", ".temp");
		PersistenceMechanism refactoringFileHandler = new CSVFile(fileRefactoringNameTemp, false);
		refactoringFileHandler.write("Commmit-id", "Refactring-name", "Refactoring-Type", "Code Element Left",
				"Code Element Right", "Class Before", "Class After");
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			refactoringFileHandler.write(refactoringDto.commitId, refactoringDto.name, refactoringDto.type,
					refactoringDto.leftSide.size() > 0 ? refactoringDto.leftSide.get(0).getCodeElement() : 0,
					refactoringDto.rightSide.size() > 0 ? refactoringDto.rightSide.get(0).getCodeElement() : 0,
					refactoringDto.involvedClassesBefore,
					refactoringDto.involvedClassesAfter);
		}
		refactoringFileHandler.close();
		File tempfile =new File(fileRefactoringNameTemp);
		File newfile =new File(this.cacheFileName);
		tempfile.renameTo(newfile);
	}

	
	public List<RefactoringMinerWrapperDto> getRefactoringDtoListFromFile() {
		List<RefactoringMinerWrapperDto> refactoringDtoList = new ArrayList<RefactoringMinerWrapperDto>(); 
		try {
			CSVReader reader = new CSVReader(new FileReader(this.cacheFileName));
			String[] nextLine;
			nextLine = reader.readNext(); // ignore header
			if (nextLine != null) {
				while ((nextLine = reader.readNext()) != null) {
					RefactoringMinerWrapperDto refactoringDto = getRefactoringDtoFromCsvLine(nextLine);
					refactoringDtoList.add(refactoringDto);
				}
			}
			reader.close();
		} catch (Exception e) {
			e.getStackTrace();
		}
		return refactoringDtoList;
	}
	
	
	private static RefactoringMinerWrapperDto getRefactoringDtoFromCsvLine(String[] line) {
		RefactoringMinerWrapperDto result = new RefactoringMinerWrapperDto();
		try {
			result.commitId  = line[0];
			result.name      = line[1];
			result.type      = RefactoringType.valueOf(line[2]);
			
			result.leftSide  = new ArrayList<CodeRange>();
			if ( (line[3] != null) && (line[3] != "") && (line[3] != "0")) {
				CodeRange leftSideCodeRange = new CodeRange(null, -1, -1, -1, -1, null);
				leftSideCodeRange.setCodeElement(line[3]);
				result.leftSide.add(leftSideCodeRange);
			}

			result.rightSide  = new ArrayList<CodeRange>();
			if ( (line[4] != null) && (line[4] != "") && (line[4] != "0")) {
				CodeRange rightSideCodeRange = new CodeRange(null, -1, -1, -1, -1, null);
				rightSideCodeRange.setCodeElement(line[4]);
				result.rightSide.add(rightSideCodeRange);
			}

			result.involvedClassesBefore = new ArrayList<String>();
			result.involvedClassesBefore.add(line[5]);

			result.involvedClassesAfter = new ArrayList<String>();
			result.involvedClassesAfter.add(line[6]);
		} catch (Exception e) {
			e.getStackTrace();
		}
		return result;
	}

	
}
