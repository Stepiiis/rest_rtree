package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CachedDatabaseTest {

    @Test
    void testPutAndGet() {
        TreeConfig config = new TreeConfig((byte)2,(byte)2);
        IOMonitoring monitoring = new IOMonitoring();
        PersistentCachedDatabase db = new PersistentCachedDatabase(10, config, new InMemorySequenceGenerator(), "testDb",monitoring);
        RTreeNode node = new RTreeNode(5,new RTreeRegion(new Coordinate(1d,1d),new Coordinate(3d,3d)));
        db.putNode(node);
        RTreeNode child = new RTreeNode(6, new RTreeRegion(new Coordinate(1d,1d), new Coordinate(2d,2d)));
        RTreeNode child2= new RTreeNode(7, new RTreeRegion(new Coordinate(2d,2d), new Coordinate(2d,2d)));
        db.putChild(5, child);
        assertFalse(db.putChild(5, child));
        db.putChild(5, child2);
        assertEquals(child, db.getChild(5,0));
        assertEquals(child2, db.getChild(5,1));
//        assertEquals(child, db.getChild(5,1));
        db.clearDatabase(false, false);
    }

    @Test
    void put() {
    }

    @Test
    void delete() {
    }
}