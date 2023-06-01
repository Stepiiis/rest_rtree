package cz.cvut.fit.wi.beranst6.rtreedb.persistent.util;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileHandlingUtilTest {

	@Test
	void testLoadObjectsIntoArray() {
		String path = "src/test/resources/testFileHandlingUtil.txt";
		List<RTreeRegion> expRes = List.of(
				new RTreeRegion(new Coordinate(11d,11d), new Coordinate(16d,11d)),
				new RTreeRegion(new Coordinate(13d,15d), new Coordinate(16d,20d)),
				new RTreeRegion(new Coordinate(20d,5d), new Coordinate(20d,5d)),
				new RTreeRegion(new Coordinate(10d,20d), new Coordinate(20d,20d)),
				new RTreeRegion(new Coordinate(7d,18d), new Coordinate(11d,18d))
		);
		List<RTreeRegion> regions = FileHandlingUtil.loadObjectsIntoArray(path, 2);
		assertEquals(5, regions.size());
		for(int i = 0; i < regions.size(); i++){
			assertEquals(expRes.get(i), regions.get(i));
		}
	}
}