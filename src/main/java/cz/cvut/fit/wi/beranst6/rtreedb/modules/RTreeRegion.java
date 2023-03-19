package cz.cvut.fit.wi.beranst6.rtreedb.modules;

public class RTreeRegion{

    private final int id;
    private final int dim;
    Coordinate c1,c2, c3;

    public RTreeRegion(int id, Coordinate c1, Coordinate c2){
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = new Coordinate(); // all zeroes, only for 2D
        this.dim = 2;
        this.id = id;
    }
    public RTreeRegion(int id, Coordinate c1, Coordinate c2, Coordinate c3){
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.dim = 3;
        this.id = id;
    }


}
