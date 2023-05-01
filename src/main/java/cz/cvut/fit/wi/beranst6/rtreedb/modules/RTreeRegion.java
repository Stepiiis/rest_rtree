package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;

public class RTreeRegion{

    public Coordinate[] getBoundingRect() {
        return boundingBox;
    }

    Coordinate[] boundingBox;

    public RTreeRegion(Coordinate... box){
        boundingBox = box;
    }

    /**
     *  returns true if object is inside region, specified by obj2
     * */
    public boolean isInside(RTreeRegion reg){
        //todo
        throw new Error("not implemented");
    }

}
