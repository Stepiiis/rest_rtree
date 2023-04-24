package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;

public class PersistentCachedDatabase implements DatabaseInterface{

    private final RTreeNode[] cachedDB;
    private final int cacheSize;

    public PersistentCachedDatabase(int cacheSize){
        cachedDB = new RTreeNode[cacheSize];
        this.cacheSize=cacheSize;
    }

    @Override
    public RTreeNode getChildOfNodeByIndex(int id, int index) {
        RTreeNode node = getFromCache(id, index);
         return node == null ? getNodeFromFile(id).getChildByIndex(index) : node;
    }

    @Override
    public RTreeNode getNode(int id) {
        return getNodeFromFile(id);
    }

    @Override
    public void editBoundingBox(int id, RTreeRegion mbr) {
        throw new Error("Not implemented yet");
    }

    public int put(int id, RTreeRegion object) {
        throw new Error("Not implemented yet");
    }

    @Override
    public void deleteChildByIndex(int id, int index) {
        throw new Error("Not implemented yet");
    }

    @Override
    public void delete(int id) {
        throw new Error("Not implemented yet");
    }

    @Override
    public RTreeRegion[] getChildren(int id, int index) {
        throw new Error("not implemented yet");
    }

    private RTreeNode getNodeFromFile(int id){
        String fileName = "example.bin";
        File file = new File(fileName);
        RTreeNode foundNode = null;
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            ArrayList<RTreeNode> objects = new ArrayList<>();
            for(int i = Constants.INDEX_FILE_TOTAL_HEADER_SIZE; i< data.length; i += Constants.INDEX_FILE_NODE_SIZE) {
                if(!isNodeValid(data[i])) continue;
                int temp_id = getIntegerFromByteArray(data, i + CHILD_NODE_ID_POS);
                int headSize = data[i+CHILD_NODE_HEADER_SIZE_POS];
                RTreeRegion region = getAllCoordsInRegion(id, i + headSize, data);
                RTreeNode obj = new RTreeNode(temp_id, region); // Only contains bounding boxes of child nodes. Further loading of child nodes has to be done manually if needed.
                objects.add(obj);
            };
            foundNode = new RTreeNode(id, objects.toArray(new RTreeNode[0]));
            cachedDB[id % cacheSize] = foundNode;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        } catch (IOException e) {
            System.out.println("Error reading file: " + fileName);
        }
        return foundNode;
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

    private RTreeRegion getAllCoordsInRegion(int id, int blockStart, byte[] data){
        Coordinate[] coords = new Coordinate[CURR_DIMENSION];
        for(int u = 0; u< CURR_DIMENSION; ++u){
            double[] point = new double[CURR_DIMENSION];
            for(int i = 0; i < CURR_DIMENSION; ++i){
                point[i]= getDoubleFromByteArray(data, blockStart + i * Double.BYTES);
            }
            coords[u] = new Coordinate(point);
        }
        return new RTreeRegion(id, coords);
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

    private RTreeNode getFromCache(int id, int index){
        return cachedDB[id % cacheSize].getChildByIndex(index);
    }
}
