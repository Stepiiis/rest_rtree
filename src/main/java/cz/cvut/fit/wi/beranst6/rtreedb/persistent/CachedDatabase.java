package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class CachedDatabase implements DatabaseInterface{

    private final Vector<RTreeInternalNode> cachedDB;
    private final Vector<RTreeObject> cachedObjects;
    private final int cacheSize;

    public CachedDatabase(int cacheSize){
        cachedDB = new Vector<>();
        cachedDB.setSize(cacheSize);
        this.cacheSize=cacheSize;
    }


    @Override
    public RTreeRegion get(int id, int index) {
        if(cachedDB.get(id % cacheSize) == null){
            return getFromFile(id, index);
        }
    }

    @Override
    public int put(int id, RTreeObject object) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public void delete(int id, int index) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private RTreeRegion getFromFile(int id, int index){
        String fileName = "example.bin";
        File file = new File(fileName);

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            ArrayList<RTreeNode> objects = new ArrayList<>();
            for(int i = Constants.BIN_FILE_HEADER_SIZE; i< data.length; i += Constants.BIN_FILE_NODE_SIZE) {
                int blockStart = i;
                int statusByte = data[i];
                int temp_id = getIntegerFromByteArray(data, blockStart + 1);
                int headSize = data[i+5];
                if(isNodeInternal(statusByte)) {
                    RTreeRegion region = getTripleCoordsAsRegion(id, blockStart + headSize, data);

                    RTreeInternalNode obj = new RTreeInternalNode(temp_id, region);
                }
                objects.add(obj);
            };
            cachedDB.set(id % cacheSize, new RTreeInternalNode(id, objects));
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        } catch (IOException e) {
            System.out.println("Error reading file: " + fileName);
        }
    }

    private boolean isNodeValid(int statusByte){
        return (statusByte & 0x01) == 0x01;
    }

    private boolean isNodeInternal(int statusByte){
        return (statusByte & 0x02) == 0x02;
    }



    private int getNodeId(byte[] data){
        return getIntegerFromByteArray(data, 0);
    }
    private int getNodeSize(byte[] data){
        return getIntegerFromByteArray(data, 4);
    }

    private RTreeRegion getTripleCoordsAsRegion(int id, int blockStart, byte[] data){
        double x = getDoubleFromByteArray(data, blockStart );
        double y = getDoubleFromByteArray(data, blockStart + Double.BYTES);
        double z = getDoubleFromByteArray(data, blockStart + 2 * Double.BYTES);
        Coordinate c1 = new Coordinate(x, y, z);
        x = getDoubleFromByteArray(data, blockStart + 3 * Double.BYTES);
        y = getDoubleFromByteArray(data, blockStart + 4 * Double.BYTES);
        z = getDoubleFromByteArray(data, blockStart + 5 * Double.BYTES);
        Coordinate c2 = new Coordinate(x, y, z);
        x = getDoubleFromByteArray(data, blockStart + 6 * Double.BYTES);
        y = getDoubleFromByteArray(data, blockStart + 7 * Double.BYTES);
        z = getDoubleFromByteArray(data, blockStart + 8 * Double.BYTES);
        return new RTreeRegion(id, c1,c2,new Coordinate(x,y,z));
    }


    private Coordinate getCoordianateFromByteArray(byte[] data, int offset){
        double x = getDoubleFromByteArray(data, offset);
        double y = getDoubleFromByteArray(data, offset + Double.BYTES);
        double z = getDoubleFromByteArray(data, offset + 2 * Double.BYTES);
        return new Coordinate(x,y,z);
    }

    private int getIntegerFromByteArray(byte[] data, int offset){
        int temp = data[offset];
        temp |= data[offset + 1]>>8;
        temp |= data[offset + 2]>>16;
        temp |= data[offset + 3]>>24;
        return temp;
    }

    private double getDoubleFromByteArray(byte[] data, int offset){
        long temp = getIntegerFromByteArray(data, offset);
        temp |= ((long)getIntegerFromByteArray(data, offset+4) >> 32);
        return Double.longBitsToDouble(temp);
    }

    private RTreeObject getFromCache(int id, int index){

    }
}
