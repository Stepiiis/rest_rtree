package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.utils.IndexRecordInvalidException;

import java.io.*;
import java.util.ArrayList;
import java.util.Set;
import java.util.logging.Logger;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;

public class PersistentCachedDatabase implements DatabaseInterface{

    Logger LOGG = Logger.getLogger(PersistentCachedDatabase.class.getName());
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
    public void updateBoundingBox(int id, Set<Integer> changedChildren) {
        RTreeNode node = getNodeFromFile(id);
        try {
            updateNodeInDb(node,changedChildren);
        }catch(IndexRecordInvalidException e){
            LOGG.warning("Index file invalid. id:" +id + " dim: " + CURR_DIMENSION);
        }
    }

    public boolean loadRecordHeaderFromFile(IndexRecord record, BufferedInputStream fis) throws IOException {
        byte[] data = new byte[Constants.INDEX_FILE_TOTAL_HEADER_SIZE];
        fis.read(data);
        record.setNodeCount(getIntegerFromByteArray(data, INDEX_HEADER_NODE_COUNT_POS));
        record.setStatusByte(data[INDEX_HEADER_STATUS_POS]);
        if(isNodeInvalid(record.getStatusByte()))
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

    // assumes that all child elements which have been changed are tagged
    public void updateNodeInDb(RTreeNode node, Set<Integer> changedChildrenIds) throws IndexRecordInvalidException {
        String fileName = "index_"+CURR_DIMENSION+"_"+node.getId()+".bin";
        File file = new File(fileName);
        byte[] fileData = new byte[(int) file.length() - INDEX_FILE_TOTAL_HEADER_SIZE]; // assumes standard header size
        IndexRecord record = new IndexRecord();

        // read header from file
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
            if (!loadRecordHeaderFromFile(record, fis))
                throw new IndexRecordInvalidException("index stored in " + fileName + " is invalid.");
            record.setMbr(node.getMbr());
            fis.read(fileData);
        } catch (FileNotFoundException e) {
            LOGG.info("File not found: " + fileName);
            createNewRecord(node);
            cachedDB[node.getId() % cacheSize] = node;
            return;
        } catch (IOException e) {
            LOGG.severe("Error reading file: " + fileName);
        }

        try(BufferedOutputStream fos = new BufferedOutputStream (new FileOutputStream(file))){
            putAllCordsFromCoordArray(record.getHeaderSize(), node.getMbrArr(), fos);
            final int totalHeaderOffset = record.getHeaderSize()+record.getDimension()*COORDINATE_SIZE;
            final int childNodeHeaderSize = fileData[totalHeaderOffset + CHILD_NODE_HEADER_SIZE_POS];
            for (int i = totalHeaderOffset; i < fileData.length; i += Constants.INDEX_FILE_NODE_SIZE) {
                int childId = getIntegerFromByteArray(fileData, i + CHILD_NODE_ID_POS);
                if (!changedChildrenIds.contains(childId)) {
                    continue;
                }
                changedChildrenIds.remove(childId);
                putAllCordsFromCoordArray(childNodeHeaderSize, node.getChildByIndex(childId).getMbrArr(), fos);
            }
            if(changedChildrenIds.size() > 0 ){
                for(int childId : changedChildrenIds){
                    int offsetFromStart = record.getHeaderSize()+record.getNodeCount()*(record.getNodeSize() + childNodeHeaderSize);
                    putAllCordsFromCoordArray(offsetFromStart, node.getChildByIndex(childId).getMbrArr(), fos);
                    record.setNodeCount(record.getNodeCount()+1);
                }
                putNodeCount(record.getNodeCount(), fos);
            }
        } catch (FileNotFoundException e) {
            LOGG.severe("File not found: " + fileName);
            return;
        } catch (IOException e) {
            LOGG.severe("Error writing to file: " + fileName);
        }
        cachedDB[node.getId() % cacheSize] = node;
    }

    private void createNewRecord(RTreeNode node) {
        String fileName = "index_"+CURR_DIMENSION+"_"+node.getId()+".bin";
        try(BufferedOutputStream fos = new BufferedOutputStream (new FileOutputStream(fileName,true))){
            writeHeaderOfFile(node,fos);
            for(int i=0 ; i < node.getChildren().length ; i++)
                writeChildNodeToFile(INDEX_HEADER_SIZE + (INDEX_FILE_NODE_SIZE * i), node.getChildren()[i], fos);
        } catch (FileNotFoundException e) {
            LOGG.severe("File could not be opened or created: " + fileName);
            return;
        } catch (IOException e) {
            LOGG.severe("Error writing to file during creation rutine: " + fileName);
        }
    }

