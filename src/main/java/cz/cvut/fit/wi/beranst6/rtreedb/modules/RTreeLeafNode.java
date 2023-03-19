package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import java.util.List;

public class RTreeLeafNode extends RTreeNode {
    public List<RTreeRegion> objects;

    public RTreeLeafNode(int id, RTreeRegion mbr, List<RTreeRegion> objects) {
        super(id, mbr);
        this.objects = objects;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }
}