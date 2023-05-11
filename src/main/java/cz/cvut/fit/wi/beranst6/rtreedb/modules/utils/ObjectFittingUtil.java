package cz.cvut.fit.wi.beranst6.rtreedb.modules.utils;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

public class ObjectFittingUtil {
    public static boolean SAT(RTreeRegion object, RTreeRegion target){
        int dimension = Math.min(object.getDimension(),target.getDimension());
        for(int axis = 0; axis < dimension; ++axis){
            if(!checkProjectionContainment(object.getProjectionByAxis(axis),target.getProjectionByAxis(axis))){
                return false;
            }
        }
        return true;
    }

    private static boolean checkProjectionContainment(Pair<Double,Double> object, Pair<Double,Double> target) {
        return (target.getFirst() <= object.getFirst() && target.getSecond() >= object.getSecond());
    }
}
