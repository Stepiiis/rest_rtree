package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;

public class RTree {
    public RTreeNode root;

    public RTree(int maxNodeEntries, int dimension) {
        Constants.CURR_DIMENSION=(byte)dimension;
        Constants.CURR_MAX_CAPACITY = (byte)maxNodeEntries;// stands for M in Guttman's paper. m = M/2 is implicit, no need to store it
    }
}
