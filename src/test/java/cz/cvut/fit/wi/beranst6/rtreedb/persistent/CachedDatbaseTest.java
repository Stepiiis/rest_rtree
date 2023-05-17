package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CachedDatabaseTest {

    @Test
    void testPutAndGet() {
        TreeConfig config = new TreeConfig((byte)2,(byte)2);
        PersistentCachedDatabase db = new PersistentCachedDatabase(10, config, new InMemorySequenceGenerator(), "testDb");
        RTreeNode node = new RTreeNode(5,new RTreeRegion(new Coordinate(1d,1d),new Coordinate(3f,3f)));
        db.putNode(node);
        RTreeNode child = new RTreeNode(6, new RTreeRegion(new Coordinate(1f,1f), new Coordinate(2f,2f)));
        RTreeNode child2= new RTreeNode(7, new RTreeRegion(new Coordinate(2f,2f), new Coordinate(2f,2f)));
        db.putChild(5, child);
        assertFalse(db.putChild(5, child));
        db.putChild(5, child2);
        assertEquals(child, db.getChild(5,0));
        assertEquals(child2, db.getChild(5,1));
//        assertEquals(child, db.getChild(5,1));
        db.clearDatabase(false);
    }

    @Test
    void put() {
    }

    @Test
    void delete() {
    }
}