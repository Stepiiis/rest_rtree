package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;
import cz.cvut.fit.wi.beranst6.rtreedb.utils.IndexRecordInvalidException;

import javax.sound.midi.Sequence;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;
import static cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil.*;

public class PersistentCachedDatabase implements DatabaseInterface{

    Logger LOGG = Logger.getLogger(PersistentCachedDatabase.class.getName());
    private final RTreeNode[] cachedDB;
    private final int cacheSize;
    private final TreeConfig config;
    private final SequenceGeneratorInterface sequenceGen;
    private final String indexFolder;

    public PersistentCachedDatabase(int cacheSize, TreeConfig config, SequenceGeneratorInterface sequenceGen, String indexFolder) throws IOException {
        cachedDB = new RTreeNode[cacheSize];
        this.cacheSize=cacheSize;
        this.config = config;
        this.sequenceGen = sequenceGen;
        this.indexFolder = indexFolder;
        try {
            Files.createDirectories(Paths.get(indexFolder));
        }catch(IOException e) {
            LOGG.severe("Could not create directory for index files");
            throw e;
        }
    }

    @Override
    public RTreeNode getChildOfNodeByIndex(int id, int index) {
        RTreeNode node = getNodeFromCache(id);
        if (node.getId() == id)
            node = node.getChildByIndex(index);
        else node = null;
        return (node == null ? getNodeFromFile(id).getChildByIndex(index) : node);
    }

    @Override
    public List<RTreeNode> getAllChildren(int id) {
        RTreeNode node = getNodeFromCache(id);
        if(node.getId() != id || node == null)
            return getNodeFromFile(id).getChildren();
        return node.getChildren();
    }

    @Override
    public void setChildren(int id, RTreeNode... children) {
        throw new Error("Not implemented yet");
    }

    public String getNodeFileName(int id){
        return indexFolder+"index_"+config.getDimension()+"_"+id+".bin";
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
            for(int i = config.getTotalIndexHeaderSize(); i< data.length ; i += config.getIndexNodeSize()) {
                readAndAddChild(data, children, i, config); // Only contains bounding boxes of child nodes alog with their ids.
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

    public static void readAndAddChild(byte[] data, Collection<RTreeNode> children, int readStartPos, TreeConfig config){
        if(isNodeInvalid(data[readStartPos])) return;
        int temp_id = getIntegerFromByteArray(data, readStartPos + CHILD_NODE_ID_POS);
        int headSize = data[readStartPos+CHILD_NODE_HEADER_SIZE_POS];
        RTreeRegion region = getAllCoordsInRegion(temp_id, readStartPos + headSize, data, config);
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
            LOGG.warning("Index file invalid. id:" +id + " dim: " + config.getDimension());
        }
    }



    // assumes that all child elements which have been changed are tagged
    public void updateNodeInDb(RTreeNode node, Set<Integer> changedChildrenIds) throws IndexRecordInvalidException {
        String fileName = getNodeFileName(node.getId());
        File file = new File(fileName);
        byte[] fileData = new byte[(int) file.length() - config.getTotalIndexHeaderSize()]; // assumes standard header size
        IndexRecord record = new IndexRecord();

        // read header from file
        try (BufferedInputStream fis = new BufferedInputStream(new FileInputStream(file))) {
            if (!loadRecordHeaderFromFile(record, fis, config))
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
            putAllCordsFromCoordArray(record.getHeaderSize(), node.getMbrArr(), fos, config);
            final int totalHeaderOffset = record.getHeaderSize()+record.getDimension()*config.getCoordinateSize();
            final int childNodeHeaderSize = fileData[totalHeaderOffset + CHILD_NODE_HEADER_SIZE_POS];
            for (int i = totalHeaderOffset; i < fileData.length; i += config.getIndexNodeSize() ) {
                int childId = getIntegerFromByteArray(fileData, i + CHILD_NODE_ID_POS);
                if (!changedChildrenIds.contains(childId)) {
                    continue;
                }
                changedChildrenIds.remove(childId);
                putAllCordsFromCoordArray(childNodeHeaderSize, node.getChildByIndex(childId).getMbrArr(), fos, config);
            }
            if(changedChildrenIds.size() > 0 ){
                for(int childId : changedChildrenIds){
                    int offsetFromStart = record.getHeaderSize()+record.getNodeCount()*(record.getNodeSize() + childNodeHeaderSize);
                    putAllCordsFromCoordArray(offsetFromStart, node.getChildByIndex(childId).getMbrArr(), fos, config);
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
        String fileName = "index_"+config.getDimension()+"_"+node.getId()+".bin";
        try(BufferedOutputStream fos = new BufferedOutputStream (new FileOutputStream(fileName,true))){
            writeHeaderOfFile(node,fos, config);
            for(int i=0 ; i < node.getChildren().size() ; i++)
                writeChildNodeToFile(INDEX_HEADER_SIZE + (config.getIndexNodeSize() * i), node.getChildren().get(i), fos, config);
        } catch (FileNotFoundException e) {
            LOGG.severe("File could not be opened or created: " + fileName);
            return;
        } catch (IOException e) {
            LOGG.severe("Error writing to file during creation rutine: " + fileName);
        }
    }




    public int putNode(int id, RTreeNode object) {
        throw new Error("Not implemented yet");
    }

    @Override
    public int putChild(int idNode, RTreeNode child) {
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
    public RTreeRegion getChild(int id, int index) {
        throw new Error("not implemented yet");
    }

    public boolean isLeaf(int id){
        try{getNodeFromFile(id);}
        catch(Exception e){return false;};
        return true;
    }

    @Override
    public int getNextId() {
        return 0;
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
