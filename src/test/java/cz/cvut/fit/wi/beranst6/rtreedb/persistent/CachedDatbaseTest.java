package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import org.junit.jupiter.api.Test;

class CachedDatbaseTest {

    @Test
    void get() {
        int prevMaxDim = Constants.CURR_DIMENSION;
        Constants.CURR_DIMENSION = 3;
        PersistentCachedDatabase db = new PersistentCachedDatabase(10);
        int index = db.put(5, new RTreeRegion(5,new Coordinate(1),new Coordinate(2),new Coordinate(3)));
        db.getChildOfNodeByIndex(5, index);
        Constants.CURR_DIMENSION = prevMaxDim;

    }

    @Test
    void put() {
    }

    @Test
    void delete() {
    }
}