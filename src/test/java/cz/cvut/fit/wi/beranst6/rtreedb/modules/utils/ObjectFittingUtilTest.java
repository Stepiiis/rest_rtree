package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectFittingUtilTest {


    @Test
    void testSAT() {
        RTreeRegion[][] objectTargetTestVals = new RTreeRegion[][]{
                {new RTreeRegion(new Coordinate(0,1), new Coordinate(1,0)), new RTreeRegion(new Coordinate(0,1), new Coordinate(1,0))},
                {new RTreeRegion(new Coordinate(0,1,0), new Coordinate(1,0,0), new Coordinate(0,0,1)),new RTreeRegion(new Coordinate(0,1,0), new Coordinate(1,0,0), new Coordinate(0,0,1))},
                {new RTreeRegion(new Coordinate(0,1,0), new Coordinate(1,0,0), new Coordinate(0,0,1)),new RTreeRegion(new Coordinate(0,1,0), new Coordinate(3,5,7), new Coordinate(3,3,3))},
                {new RTreeRegion(new Coordinate(0,1,0), new Coordinate(1,0,0), new Coordinate(0,0,4)),new RTreeRegion(new Coordinate(0,0,0), new Coordinate(6,6,6), new Coordinate(5,5,5))},
                {new RTreeRegion(new Coordinate(0,0,0), new Coordinate(0,0,0), new Coordinate(0,0,0)),new RTreeRegion(new Coordinate(0,0,0), new Coordinate(6,6,6), new Coordinate(1,1,1))},
        };

        RTreeRegion[][] objectTargetFalseTestVals = new RTreeRegion[][]{
                {new RTreeRegion(new Coordinate(0,1,0), new Coordinate(3,5,7)),new RTreeRegion(new Coordinate(0,1,0), new Coordinate(1,0,0))},
                {new RTreeRegion(new Coordinate(0,0,0), new Coordinate(6,6,6)),new RTreeRegion(new Coordinate(0,1,0), new Coordinate(1,0,0))},
                {new RTreeRegion(new Coordinate(0,0,0), new Coordinate(6,6,6)),new RTreeRegion(new Coordinate(0,0,0), new Coordinate(0,0,0))},
        };


        for(var testVal: objectTargetTestVals) {
            assertTrue(ObjectFittingUtil.SAT(testVal[0], testVal[1]));
        }
        for(var testVal: objectTargetFalseTestVals){
            assertFalse(ObjectFittingUtil.SAT(testVal[0], testVal[1]));
        }
    }
}