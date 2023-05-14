package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RTreeTest {

	TreeConfig config = new TreeConfig((byte)2,(byte)2);
	private PersistentCachedDatabase db;


	@BeforeAll
	void setup(){
		db = new PersistentCachedDatabase(10, config, new InMemorySequenceGenerator(), "testDb");
	}

	@AfterAll()
	void cleanUp(){
//		db.clearDatabase();
	}

	@Test
	void testInsert() {
		RTree tree = new RTree(config, db);
		List<RTreeRegion> expRes = List.of(
				new RTreeRegion(new Coordinate(11,11), new Coordinate(16,11)),
				new RTreeRegion(new Coordinate(13,15), new Coordinate(16,20)),
				new RTreeRegion(new Coordinate(20,5), new Coordinate(20,5)),
				new RTreeRegion(new Coordinate(10,20), new Coordinate(20,20)),
				new RTreeRegion(new Coordinate(7,18), new Coordinate(11,18))
		);
		for(RTreeRegion region : expRes){
			tree.insert(region);
		}
		for(RTreeRegion region : expRes) {
			assertEquals(1,tree.search(region).size());
			assertEquals(region, tree.search(region).get(0).getMbr());
		}
		db.clearDatabase();
	}

	@Test
	void testRead(){
		db.getNode(1);
	}

}