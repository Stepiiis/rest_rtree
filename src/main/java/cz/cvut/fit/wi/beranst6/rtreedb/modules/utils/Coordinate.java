package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import java.util.Arrays;

public class Coordinate {

    private final double[] vectorCoordinates; // dimension is implicit by the size of the array

    public Coordinate(double... vectorCoordinates){
        this.vectorCoordinates = vectorCoordinates;
    }
    public Coordinate(int length){
        vectorCoordinates = new double[length];
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
    public double[] getCoordinates() {
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
