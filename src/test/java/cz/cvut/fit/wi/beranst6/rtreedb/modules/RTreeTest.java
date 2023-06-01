package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.IOMonitoring;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.LinearDB;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.PersistentSequenceGenerator;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.SequenceGeneratorInterface;
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
	private SequenceGeneratorInterface sequence;


	@BeforeEach
	void setup(){
		 sequence = new PersistentSequenceGenerator("testDb");
		 IOMonitoring monitoring = new IOMonitoring();
		db = new PersistentCachedDatabase(10, config, sequence, "testDb", monitoring);
		db.clearDatabase(true, true);
	}

	@AfterEach()
	void cleanUp(){
		db.clearDatabase(false, false);
	}

	@Test
	void testInsert() {
		RTree tree = new RTree(config, db);
		List<RTreeRegion> expRes = List.of(
				new RTreeRegion(new Coordinate(11d,11d), new Coordinate(16d,11d)),
				new RTreeRegion(new Coordinate(13d,15d), new Coordinate(16d,20d)),
				new RTreeRegion(new Coordinate(20d,5d), new Coordinate(20d,5d)),
				new RTreeRegion(new Coordinate(10d,20d), new Coordinate(20d,20d)),
				new RTreeRegion(new Coordinate(7d,18d), new Coordinate(11d,18d))
		);
		sequence.getCurrentValue();
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


		RTreeRegion allBounded = new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(20d, 20d));
		List<RTreeNode> res = tree.rangeQuery(allBounded);
		assertEquals(5, res.size());

		RTreeRegion[] sorted = expRes.stream().sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		RTreeRegion[] sortedRes = res.stream().map(RTreeNode::getMbr).sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		assertArrayEquals(sorted,sortedRes);

		res = tree.rangeQuery(new RTreeRegion(new Coordinate(10d,10d), new Coordinate(20d,20d)));
		assertEquals(2, res.size());

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0d,0d), 5, kNNType.BETTER_BFS);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 4, kNNType.BETTER_BFS);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 3, kNNType.BETTER_BFS);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 2, kNNType.BETTER_BFS);
		assertEquals(2, kNNRes.size());

	}

	@Test
	void testDelete(){
		RTree tree = new RTree(config, db);
		List<RTreeRegion> expRes = List.of(
				new RTreeRegion(new Coordinate(11d,11d), new Coordinate(16d,11d)),
				new RTreeRegion(new Coordinate(13d,15d), new Coordinate(16d,20d)),
				new RTreeRegion(new Coordinate(20d,5d), new Coordinate(20d,5d)),
				new RTreeRegion(new Coordinate(10d,20d), new Coordinate(20d,20d)),
				new RTreeRegion(new Coordinate(7d,18d), new Coordinate(11d,18d))
		);
		for(RTreeRegion region : expRes){
			tree.insert(region);
		}
		RTreeRegion allBound = new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(20d, 20d));

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
				new RTreeRegion(new Coordinate(11d,11d), new Coordinate(16d,11d)),
				new RTreeRegion(new Coordinate(13d,15d), new Coordinate(16d,20d)),
				new RTreeRegion(new Coordinate(20d,5d), new Coordinate(20d,5d)),
				new RTreeRegion(new Coordinate(10d,20d), new Coordinate(20d,20d)),
				new RTreeRegion(new Coordinate(7d,18d), new Coordinate(11d,18d))
		);
		for(RTreeRegion region : expRes){
			tree.insert(region);
		}
		for(RTreeRegion region : expRes) {
			assertEquals(1,tree.search(region).size());
			assertEquals(region, tree.search(region).get(0).getMbr());
		}

		RTreeRegion allBounded = new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(20d, 20d));
		List<RTreeNode> res = tree.rangeQuery(allBounded);
		assertEquals(5, res.size());

		RTreeRegion[] sorted = expRes.stream().sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		RTreeRegion[] sortedRes = res.stream().map(RTreeNode::getMbr).sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		assertArrayEquals(sorted,sortedRes);

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0d,0d), 5, kNNType.BETTER_BFS);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 4, kNNType.BETTER_BFS);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 3, kNNType.BETTER_BFS);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 2, kNNType.BETTER_BFS);
		assertEquals(2, kNNRes.size());

	}

	@Test
	void test3Ddata(){
		TreeConfig configMulti = new TreeConfig((byte)3,(byte)3);
		IOMonitoring monitoring = new IOMonitoring();
		PersistentCachedDatabase  db3d = new PersistentCachedDatabase(10, configMulti, new PersistentSequenceGenerator("testDb"), "testDb", monitoring);
		RTree tree = new RTree(configMulti, db3d);
		List<RTreeRegion> expRes = List.of(
				new RTreeRegion(new Coordinate(11d,11d,11d), new Coordinate(16d,11d,11d)),
				new RTreeRegion(new Coordinate(13d,15d,15d), new Coordinate(16d,20d,20d)),
				new RTreeRegion(new Coordinate(20d,5d,5d), new Coordinate(20d,5d,5d)),
				new RTreeRegion(new Coordinate(10d,20d,20d), new Coordinate(20d,20d,20d)),
				new RTreeRegion(new Coordinate(7d,18d,18d), new Coordinate(11d,18d,18d))
		);
		for(RTreeRegion region : expRes){
			tree.insert(region);
		}

		for(RTreeRegion region : expRes) {
			assertEquals(1,tree.search(region).size());
			assertEquals(region, tree.search(region).get(0).getMbr());
		}

		RTreeRegion allBounded = new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(20d, 20d));
		List<RTreeNode> res = tree.rangeQuery(allBounded);
		assertEquals(5, res.size());

		RTreeRegion[] sorted = expRes.stream().sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		RTreeRegion[] sortedRes = res.stream().map(RTreeNode::getMbr).sorted(Comparator.comparingDouble(val -> val.getBoundingRect().getMinByAxis(0))).toArray(RTreeRegion[]::new);
		assertArrayEquals(sorted,sortedRes);

		List<RTreeNode> kNNRes = tree.kNN(new Coordinate(0d,0d), 5, kNNType.BETTER_BFS);
		assertEquals(5, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 4, kNNType.BETTER_BFS);
		assertEquals(4, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 3, kNNType.BETTER_BFS);
		assertEquals(3, kNNRes.size());
		kNNRes = tree.kNN(new Coordinate(0d,0d), 2, kNNType.BETTER_BFS);
		assertEquals(2, kNNRes.size());

		RTreeRegion allBound = new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(20d, 20d));

		for(int i = 0 ; i < expRes.size(); i++){
			tree.delete(expRes.get(i));
			List<RTreeNode> resRange = tree.rangeQuery(allBound);
			assertEquals(expRes.size()-(i+1), resRange.size());
		}
	}


	@Test
	void performanceTest(){
		TreeConfig configMulti = new TreeConfig((byte)30,(byte)5);
		IOMonitoring monitoring = new IOMonitoring();
		PersistentCachedDatabase  dbMulti = new PersistentCachedDatabase(20000, configMulti, new PersistentSequenceGenerator("testDb"), "testDb", monitoring);
		RTree tree = new RTree(configMulti, dbMulti);

		IOMonitoring monitoringLin = new IOMonitoring();
		List<RTreeRegion> objects = FileHandlingUtil.loadObjectsIntoArray("src/test/resources/performance_5D",5);
		LinearDB linearDb = new LinearDB(configMulti, "testLinDB", new InMemorySequenceGenerator(),monitoringLin);

		long start = System.currentTimeMillis();
		for(RTreeRegion region : objects){
			linearDb.putNode(region);
		}
		long finish = System.currentTimeMillis();
		long timeElapsed=finish-start;
		System.out.println("Inserting "+ objects.size() + " objects into linear DB took " + timeElapsed + " ms");
		System.out.println("Inserting objects into linear DB contained " + monitoringLin.getTotalIO() + " IO operations");
		int queryCnt = objects.size() / 10;
		Random rand = new Random(System.currentTimeMillis());
		start = System.currentTimeMillis();
		for( int i = 0 ; i <  queryCnt ; ++i){
			int index = rand.nextInt(objects.size());
			List<RTreeRegion> res = linearFindFittingRegion(linearDb, objects.get(index));
			assertNotEquals(0,res.size());
//			assertEquals(objects.get(i%objects.size()), res);
		}
		finish = System.currentTimeMillis();
		timeElapsed=finish-start;
		System.out.println("Searching " + queryCnt + " linear search queries took " + timeElapsed + " ms and contained " + (monitoringLin.getTotalIO()-objects.size()) + " IO operations");



		start = System.currentTimeMillis();
		for(RTreeRegion region : objects){
			tree.insert(region);
		}
 		finish = System.currentTimeMillis();
		timeElapsed = finish - start;
		System.out.println("Inserting "+ objects.size() +" elements took " + timeElapsed + " ms"); // 10000 = 1,7s, 100000 = 378s
		int insertingIOs=monitoring.getTotalIO();
		System.out.println("Inserting objects into R-Tree took " + insertingIOs + " IO operations");
		start = System.currentTimeMillis();
		for( int i = 0 ; i <  queryCnt ; ++i){
			List<RTreeNode> res = tree.search(objects.get(i%objects.size()));
			assert(res.size() > 0);
		}
		finish = System.currentTimeMillis();
		timeElapsed=finish-start;
		System.out.println("Searching " + queryCnt + " RTree range queries took " + timeElapsed + " ms");
		int searchIOs=monitoring.getTotalIO()-insertingIOs;
		System.out.println("Querying objects in R-Tree took " + searchIOs + " IO operations");
	}


	public List<RTreeRegion> linearFindFittingRegion(LinearDB db, RTreeRegion region){
		List<RTreeRegion> found = new ArrayList<>();
		for(int i = 1 ; i < db.getDbSize(); ++i){
			RTreeRegion node = db.getNode(i);
			if(node.canFit(region))
				found.add(node);
		}
		return found;
	}
}