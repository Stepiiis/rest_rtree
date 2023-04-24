package cz.cvut.fit.wi.beranst6.rtreedb.modules;

public abstract class RTreeNode {
    public RTreeRegion mbr;
    private final int id;
    private boolean isValid;

    public RTreeNode(int id, RTreeRegion mbr) {
        this.mbr = mbr;
        this.id = id;
        this.isValid = true;
    }

    public int getId() {
        return id;
    }

    public abstract boolean isLeaf();

    public void invalidate(){
        this.isValid = false;
    }
}

