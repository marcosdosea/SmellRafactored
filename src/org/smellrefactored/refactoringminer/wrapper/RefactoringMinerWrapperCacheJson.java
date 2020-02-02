package org.smellrefactored.refactoringminer.wrapper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class RefactoringMinerWrapperCacheJson {

	private String cacheFileName;
	
	public RefactoringMinerWrapperCacheJson(String resultBaseFileName) {
		String fileRefactoringVersion = "1";
		this.cacheFileName = resultBaseFileName + "-cache-v" + fileRefactoringVersion + ".json";
	}

	public boolean hasCache() {
		File cacheFile = new File(this.cacheFileName);
		return (cacheFile.exists());
	}

	public void saveRefactoringDtoListToFile(List<RefactoringMinerWrapperDto> refactoringDtoList) throws Exception {
		Gson gson = new Gson();
		String fileRefactoringNameTemp = this.cacheFileName.replace(".json", ".temp");
		File cacheFile = new File(fileRefactoringNameTemp);
		cacheFile.mkdirs();
		cacheFile.delete();
		final FileWriter refactoringFileHandler = new FileWriter(fileRefactoringNameTemp);
		refactoringFileHandler.append("[\n"); // keep this character lonely.
		for (RefactoringMinerWrapperDto refactoringDto : refactoringDtoList) {
			try {
				refactoringFileHandler.append(gson.toJson(refactoringDto) + ",\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		refactoringFileHandler.append("]");
		refactoringFileHandler.close();
		File tempfile = new File(fileRefactoringNameTemp);
		File newfile = new File(this.cacheFileName);
		tempfile.renameTo(newfile);
	}

	public List<RefactoringMinerWrapperDto> getRefactoringDtoListFromFile() throws FileNotFoundException {
		RefactoringMinerWrapperDto[] refactoringDtoArray = getRefactoringDtoArrayFromFile(this.cacheFileName);  
		List<RefactoringMinerWrapperDto> refactoringDtoList = Arrays.asList(refactoringDtoArray);
		return (refactoringDtoList);
	}

	private RefactoringMinerWrapperDto[] getRefactoringDtoArrayFromFile(String fileRefactoringName) throws FileNotFoundException {
		Gson gson = new Gson();  
		JsonReader reader = new JsonReader(new FileReader(fileRefactoringName));
		return (gson.fromJson(reader, RefactoringMinerWrapperDto[].class));  
	}

}