    private void writeChildNodeToFile(int offset, RTreeNode child, BufferedOutputStream fos) throws IOException {
        fos.write(new byte[] {(byte)1}, offset + CHILD_NODE_STATUS_POS, CHILD_NODE_STATUS_BYTES);
        fos.write(getByteArrayFromInteger(child.getId()), offset + CHILD_NODE_ID_POS, CHILD_NODE_ID_SIZE);
        fos.write(new byte[] { CHILD_NODE_HEADER_SIZE}, offset + CHILD_NODE_HEADER_SIZE_POS, 1);
        putAllCordsFromCoordArray(offset+CHILD_NODE_HEADER_SIZE, child.getMbrArr(), fos);
    }

    private void writeHeaderOfFile(RTreeNode node, BufferedOutputStream fos) throws IOException{
        fos.write(INDEX_HEADER_MAGIC, INDEX_HEADER_MAGIC_POS, INDEX_HEADER_MAGIC_BYTES);
        fos.write(getByteArrayFromInteger(node.getChildren().length), INDEX_HEADER_NODE_COUNT_POS, INDEX_HEADER_NODE_COUNT_BYTES);
        fos.write(new byte[] {(byte)1}, INDEX_HEADER_STATUS_POS, INDEX_HEADER_STATUS_BYTE_BYTES);
        fos.write(new byte[] {INDEX_HEADER_SIZE}, INDEX_HEADER_SIZE_POS, INDEX_HEADER_SIZE_BYTES);
        fos.write(new byte[] {CURR_DIMENSION}, INDEX_HEADER_DIMENSION_POS, INDEX_HEADER_DIMENSION_BYTES);
        fos.write(new byte[] {CURR_MAX_CAPACITY}, INDEX_HEADER_NODE_CAPACITY_POS, INDEX_HEADER_NODE_CAPACITY_BYTES);
        fos.write(getByteArrayFromInteger(INDEX_FILE_NODE_SIZE), INDEX_HEADER_NODE_SIZE_POS, INDEX_HEADER_NODE_SIZE_BYTES);
        fos.write(getByteArrayFromInteger(node.getId()), INDEX_HEADER_ROOT_NODE_ID_POS, INDEX_HEADER_ROOT_NODE_ID_BYTES);
        fos.write(getByteArrayFromInteger(node.getParentId()), INDEX_HEADER_PARENT_NODE_ID_POS, INDEX_HEADER_PARENT_NODE_ID_BYTES);
    }

    private void putNodeCount(int nodeCount, BufferedOutputStream fos) throws IOException {
        fos.write(getByteArrayFromInteger(nodeCount), INDEX_HEADER_NODE_COUNT_POS, INDEX_HEADER_NODE_COUNT_BYTES);
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
                if(isNodeInvalid(data[i])) continue;
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

    private boolean isNodeInvalid(int statusByte){
        return (statusByte & 0x01) != 0x01;
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

    private void putAllCordsFromCoordArray(int blockStart, Coordinate[] coords, BufferedOutputStream fos) throws IOException {
        for(int u = 0; u < CURR_DIMENSION; ++u){
           putCoordinate(blockStart + u * COORDINATE_SIZE, coords[u], fos);
        }
    }

    private void putCoordinate(int blockStart, Coordinate coord, BufferedOutputStream fos) throws IOException {
        for(int i = 0; i < CURR_DIMENSION; ++i){
            fos.write(getByteArrayFromDouble(coord.getCoordinates()[i]), blockStart + i * COORDINATE_SIZE, Double.BYTES);
        }
    }

    private RTreeRegion getAllCoordsInRegion(int id, int blockStart, byte[] data){
        Coordinate[] coords = new Coordinate[CURR_DIMENSION];
        for(int u = 0; u< CURR_DIMENSION; ++u){
            double[] point = new double[CURR_DIMENSION];
            for(int i = 0; i < CURR_DIMENSION; ++i){
                point[i] = getDoubleFromByteArray(data, blockStart + i * Double.BYTES);
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
    private byte[] getByteArrayFromInteger(int data){
        byte[] arr = new byte[4];
        for( int i = 0; i < 4 ; i ++)
            arr[i] = getByteFromLong(data, i);
        return arr;
    }
    private byte[] getByteArrayFromDouble(double data){
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
