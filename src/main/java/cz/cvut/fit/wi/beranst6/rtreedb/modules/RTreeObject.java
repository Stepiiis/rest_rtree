package cz.cvut.fit.wi.beranst6.rtreedb.modules;

public class RTreeObject {
    Coordinate coordinates;
    private final int id;

    public RTreeObject(int id, double x, double y, double z){
        this.id = id;
        coordinates = new Coordinate(x,y,z);
    }

    /**
     *  returns true if object is inside region, specified by obj2
     * */
    public boolean isInside(RTreeRegion reg){
        //todo
        throw new Error("not implemented");
    }
}
