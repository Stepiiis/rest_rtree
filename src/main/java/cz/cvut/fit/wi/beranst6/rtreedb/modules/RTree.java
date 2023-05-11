package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Pair;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.ObjectFittingUtil.SAT;

public class RTree {
    public RTreeNode root = null;

    public TreeConfig config;
    public final DatabaseInterface db;

    public RTree(int maxNodeEntries, int dimension) {
        config = new TreeConfig((byte) maxNodeEntries, (byte) dimension); // maxNodeEntries stands for M in Guttman's paper. m = M/2 is implicit, no need to store it
        db = new InMemoryDatabase(new InMemorySequenceGenerator());
    }

    public List<RTreeNode> kNN(Coordinate point, int k){
        // todo implement
        throw new RuntimeException("not implemented");
    }

    public List<RTreeNode> rangeQuery(RTreeRegion region){
        // todo implement
        throw new RuntimeException("not implemented");
    }

    public void insert(RTreeRegion region){
        if(root == null) // tree empty
        {
            root = new RTreeNode(db.getNextId(), region);
            db.putNode(root.getId(), root);
            return;
        }
        RTreeNode leaf = chooseLeaf(region);
        int leafSize = leaf.addChild(db.getNextId(), region);
        if(leafSize < config.getMaxNodeEntries()){
            adjustTree(new Pair<>(List.of(leaf),null));
        }else{
            Pair<List<RTreeNode>,List<RTreeNode>> pair = splitNode(leaf);
            adjustTree(pair);
        }
    }

    /**
     * Returns all nodes that are inside given region
     * @param region
     * @return List of nodes inside region given as parameter
     */
    public List<RTreeNode> search(RTreeRegion region){
        return searchImpl(root,region);
    }

    private List<RTreeNode> searchImpl(RTreeNode node,RTreeRegion region){
        ArrayList<RTreeNode> result = new ArrayList<>();
        if(node == null)
            return result;
        boolean isLeaf = db.isLeaf(node.getId());
        List<RTreeNode> children = db.getAllChildren(node.getId());
        for( var child : children){
            if (SAT(child.getMbr(), region)){
                if(isLeaf)
                    result.add(child);
                else
                    result.addAll(searchImpl(child, region));
            }
        }
        return result;
    }

    public boolean delete(RTreeNode entry){
        // todo implement
        throw new RuntimeException("not implemented");
    }

    public RTreeNode chooseLeaf(RTreeRegion entry){
        return chooseLeafImpl(root, entry);
    }

    private RTreeNode chooseLeafImpl(RTreeNode node, RTreeRegion entry) {
        if(db.isLeaf(node.getId()))
            return node;
        List<RTreeNode> children = db.getAllChildren(node.getId());
        RTreeNode bestChild = getBestEnlargedChildContaining(children, entry);
        if(bestChild == null)
            throw new DatabaseException("Given node has no children. NodeID="+node.getId() + " Entry="+entry + " Node="+node);
        return chooseLeafImpl(bestChild, entry);
    }

    private RTreeNode getBestEnlargedChildContaining(List<RTreeNode> children, RTreeRegion entry) {
        double bestArea=0;
        RTreeNode bestChild = null;
        for(var child: children){

        }
    }

    public Pair<List<RTreeNode>,List<RTreeNode>> splitNode(RTreeNode node){
        // todo implement
        throw new RuntimeException("not implemented");
    }

    // saves to db and returns true if any split needed to be done
    public boolean adjustTree(Pair<List<RTreeNode>,List<RTreeNode>> pair){
        // todo implement
        throw new RuntimeException("not implemented");
    }

    public Pair<RTreeNode,RTreeNode> linPickSeeds(List<RTreeNode> entries){
        // todo implement
        throw new RuntimeException("not implemented");
    }

    public RTreeNode pickNext(Collection<RTreeNode> group) {
        // todo implement
        throw new RuntimeException("not implemented");
    }

    public void condenseTree(RTreeNode leafNode){
        // todo implement
        throw new RuntimeException("not implemented");
    }
}
