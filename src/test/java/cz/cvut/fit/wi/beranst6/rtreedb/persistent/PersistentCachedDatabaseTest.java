package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.SequenceGeneratorInterface;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PersistentCachedDatabaseTest {

	@Test
	void testGetAndPutNode() {
		SequenceGeneratorInterface sequenceGenerator = new InMemorySequenceGenerator();
		TreeConfig config = new TreeConfig((byte)2,(byte)2);
		PersistentCachedDatabase db = new PersistentCachedDatabase(10, config, sequenceGenerator, "PersistenceDBTestDB");
		List<RTreeRegion> regions = List.of(
				new RTreeRegion(new Coordinate(11d,11d), new Coordinate(16d,11d)),
				new RTreeRegion(new Coordinate(13d,15d), new Coordinate(16d,20d)),
				new RTreeRegion(new Coordinate(20d,5d), new Coordinate(20d,5d)),
				new RTreeRegion(new Coordinate(10d,20d), new Coordinate(20d,20d)),
				new RTreeRegion(new Coordinate(7d,18d), new Coordinate(11d,18d))
		);
		List<RTreeNode> nodes = new ArrayList<>();
		for(RTreeRegion region : regions){
			nodes.add(new RTreeNode(db.getNextId(), region));
		}
		for(RTreeNode node : nodes){
			db.putNode(node);
		}
		for(RTreeNode node : nodes){
			assertEquals(node, db.getNode(node.getId()));
		}
		db.clearDatabase(false);
	}
}