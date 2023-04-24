package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.utils.IndexRecordInvalidException;

import javax.management.modelmbean.InvalidTargetObjectTypeException;
import java.io.*;
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
    public void editBoundingBox(int id) {
        RTreeNode node = getNodeFromFile(id);
        node.updateMBR();
        try {
            updateNodeInDb(node);
        }catch(IndexRecordInvalidException e){
            System.out.println("index file invalid");
        }
    }

    public boolean loadRecordHeaderFromFile(IndexRecord record, FileInputStream fis) throws IOException {
        byte[] data = new byte[Constants.INDEX_FILE_TOTAL_HEADER_SIZE];
        fis.read(data);
        record.setNodeCount(getIntegerFromByteArray(data, INDEX_HEADER_NODE_COUNT_POS));
        record.setStatusByte(data[INDEX_HEADER_STATUS_POS]);
        if(!isNodeValid(record.getStatusByte()))
            return false;
        record.setHeaderSize(data[INDEX_HEADER_SIZE_POS]);
        record.setDimension(data[INDEX_HEADER_DIMENSION_POS]);
        record.setCapacity(data[INDEX_HEADER_NODE_CAPACITY_POS]);
        record.setNodeSize(getIntegerFromByteArray(data, INDEX_HEADER_NODE_SIZE_POS));
        record.setId(getIntegerFromByteArray(data,INDEX_HEADER_ROOT_NODE_ID_POS));
        record.setParentId(getIntegerFromByteArray(data,INDEX_HEADER_PARENT_NODE_ID_POS));
        RTreeRegion mbr = getAllCoordsInRegion(record.getId(), record.getHeaderSize(), data);
        record.setMbr(mbr);
        return true;
    }

    public void updateNodeInDb(RTreeNode node) throws IndexRecordInvalidException {
        String fileName = "index_"+CURR_DIMENSION+"_"+node.getId()+".bin";
        File file = new File(fileName);
        try (FileInputStream fis = new FileInputStream(file)) {
            IndexRecord record = new IndexRecord();
            if(!loadRecordHeaderFromFile(record,fis))
                throw new IndexRecordInvalidException("index stored in "  + fileName + " is invalid.");
            // todo write new bounding box in header
            byte[] data = new byte[(int) file.length() - INDEX_FILE_TOTAL_HEADER_SIZE];
            fis.read(data);
            for(int i = Constants.INDEX_FILE_TOTAL_HEADER_SIZE; i < data.length; i += Constants.INDEX_FILE_NODE_SIZE) {
                int childId = getIntegerFromByteArray(data, i + CHILD_NODE_ID_POS);
                if(childId != node.getId())
                    continue;
                int headSize = data[i+CHILD_NODE_HEADER_SIZE_POS];
                try(FileOutputStream fos = new FileOutputStream(file)){
                    // todo write node
                }
            };
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        } catch (IOException e) {
            System.out.println("Error reading file: " + fileName);
        }
        cachedDB[node.getId() % cacheSize] = node;
    }


    public int put(int id, RTreeRegion object) {
        throw new Error("Not implemented yet");
    }

    /**
     * if db persistent and cached, then invalidate records
     * */
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

    // updates cache and returns the node
    private RTreeNode getNodeFromFile(int id){
        String fileName = "index_"+CURR_DIMENSION+"_"+id+".bin";
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

    private void putAllCordsFromRegion(int blockStart, RTreeNode node, FileOutputStream fos) throws IOException {
        Coordinate[] coords = node.getMbr().getBoundingRect();
        for(int u = 0; u< CURR_DIMENSION; ++u){
            for(int i = 0; i < CURR_DIMENSION; ++i){
                fos.write(getByteArrFromDouble(coords[u].getCoordinates()[i]), blockStart + u * i * COORDINATE_SIZE, Double.BYTES);
            }
        }
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

    // assumes big endian
    private int getIntegerFromByteArray(byte[] data, int offset){
        int temp = data[offset];
        temp |= data[offset + 1]>>8;
        temp |= data[offset + 2]>>16;
        temp |= data[offset + 3]>>24;
        return temp;
    }

    private byte getByteFromLong(long data, int offset){
        return (byte) ((data & ( 0xFF << offset*8))>>offset*8);
    }

    // uses big endian, last byte represents MSB of data
    private byte[] getByteArrFromInteger(int data){
        byte[] arr = new byte[4];
        for( int i = 0; i < 4 ; i ++)
            arr[i] = getByteFromLong(data, i);
        return arr;
    }
    private byte[] getByteArrFromDouble(double data){
        long doubleBits = Double.doubleToLongBits(data);
        byte[] arr = new byte[8];
        for( int i = 0; i < 8 ; i ++)
            arr[i] = getByteFromLong(doubleBits, i);
        return arr;
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
