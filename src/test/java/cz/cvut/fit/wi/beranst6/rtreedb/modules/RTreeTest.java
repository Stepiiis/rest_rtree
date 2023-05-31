package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RTreeTest {

	TreeConfig config = new TreeConfig((byte)2,(byte)2);
	private PersistentCachedDatabase db;


	@BeforeEach
	void setup(){
		db = new PersistentCachedDatabase(10, config, new InMemorySequenceGenerator(), "testDb");
		db.clearDatabase(true);
	}

	@AfterEach()
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
// 		long finish = System.currentTimeMillis();
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

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0,0), 5, kNNType.BRANCH_AND_BOUND);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 4, kNNType.BRANCH_AND_BOUND);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 3, kNNType.BRANCH_AND_BOUND);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 2, kNNType.BRANCH_AND_BOUND);
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

	@Test
	void testInsert3Nodes(){
		TreeConfig configMulti = new TreeConfig((byte)3,(byte)2);
		RTree tree = new RTree(configMulti, db);
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

		RTreeRegion allBounded = new RTreeRegion(new Coordinate(0, 0), new Coordinate(20, 20));
		List<RTreeNode> res = tree.rangeQuery(allBounded);
		assertEquals(5, res.size());

		RTreeRegion[] sorted = expRes.stream().sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		RTreeRegion[] sortedRes = res.stream().map(RTreeNode::getMbr).sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		assertArrayEquals(sorted,sortedRes);

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0,0), 5, kNNType.BRANCH_AND_BOUND);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 4, kNNType.BRANCH_AND_BOUND);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 3, kNNType.BRANCH_AND_BOUND);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 2, kNNType.BRANCH_AND_BOUND);
		assertEquals(2, kNNRes.size());

		db.clearDatabase(false);
	}

	@Test
	void test3Ddata(){
		TreeConfig configMulti = new TreeConfig((byte)3,(byte)3);
		PersistentCachedDatabase  db3d = new PersistentCachedDatabase(10, configMulti, new InMemorySequenceGenerator(), "testDb");
		RTree tree = new RTree(configMulti, db3d);
		List<RTreeRegion> expRes = List.of(
				new RTreeRegion(new Coordinate(11,11,11), new Coordinate(16,11,11)),
				new RTreeRegion(new Coordinate(13,15,15), new Coordinate(16,20,20)),
				new RTreeRegion(new Coordinate(20,5,5), new Coordinate(20,5,5)),
				new RTreeRegion(new Coordinate(10,20,20), new Coordinate(20,20,20)),
				new RTreeRegion(new Coordinate(7,18,18), new Coordinate(11,18,18))
		);
		for(RTreeRegion region : expRes){
			tree.insert(region);
		}

		for(RTreeRegion region : expRes) {
			assertEquals(1,tree.search(region).size());
			assertEquals(region, tree.search(region).get(0).getMbr());
		}

		RTreeRegion allBounded = new RTreeRegion(new Coordinate(0, 0), new Coordinate(20, 20));
		List<RTreeNode> res = tree.rangeQuery(allBounded);
		assertEquals(5, res.size());

		RTreeRegion[] sorted = expRes.stream().sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		RTreeRegion[] sortedRes = res.stream().map(RTreeNode::getMbr).sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		assertArrayEquals(sorted,sortedRes);

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0,0), 5, kNNType.BRANCH_AND_BOUND);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 4, kNNType.BRANCH_AND_BOUND);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 3, kNNType.BRANCH_AND_BOUND);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0,0), 2, kNNType.BRANCH_AND_BOUND);
		assertEquals(2, kNNRes.size());

		RTreeRegion allBound = new RTreeRegion(new Coordinate(0, 0), new Coordinate(20, 20));

		for(int i = 0 ; i < expRes.size(); i++){
			tree.delete(expRes.get(i));
			List<RTreeNode> resRange = tree.rangeQuery(allBound);
			assertEquals(expRes.size()-(i+1), resRange.size());
		}

		db3d.clearDatabase(false);
	}


	@Test
	void performanceTest(){
		TreeConfig configMulti = new TreeConfig((byte)30,(byte)2);
		PersistentCachedDatabase  dbMulti = new PersistentCachedDatabase(20000, configMulti, new InMemorySequenceGenerator(), "testDb");
		RTree tree = new RTree(configMulti, dbMulti);

		List<RTreeRegion> objects = FileHandlingUtil.loadObjectsIntoArray("src/test/resources/performance_2D",2);

		int queryCnt = objects.size() / 10;
		Random rand = new Random(System.currentTimeMillis());
		long start = System.currentTimeMillis();
		for( int i = 0 ; i <  queryCnt ; ++i){
			int index = rand.nextInt(objects.size());
			List<RTreeRegion> res = linearFindFittingRegion(objects, objects.get(index));
			assertNotEquals(0,res.size());
//			assertEquals(objects.get(i%objects.size()), res);
		}
		long finish = System.currentTimeMillis();
		long timeElapsed=finish-start;
		System.out.println("Searching " + queryCnt + " linear search queries took " + timeElapsed + " ms");



		start = System.currentTimeMillis();
		for(RTreeRegion region : objects){
			tree.insert(region);
		}
 		finish = System.currentTimeMillis();
		timeElapsed = finish - start;
		System.out.println("Inserting "+ objects.size() +" elements took " + timeElapsed + " ms"); // 10000 = 1,7s, 100000 = 378s

		start = System.currentTimeMillis();
		for( int i = 0 ; i <  queryCnt ; ++i){
			List<RTreeNode> res = tree.search(objects.get(i%objects.size()));
			assert(res.size() > 0);
		}
		finish = System.currentTimeMillis();
		timeElapsed=finish-start;
		System.out.println("Searching " + queryCnt + " RTree range queries took " + timeElapsed + " ms");



	}


	public List<RTreeRegion> linearFindFittingRegion(List<RTreeRegion> nodes, RTreeRegion region){
		List<RTreeRegion> found = new ArrayList<>();
		for(RTreeRegion node : nodes){
			if(node.canFit(region))
				found.add(node);
		}
		return found;
	}
}