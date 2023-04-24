package cz.cvut.fit.wi.beranst6.rtreedb.persistent;


import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

public interface DatabaseInterface {

    RTreeNode getChildOfNodeByIndex(int idNode, int childIndex); // returns object from database

    RTreeNode getNode(int id); // returns object from database

    void editBoundingBox(int idNode, RTreeRegion mbr); // sets object in database

    int put(int idNode, RTreeRegion object); // pusts object in database, returns index of the object in the node

    void deleteChildByIndex(int id, int index); // invalidates objects in database and edits MBR
    void delete(int id); // removes object from database

    RTreeRegion[] getChildren(int idNode, int childIndex); // returns children of the object

}
