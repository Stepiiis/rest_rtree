package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import java.util.Objects;

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

    public BoundingBox copy() {
        return new BoundingBox(first.copy(), second.copy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof BoundingBox pair)) return false;
        return getFirst().equals(pair.getFirst()) && getSecond().equals(pair.getSecond());
    }
}
