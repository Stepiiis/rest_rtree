package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

public class ObjectFittingUtil {
    public static boolean SAT(RTreeRegion objectToFit, RTreeRegion target){
        int dimension = Math.min(objectToFit.getDimension(),target.getDimension());
        for(int axis = 0; axis < dimension; ++axis){
            if(!checkProjectionContainment(objectToFit.getProjectionByAxis(axis),target.getProjectionByAxis(axis))){
                return false;
            }
        }
        return true;
    }

    private static boolean checkProjectionContainment(Pair<Double,Double> objectToFit, Pair<Double,Double> target) {
        return (target.getFirst() <= objectToFit.getFirst() && target.getSecond() >= objectToFit.getSecond());
    }

    public static boolean intersects(RTreeRegion object1, RTreeRegion object2) {
        int dimension = Math.min(object1.getDimension(),object2.getDimension());
        for(int axis = 0; axis < dimension; ++axis){
            if(checkProjectionIntersection(object1.getProjectionByAxis(axis),object2.getProjectionByAxis(axis))){
                return true;
            }

        }
        return false;
    }

    private static boolean checkProjectionIntersection(Pair<Double, Double> object1, Pair<Double, Double> object2) {
        return object1.getFirst() < object2.getSecond() && object2.getFirst() < object1.getSecond();
    }
}
