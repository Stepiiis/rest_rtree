package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;

import java.util.Collection;

public class RTreeNode {
    private RTreeRegion minimumBoundingRegion;
    private RTreeNode[] children = new RTreeNode[Constants.NODE_CAPACITY];
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
        this.children = children;
        this.id = id;
        updateMBR();
    }

    private void setChildren(RTreeNode[] children) {
        this.children = new RTreeNode[children.length];
        for (RTreeNode child : children) {
            addChild(child);
        }
    }

    public void addChild(RTreeNode child, int childId){
        this.children[this.children.length-1] = child;
        this.children[this.children.length-1].setParent(this);
    }
    public int addChild(RTreeRegion child, int childId){
        this.children[this.children.length-1] = new RTreeNode(id, child);
        this.children[this.children.length-1].setParent(this);
    }


    public void setParent(RTreeNode parent){
        this.parent = parent;
    }

    public RTreeNode getParent(){
        return this.parent;
    }

    public RTreeNode(int id, RTreeRegion mbr){
        this.id = id;
        this.minimumBoundingRegion = mbr;
    }

    public void updateMBR(){
        //todo implement
        throw new Error("not implemented");
    }

    public int getId() {
        return id;
    }

    public boolean isLeaf(){
        return isLeaf;
    }

    public void setLeaf(boolean val){
        isLeaf=val;
    }

    public RTreeRegion getMbr() {
        return minimumBoundingRegion;
    }
    public Coordinate[] getMbrArr() {
        return minimumBoundingRegion.getBoundingRect();
    }

    public RTreeNode getChildByIndex(int index) {
        return children[index];
    }
    public void deleteChildByIndex(int index) {
        this.children[index] = null;
        updateMBR();
    }

    public RTreeNode[] getChildren() {
        return children;
    }

    public int getParentId() {
        return parent.getId();
    }
}

