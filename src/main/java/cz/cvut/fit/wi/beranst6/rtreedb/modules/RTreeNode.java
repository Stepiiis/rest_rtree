package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RTreeNode {
    private RTreeRegion minimumBoundingRegion;
    private double area = 0d;
    private List<RTreeNode> children = new ArrayList<>(Constants.NODE_CAPACITY);
    private RTreeNode parent = null;
    private int parentId = 0;
    private int id;
    private int depth = 0; // root is 0;

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    private boolean changed = false;
    private boolean isLeaf = true;

    public RTreeNode(int id, List<RTreeNode> children) {
        this.children = children;
        this.id = id;
        updateMBR();
    }
    public RTreeNode(RTreeNode... children){
        this.children = List.of(children);
        updateMBR();
    }
    public RTreeNode(int id){
        this.id = id;
    }

    public void setChildren(List<RTreeNode> children) {
        this.children = children;
        updateMBR();
    }

    public void setChildren(List<RTreeNode> children, RTreeRegion mbr){
        this.children = children;
        this.minimumBoundingRegion = mbr;
        this.calculateArea();
    }

    // returns index of the child
    public int addChild(RTreeNode child) {
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
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
    public void setParent(RTreeNode parent) {
        this.parent = parent;
        this.parentId = (parent != null? parent.getId():0) ;
    }

    public RTreeNode getParent() {
        return this.parent;
    }

    public RTreeNode(int id, RTreeRegion mbr) {
        this.id = id;
        this.minimumBoundingRegion = mbr;
        this.calculateArea();
    }

    public void updateChildMbrInParent(RTreeNode child){
        RTreeNode tempChild = this.getChildById(child.getId());
        if(tempChild == null)
            return;
        tempChild.setMbr(child.getMbr());
        this.updateMBR();
    }

    public void setMbr(RTreeRegion mbr) {
        this.minimumBoundingRegion=mbr;
        calculateArea();
    }


    protected void updateMBR() {
        if(children.size()==0) {
            this.minimumBoundingRegion = null;
            this.area = 0;
            return;
        }

        Coordinate min = new Coordinate();
        Coordinate max = new Coordinate();
        BoundingBox boundingBox = children.get(0).getMbr().getBoundingRect().copy();
        for(var child: children){
            BoundingBox childBB = child.getMbr().getBoundingRect();
            for(int i = 0 ;i < childBB.getFirst().getDimension(); ++i){
                if(childBB.getMinByAxis(i) < boundingBox.getMinByAxis(i))
                    boundingBox.setMinByAxis(i, childBB.getMinByAxis(i));
                if(childBB.getMaxByAxis(i) > boundingBox.getMaxByAxis(i))
                    boundingBox.setMaxByAxis(i, childBB.getMaxByAxis(i));
            }
        }
        this.minimumBoundingRegion = new RTreeRegion(boundingBox);
        this.calculateArea();
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

    // linearni staci, max pocet deti nebude presahovat nizsi desitky => linear je rychlejsi nez overhead rekurze + binSearch
    public RTreeNode getChildById(int id) {
        for(int i = 0 ; i<children.size(); ++i){
            if(children.get(i).getId() == id)
                return children.get(i);
        }
        return null;
    }
    public RTreeNode getChildByIndex(int index){
        return children.get(index);
    }

    // linearni staci, max pocet deti nebude presahovat nizsi desitky => linear je rychlejsi nez overhead rekurze + binSearch
    public boolean deleteChildById(int id) {
        for(int i = 0 ;i < children.size();++i){
            if(children.get(i).getId() == id){
                children.remove(i);
                updateMBR();
                return true;
            }
        }
        return false;
    }

    public List<RTreeNode> getChildren() {
        return children;
    }

    public int getParentId() {
        return parentId;
    }

    // returns hypothetical area of the node if it contained the given node
    public double getAreaContaining(RTreeRegion region) {
        List<RTreeNode> childrenArr = new ArrayList<>(List.copyOf(this.children));
        childrenArr.add(new RTreeNode(-2, region));
        RTreeNode temp = new RTreeNode(-1, childrenArr);
        return temp.getArea();
    }
    /**
    * @return added hypothetical area of the node if it contained given node.
    * first is delta of area.
    * second is totalArea containing the node.
   */
    public Pair<Double,Double> getAreaDeltaContaining(RTreeRegion node) {
        double containing = getAreaContaining(node);
        return new Pair<>(containing - area, containing);
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
        this.area=area;
        return area;
    }

    public void setId(int idNode) {
        this.id = idNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (! (o instanceof RTreeNode rTreeNode)) return false;
        return Double.compare(rTreeNode.getArea(), getArea()) == 0 && getId() == rTreeNode.getId() && minimumBoundingRegion.equals(rTreeNode.minimumBoundingRegion) && Objects.equals(getChildren(), rTreeNode.getChildren()) && Objects.equals(getParentId(), rTreeNode.getParentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}

