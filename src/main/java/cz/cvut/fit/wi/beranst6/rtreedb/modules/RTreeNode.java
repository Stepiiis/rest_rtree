package cz.cvut.fit.wi.beranst6.rtreedb.modules;

public abstract class RTreeNode {
    public RTreeRegion mbr;
    private final int id;

    public RTreeNode(int id, RTreeRegion mbr) {
        this.mbr = mbr;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public abstract boolean isLeaf();
}
