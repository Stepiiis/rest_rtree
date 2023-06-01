package cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence;

import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PersistentSequenceGenerator implements SequenceGeneratorInterface {
	private int counter;
	private int startValue;
	private String indexFolder;

	Logger LOGG = Logger.getLogger(PersistentSequenceGenerator.class.getName());

	public PersistentSequenceGenerator(String indexFolder, int startValue) {
		this.indexFolder = "db/"+indexFolder;
		try {
			Path path = Paths.get(this.indexFolder);
			if (Files.notExists(path))
				Files.createDirectories(path);
		} catch (IOException e) {
			LOGG.severe("Could not create directory for index files");
			throw new DatabaseException("Could not create directory for index files");
		}
		if(startValue <= 0)
			throw new DatabaseException("Sequence start value has to be greater than 0");
		if(Files.notExists(Paths.get(this.indexFolder + "/sequence.bin"))) {
			counter = startValue;
			this.startValue = startValue;
			writeCounterToFile();
		}else{
			this.startValue = 1;
			loadCounterFromFile();
		}
	}
	public PersistentSequenceGenerator(String indexFolder){
		this(indexFolder, 1);
	}

	@Override
	public int getAndIncrease() {
		loadCounterFromFile();
		int temp = counter;
		counter++;
		writeCounterToFile();
		return temp;
	}

	private void writeCounterToFile() throws DatabaseException {
		FileHandlingUtil.handleFileOperation(indexFolder + "/sequence.bin", "rw", LOGG, file -> {
			file.seek(0);
			file.writeInt(counter);
			return 0;
		});
	}

	private void loadCounterFromFile() throws DatabaseException {
 		counter = FileHandlingUtil.handleFileOperation(indexFolder + "/sequence.bin", "r", LOGG, file -> {
			file.seek(0);
			return file.readInt();
		});
	}

	@Override
	public int getCurrentValue() {
		loadCounterFromFile();
		return counter;
	}

	@Override
	public void setCurrentValue(int value) {
		counter = value;
        writeCounterToFile();
	}

	@Override
	public void reset() {
		this.counter = startValue;
		writeCounterToFile();
	}


}
