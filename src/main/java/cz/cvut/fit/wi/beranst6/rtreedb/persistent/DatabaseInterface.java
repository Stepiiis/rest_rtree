package cz.cvut.fit.wi.beranst6.rtreedb.persistent;


import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

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
    void editBoundingBox(int idNode);
    /**
     *  puts object in database, returns index of the object in the node
     */
    int put(int idNode, RTreeRegion object);
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
    RTreeRegion[] getChildren(int idNode, int childIndex);

}
