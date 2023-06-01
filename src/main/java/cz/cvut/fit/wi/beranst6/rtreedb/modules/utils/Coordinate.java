package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import java.util.Arrays;
import java.util.List;

public class Coordinate {

    private final Double[] vectorCoordinates; // dimension is implicit by the size of the array

    public Coordinate(Double... vectorCoordinates){
        this.vectorCoordinates = vectorCoordinates;
    }
    public Coordinate(int length){
        vectorCoordinates = new Double[length];
    }

    public Coordinate(List<Double> coordinates) {
        this.vectorCoordinates = coordinates.toArray(Double[]::new);
    }

    @Override
    public String toString() {
        return Arrays.toString(vectorCoordinates);
    }

    public Double getCoordinateByIndex(int index){
        return vectorCoordinates[index];
    }

    public CoordIndexPair getHighestCoordinate() {
        double highestCoordinate = vectorCoordinates[0];
        int i = 1;
        for (; i < vectorCoordinates.length; i++) {
            if (vectorCoordinates[i] > highestCoordinate) {
                highestCoordinate = vectorCoordinates[i];
            }
        }
        return new CoordIndexPair(i, highestCoordinate);
    }

    public void setCoordinateByAxis(int index, double value){
        vectorCoordinates[index] = value;
    }

    public int getDimension(){
        return vectorCoordinates.length;
    }
    public Double[] getCoordinates() {
        return vectorCoordinates;
    }

	public Coordinate copy() {
        return new Coordinate(vectorCoordinates.clone());
	}

    @Override
    public boolean equals(Object o) {
        if(o == null) return false;
        if (this == o) return true;
        if (! (o instanceof Coordinate that)) return false;
        return Arrays.equals(vectorCoordinates, that.vectorCoordinates);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vectorCoordinates);
    }
}
