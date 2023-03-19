package cz.cvut.fit.wi.beranst6.rtreedb.persistent;


import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeObject;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

public interface DatabaseInterface {

    RTreeRegion get(int id, int index); // returns object from database

    int put(int id, RTreeObject object); // pusts object in database, returns index of the object

    void delete(int id, int index); // invalidates objects in database

}
