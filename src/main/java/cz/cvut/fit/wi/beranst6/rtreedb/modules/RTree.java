package cz.cvut.fit.wi.beranst6.rtreedb.modules;

public class RTree {
    public RTreeNode root;
    private final int maxNodeEntries; // stands for M in Guttman's paper. m = M/2 is implicit, no need to store it

    public RTree(int maxNodeEntries) {
        this.maxNodeEntries = maxNodeEntries;
    }
}
