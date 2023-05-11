package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class CachedDatabaseTest {

    @Test
    void testGet() throws IOException {
        TreeConfig config = new TreeConfig((byte)2,(byte)3);
        PersistentCachedDatabase db = new PersistentCachedDatabase(10, config, new InMemorySequenceGenerator(), "testDb");
        int index = db.putNode(5, new RTreeNode(5,new RTreeRegion(new Coordinate(1),new Coordinate(2),new Coordinate(3))));
        db.getChildOfNodeByIndex(5, index);
    }

    @Test
    void put() {
    }

    @Test
    void delete() {
    }
}