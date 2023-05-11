package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;

import java.util.ArrayList;
import java.util.List;

public class RTreeNode {
    private RTreeRegion minimumBoundingRegion;
    private double area = 0d;
    private List<RTreeNode> children = new ArrayList<>(Constants.NODE_CAPACITY);
    private RTreeNode parent;
    private final int id;

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    private boolean changed = false;
    private boolean isLeaf = true;

    public RTreeNode(int id, RTreeNode... children) {
        this.children = List.of(children);
        this.id = id;
        updateMBR();
    }

    private void setChildren(RTreeNode[] children) {
        this.children = List.of(children);
        updateMBR();
    }

    // returns index of the child
    public int maddChild(RTreeNode child) {
        child.setParent(this);
        this.children.add(child);
        updateMBR();
        return this.children.size() - 1;
    }

    // returns index of the child
    public int addChild(int childId, RTreeRegion child) {
        RTreeNode node = new RTreeNode(childId, child);
        node.setParent(this);
        this.children.add(node);
        updateMBR();
        return this.children.size() - 1;
    }


    public void setParent(RTreeNode parent) {
        this.parent = parent;
    }

    public RTreeNode getParent() {
        return this.parent;
    }

    public RTreeNode(int id, RTreeRegion mbr) {
        this.id = id;
        this.minimumBoundingRegion = mbr;
        this.calculateArea();
    }

    public void updateMBR() {
        if(children.size()==0) {
            this.minimumBoundingRegion = null;
            return;
        }
        Coordinate min = new Coordinate();
        Coordinate max = new Coordinate();
        BoundingBox boundingBox = children.get(0).getMbr().getBoundingRect();
        for(var child: children){
            BoundingBox childBB = child.getMbr().getBoundingRect();
            for(int i = 0 ;i < childBB.getFirst().getDimension(); ++i){
                if(childBB.getMinByAxis(i) < boundingBox.getMinByAxis(i))
                    boundingBox.setMinByAxis(i, childBB.getMinByAxis(i));
                if(childBB.getMaxByAxis(i) > boundingBox.getMaxByAxis(i))
                    boundingBox.setMaxByAxis(i, childBB.getMaxByAxis(i));
            }
        }
        this.minimumBoundingRegion.setBoundingRect(boundingBox);
    }

    public int getId() {
        return id;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean val) {
        isLeaf = val;
    }

    public RTreeRegion getMbr() {
        return minimumBoundingRegion;
    }

    public RTreeNode getChildByIndex(int index) {
        return children.get(index);
    }

    public void deleteChildByIndex(int index) {
        this.children.remove(index);
        updateMBR();
    }

    public List<RTreeNode> getChildren() {
        return children;
    }

    public int getParentId() {
        return parent.getId();
    }

    // returns hypothetical area of the node if it contained the given node
    public double getAreaContaining(RTreeNode node) {
        RTreeNode temp = new RTreeNode(-1, this.children.toArray(new RTreeNode[0]));
        temp.addChild(node.getId(), node.getMbr());
        return temp.getArea();

    }

    // returns added hypothetical area of the node if it contained given node
    public double getAreaDeltaContaining(RTreeNode node) {
        return area - getAreaContaining(node);
    }

    public double getArea() {
        return area;
    }

    public double calculateArea() {
        BoundingBox mbr = this.getMbr().getBoundingRect();
        area = 1;
        int dimension = mbr.getMin().getDimension();
        for (int i = 0 ; i < dimension ; ++i) {
            area *= mbr.getMax().getCoordinateByIndex(i) - mbr.getMin().getCoordinateByIndex(i);
        }
        return area;
    }
}

