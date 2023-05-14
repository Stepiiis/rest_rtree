package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RTreeRegion{

    public BoundingBox getBoundingRect() {
        return boundingBox;
    }
    public void setBoundingRect(BoundingBox box) {
        this.boundingBox = box;
    }
    public int getDimension() {
        return dimension;
    }
    private BoundingBox boundingBox;

    private int dimension;

    public RTreeRegion(Coordinate... box){
        boundingBox=calculateMBR(box);
        dimension = box.length;
    }

    public RTreeRegion(BoundingBox mbr){
        boundingBox = mbr;
        dimension = mbr.getMax().getDimension();
    }

    public BoundingBox calculateMBR(Coordinate[] box){
        Coordinate min = new Coordinate(box.length);
        Coordinate max = new Coordinate(box.length);
        for(int i = 0 ; i < box.length; i++){
            Pair<Double,Double> temp = calculateProjectionByAxis(i, box);
            min.setCoordinateByAxis(i, temp.getFirst());
            max.setCoordinateByAxis(i, temp.getSecond());
        }
        return new BoundingBox(min,max);
    }


    public Pair<Double,Double> getProjectionByAxis(int axis){
        return new Pair<>(boundingBox.getMinByAxis(axis), boundingBox.getMaxByAxis(axis));
    }

    public static Pair<Double,Double> calculateProjectionByAxis(int axis, Coordinate[] box) {
        Double min = box[0].getCoordinateByIndex(axis), max=min;
        for(Coordinate coord: box){
            if(coord.getCoordinateByIndex(axis) < min){
                min = coord.getCoordinateByIndex(axis);
            }
            if(coord.getCoordinateByIndex(axis) > max){
                max = coord.getCoordinateByIndex(axis);
            }
        }
        return new Pair<>(min, max);
    }

    public List<Coordinate> getMBRasList(){
        return List.of(boundingBox.getMin(), boundingBox.getMax());
    }

    public RTreeRegion copy() {
        return new RTreeRegion(boundingBox.copy());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof RTreeRegion that)) return false;
        return getDimension() == that.getDimension() && boundingBox.equals(that.boundingBox);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boundingBox, getDimension()); // this seems wrong
    }
}
