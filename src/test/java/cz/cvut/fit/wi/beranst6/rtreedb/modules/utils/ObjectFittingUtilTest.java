package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectFittingUtilTest {


    @Test
    void testSAT() {
        RTreeRegion[][] objectTargetTestVals = new RTreeRegion[][]{
                {new RTreeRegion(new Coordinate(0d,1d), new Coordinate(1d,0d)), new RTreeRegion(new Coordinate(0d,1d), new Coordinate(1d,0d))},
                {new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(1d,0d,0d), new Coordinate(0d,0d,1d)),new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(1d,0d,0d), new Coordinate(0d,0d,1d))},
                {new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(1d,0d,0d), new Coordinate(0d,0d,1d)),new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(3d,5d,7d), new Coordinate(3d,3d,3d))},
                {new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(1d,0d,0d), new Coordinate(0d,0d,4d)),new RTreeRegion(new Coordinate(0d,0d,0d), new Coordinate(6d,6d,6d), new Coordinate(5d,5d,5d))},
                {new RTreeRegion(new Coordinate(0d,0d,0d), new Coordinate(0d,0d,0d), new Coordinate(0d,0d,0d)),new RTreeRegion(new Coordinate(0d,0d,0d), new Coordinate(6d,6d,6d), new Coordinate(1d,1d,1d))},
        };

        RTreeRegion[][] objectTargetFalseTestVals = new RTreeRegion[][]{
                {new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(3d,5d,7d)),new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(1d,0d,0d))},
                {new RTreeRegion(new Coordinate(0d,0d,0d), new Coordinate(6d,6d,6d)),new RTreeRegion(new Coordinate(0d,1d,0d), new Coordinate(1d,0d,0d))},
                {new RTreeRegion(new Coordinate(0d,0d,0d), new Coordinate(6d,6d,6d)),new RTreeRegion(new Coordinate(0d,0d,0d), new Coordinate(0d,0d,0d))},
        };


        for(var testVal: objectTargetTestVals) {
            assertTrue(ObjectFittingUtil.SAT(testVal[0], testVal[1]));
        }
        for(var testVal: objectTargetFalseTestVals){
            assertFalse(ObjectFittingUtil.SAT(testVal[0], testVal[1]));
        }
    }
}