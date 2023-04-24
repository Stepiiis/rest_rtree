package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeObject;
import org.junit.jupiter.api.Test;

class CachedDatbaseTest {

    @Test
    void get() {
        CachedDatabase db = new CachedDatabase(10);
        int index = db.put(5, new RTreeObject(5,1,2,3));
        db.get(5, index);

    }

    @Test
    void put() {
    }

    @Test
    void delete() {
    }
}