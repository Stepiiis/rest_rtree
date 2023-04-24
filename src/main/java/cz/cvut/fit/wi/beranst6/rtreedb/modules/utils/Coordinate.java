package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

public class Coordinate {

    private double[] vectorCoordinates; // dimension is implicit by the size of the array

    public Coordinate(double... vectorCoordinates){
        this.vectorCoordinates = vectorCoordinates;
    }

    public Double getCoordinateByIndex(int index){
        return vectorCoordinates[index];
    }

    public CoordIndexPair getHighestCoordinate() {
        Double highestCoordinate = vectorCoordinates[0];
        int i = 1;
        for (; i < vectorCoordinates.length; i++) {
            if (vectorCoordinates[i] > highestCoordinate) {
                highestCoordinate = vectorCoordinates[i];
            }
        }
        return new CoordIndexPair(i, highestCoordinate);
    }

    public void setCoordinate(int index, double value){
        vectorCoordinates[index] = value;
    }

    public int getDimension(){
        return vectorCoordinates.length;
    }
    public double[] getCoordinates() {
        return vectorCoordinates;
    }
}
