package cz.cvut.fit.wi.beranst6.rtreedb.modules;

public class RTreeInternalNode extends RTreeNode {
    public int[] childrenIds; // array of ids of children nodes, used when caching children of an internal node
    public RTreeNode[] children; // array of children nodes, used when caching full internal node

    public RTreeInternalNode(int id, RTreeRegion mbr, int[] childrenIds) {
        super(id, mbr);
        this.childrenIds = childrenIds;
    }

    public RTreeInternalNode(int id, RTreeRegion mbr, RTreeNode[] children) {
        super(id, mbr);
        this.children = children;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }
}