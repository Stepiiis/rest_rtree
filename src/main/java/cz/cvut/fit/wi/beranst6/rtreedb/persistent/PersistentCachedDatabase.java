package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;
import cz.cvut.fit.wi.beranst6.rtreedb.utils.IndexRecordInvalidException;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Logger;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;
import static cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil.*;

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
        RTreeNode node = getChildFromCache(id, index);
        return node == null ? getNodeFromFile(id).getChildByIndex(index) : node;
    }

    @Override
    public RTreeNode[] getAllChildren(int id) {
        RTreeNode node = getNodeFromCache(id);
        if(node.getId() != id || node == null)
            return getNodeFromFile(id).getChildren();
        return node.getChildren();
    }

    public String getNodeFileName(int id){
        return "index_"+CURR_DIMENSION+"_"+id+".bin";
    }

    public void saveToCache(int id, RTreeNode node){
        int index = id % cacheSize;
        cachedDB[index] = node;
    }

    public RTreeNode getNodeFromFile(int id){
        String fileName =  getNodeFileName(id);
        File file = new File(fileName);
        RTreeNode foundNode = null;
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            ArrayList<RTreeNode> children = new ArrayList<>();
            for(int i = Constants.INDEX_FILE_TOTAL_HEADER_SIZE ; i< data.length ; i += Constants.INDEX_FILE_NODE_SIZE) {
                readAndAddChild(data, children, i); // Only contains bounding boxes of child nodes alog with their ids.
                                                    // Further loading of child nodes has to be done manually if needed
            };

            foundNode = new RTreeNode(id, children.toArray(new RTreeNode[0]));
            saveToCache(id, foundNode);
            cachedDB[id % cacheSize] = foundNode;
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + fileName);
        } catch (IOException e) {
            System.out.println("Error reading file: " + fileName);
        }
        return foundNode;
    }

    public static void readAndAddChild(byte[] data, Collection<RTreeNode> children, int readStartPos){
        if(isNodeInvalid(data[readStartPos])) return;
        int temp_id = getIntegerFromByteArray(data, readStartPos + CHILD_NODE_ID_POS);
        int headSize = data[readStartPos+CHILD_NODE_HEADER_SIZE_POS];
        RTreeRegion region = getAllCoordsInRegion(temp_id, readStartPos + headSize, data);
        children.add(new RTreeNode(temp_id, region));}

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
    public RTreeRegion[] getChild(int id, int index) {
        throw new Error("not implemented yet");
    }

    // updates cache and returns the node

    public static boolean isNodeInvalid(int statusByte){
        return (statusByte & 0x01) != 0x01;
    }

    protected static boolean isNodeInternal(int statusByte){
        return (statusByte & 0x02) == 0x02;
    }


    private RTreeNode getChildFromCache(int id, int index){
        return cachedDB[id % cacheSize].getChildByIndex(index);
    }

    private RTreeNode getNodeFromCache(int id){
        return cachedDB[id % cacheSize];
    }


}
