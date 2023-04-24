package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;

public class RTreeNode {
    private RTreeRegion minimumBoundingRegion;
    private RTreeNode[] children = new RTreeNode[Constants.NODE_CAPACITY];
    private RTreeNode parent;
    private int id;
    private boolean isValid;
    private boolean isLeaf = true;

    public RTreeNode(int id, RTreeNode... children) {
        this.children = children;
        this.id = id;
        this.isValid = true;
        updateMBR();
    }

    private void setChildren(RTreeNode[] children) {
        this.children = new RTreeNode[children.length];
        for (RTreeNode child : children) {
            addChild(child);
        }
    }

    public void addChild(RTreeNode child){
        this.children[this.children.length-1] = child;
        this.children[this.children.length-1].setParent(this);
    }
    public void addChild(RTreeRegion child){
        this.children[this.children.length-1] = new RTreeNode(child.getId(), child);
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

    public void invalidate(){
        this.isValid = false;
    }

    public RTreeRegion getMBR() {
        return minimumBoundingRegion;
    }

    public RTreeNode getChildByIndex(int index) {
        return children[index];
    }
}

