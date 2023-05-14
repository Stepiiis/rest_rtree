package cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence;

import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class PersistentSequenceGenerator implements SequenceGeneratorInterface {
    private int counter = 1;
    private String indexFolder;

    Logger LOGG = Logger.getLogger(PersistentSequenceGenerator.class.getName());

    public PersistentSequenceGenerator(String indexFolder){
        this.indexFolder = indexFolder;
        try {
            Files.createDirectories(Paths.get(indexFolder));
        }catch(IOException e) {
            LOGG.severe("Could not create directory for index files");
             throw new DatabaseException("Could not create directory for index files");
        }
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
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(indexFolder + "/sequence.bin"));
            byte[] bytes = new byte[4];
            java.nio.ByteBuffer.wrap(bytes).putInt(counter);
            bos.write(bytes, 0, bytes.length);
        }catch(IOException e){
            LOGG.severe("Couldn't load sequence.bin for index: "+indexFolder);
            throw new DatabaseException("Couldn't load sequence.bin for index: "+indexFolder);
        }
    }

    private void loadCounterFromFile() throws DatabaseException {
        try {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(indexFolder + "/sequence.bin"));
            byte[] bytes = new byte[4];
            if(bis.read(bytes, 0, bytes.length) != bytes.length)
                LOGG.info("Creating new sequence for index: "+indexFolder);
            else
                counter = java.nio.ByteBuffer.wrap(bytes).getInt();
        }catch(IOException e){
            LOGG.severe("Couldn't load sequence.bin for index: "+indexFolder);
            throw new DatabaseException("Couldn't load sequence.bin for index: "+indexFolder);
        }
    }

    @Override
    public int getCurrentValue() {
        loadCounterFromFile();
        return counter;
    }
    @Override
    public void setCurrentValue(int value) {
        counter = value;
    }
    
    
    
}
