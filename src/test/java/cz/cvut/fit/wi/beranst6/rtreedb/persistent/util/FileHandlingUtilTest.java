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
				new RTreeRegion(new Coordinate(11,11), new Coordinate(16,11)),
				new RTreeRegion(new Coordinate(13,15), new Coordinate(16,20)),
				new RTreeRegion(new Coordinate(20,5), new Coordinate(20,5)),
				new RTreeRegion(new Coordinate(10,20), new Coordinate(20,20)),
				new RTreeRegion(new Coordinate(7,18), new Coordinate(11,18))
		);
		List<RTreeRegion> regions = FileHandlingUtil.loadObjectsIntoArray(path, 2);
		assertEquals(5, regions.size());
		for(int i = 0; i < regions.size(); i++){
			assertEquals(expRes.get(i), regions.get(i));
		}
	}
}