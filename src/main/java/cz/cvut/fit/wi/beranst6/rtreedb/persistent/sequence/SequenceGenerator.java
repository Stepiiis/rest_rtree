//package cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence;
//
//import cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase;
//import org.springframework.stereotype.Service;
//
//import java.io.*;
//import java.util.logging.Logger;
//
//import static cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil.*;
//
//@Service
//public class SequenceGenerator implements SequenceGeneratorInterface {
//    int currentValue;
//    int defaultStartValue = 0xFFFF; // just to eliminate possible collisions
//    String sequenceFileName = "sequence.bin";
//    Logger LOGG = Logger.getLogger(PersistentCachedDatabase.class.getName());
//
//    SequenceGenerator(){
//        readCurrentValueFromFile();
//    }
//
//    public int getAndIncrease(){
//        int value = currentValue;
//        currentValue++;
//        writeCurrentValueToFile();
//        return value;
//    }
//    public int getCurrentValue(){
//        return currentValue;
//    }
//    public void setCurrentValue(int value){
//        currentValue=value;
//        writeCurrentValueToFile();
//    }
//
//    private void writeCurrentValueToFile(){
//        File file = new File(sequenceFileName);
//        try(BufferedOutputStream bis = new BufferedOutputStream(new FileOutputStream(file));){
//            byte[] data = getByteArrayFromInteger(currentValue);
//            bis.write(data);
//            bis.flush();
//        } catch (FileNotFoundException e) {
//            LOGG.severe("COULDN'T FIND FILE: "+sequenceFileName);
//        } catch (IOException e) {
//            LOGG.severe("COULDN'T WRITE TO FILE: "+sequenceFileName);
//        }
//    }
//    private void readCurrentValueFromFile(){
//        File file = new File(sequenceFileName);
//        byte[] data = new byte[(int) file.length()];
//        try(BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));){
//            bis.read(data);
//            currentValue = getIntegerFromByteArray(data,0);
//        } catch (FileNotFoundException e) {
//            LOGG.severe("COULDN'T FIND FILE: "+sequenceFileName);
//            currentValue = defaultStartValue;
//        } catch (IOException e) {
//            LOGG.severe("COULDN'T READ FILE: "+sequenceFileName);
//            currentValue = defaultStartValue;
//        }
//    }
//
//}
