package cz.cvut.fit.wi.beranst6.rtreedb.persistent;


import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

import java.util.List;
import java.util.Set;

public interface DatabaseInterface {
    /**
     * returns object from database
     */
    RTreeNode getChildOfNodeByIndex(int idNode, int childIndex);
    /**
     * returns object from database
     */
    RTreeNode getNode(int id);
    /**
     * updates MBR of the node
     */
    void updateBoundingBox(int idNode, Set<Integer> changedChildren);
    /**
     *  puts object in database, returns index of the object in the node
     */
    int putNode(int idNode, RTreeNode object);
    int putChild(int idNode, RTreeNode child);
    /**
     * if db persistent and cached, then invalidate records. If db is not persistent, then it deletes the object from the node, UpdateMBR nonetheless
     */
    void deleteChildByIndex(int id, int index);
    /**
     * removes object from database
     */
    void delete(int id);
    /**
     * returns children of the object
     */
    RTreeRegion getChild(int idNode, int childIndex);
    List<RTreeNode> getAllChildren(int idNode);
    void setChildren(int id, RTreeNode... children);
    boolean isLeaf(int id);
    int getNextId();
}
