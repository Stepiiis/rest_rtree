package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

public class BoundingBox extends Pair<Coordinate, Coordinate>{
    public BoundingBox(Coordinate min, Coordinate max) {
        super(min, max);
    }
    public Coordinate getMin(){
        return first;
    }
    public Coordinate getMax(){
        return second;
    }
    public double getMinByAxis(int axis){
        return first.getCoordinateByIndex(axis);
    }
    public double getMaxByAxis(int axis){
        return second.getCoordinateByIndex(axis);
    }

    public void setMinByAxis(int i, double value) {
        first.setCoordinateByAxis(i, value);
    }

    public void setMaxByAxis(int i, double value) {
        second.setCoordinateByAxis(i, value);
    }
}
