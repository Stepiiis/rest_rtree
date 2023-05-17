package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RTreeTest {

	TreeConfig config = new TreeConfig((byte)2,(byte)2);
	private PersistentCachedDatabase db;


	@BeforeAll
	void setup(){
		db = new PersistentCachedDatabase(10, config, new InMemorySequenceGenerator(), "testDb");
		db.clearDatabase(true);
	}

	@AfterAll()
	void cleanUp(){
		db.clearDatabase(false);
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
//		long start = System.currentTimeMillis();
		for(RTreeRegion region : expRes){
			tree.insert(region);
		}
//		long finish = System.currentTimeMillis();
//		long timeElapsed = finish - start;
//		System.out.println("Inserting "+ expRes.size() +" elements took " + timeElapsed + " ms");
//		start = System.currentTimeMillis();
		for(RTreeRegion region : expRes) {
			assertEquals(1,tree.search(region).size());
			assertEquals(region, tree.search(region).get(0).getMbr());
		}
//		finish = System.currentTimeMillis();
//		timeElapsed = finish - start;
//		System.out.println("Searching "+ expRes.size()*2 +" elements took " + timeElapsed + " ms");


		RTreeRegion allBounded = new RTreeRegion(new Coordinate(0, 0), new Coordinate(20, 20));
		List<RTreeNode> res = tree.rangeQuery(allBounded);
		assertEquals(5, res.size());

		RTreeRegion[] sorted = expRes.stream().sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		RTreeRegion[] sortedRes = res.stream().map(RTreeNode::getMbr).sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		assertArrayEquals(sorted,sortedRes);

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0,0), 5);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 4);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 3);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 2);
		assertEquals(2, kNNRes.size());

		db.clearDatabase(false);
	}

	@Test
	void testDelete(){
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
		RTreeRegion allBound = new RTreeRegion(new Coordinate(0, 0), new Coordinate(20, 20));

		for(int i = 0 ; i < expRes.size(); i++){
			tree.delete(expRes.get(i));
			List<RTreeNode> res = tree.rangeQuery(allBound);
			assertEquals(expRes.size()-(i+1), res.size());
		}
	}

}