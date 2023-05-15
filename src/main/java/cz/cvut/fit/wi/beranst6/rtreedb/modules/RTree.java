package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Pair;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.*;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

import static cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.ObjectFittingUtil.SAT;

/**
 * @author Štěpán Beran
 */
public class RTree {
	public RTreeNode root = null;

	public TreeConfig config;
	public final DatabaseInterface db;

	public RTree(int maxNodeEntries, int dimension, DatabaseInterface database) {
		config = new TreeConfig((byte) maxNodeEntries, (byte) dimension);
		db = database;
	}

	public RTree(TreeConfig config, DatabaseInterface database) {
		this.config = config;
		db = database;
	}

	public List<RTreeNode> kNN(Coordinate point, int k) {
		// todo implement
		throw new RuntimeException("not implemented");
	}

	public void insert(RTreeRegion region) {
		insert(new RTreeNode(db.getNextId(), region), true);
	}

	protected void insert(RTreeNode node, boolean increaseDepth) {
		if (getRoot() == null) // tree empty
		{
			root = node;
			root.addChild(new RTreeNode(db.getNextId(), node.getMbr().copy()));
			db.saveNewRoot(root);
			return;
		}
		RTreeNode leaf = chooseNode(node, increaseDepth);
		leaf = db.getNode(leaf.getId());
		if (leaf.getChildren().size() < config.getMaxNodeEntries()) {
			node.setParent(leaf);
			db.putChild(leaf.getId(), node);
			leaf = db.getNode(leaf.getId());
			adjustTree(new Pair<>(leaf, null));
		} else {
			Pair<RTreeNode, RTreeNode> pair = splitNode(leaf, node, true);
			boolean splitRoot = false;
			if(leaf.getId() == getRoot().getId()){
				// split of root, remove old and create new one;
				root = new RTreeNode(db.getNextId());
				root.addChild(pair.getFirst());
				root.addChild(pair.getSecond());
				db.saveNewRoot(root);
				splitRoot=true;
			}else{
				pair.getFirst().setParentId(leaf.getParentId());

				pair.getSecond().setParentId(leaf.getParentId());
			}
			db.deleteIndexRecord(leaf);
			db.putNode(pair.getFirst());
			db.putNode(pair.getSecond());
			if(!splitRoot)
				adjustTree(pair);
		}
	}

	/**
	 * Returns all nodes that are inside given region
	 *
	 * @param region
	 * @return List of nodes inside region given as parameter
	 */
	public List<RTreeNode> search(RTreeRegion region) {
		return searchImpl(getRoot(), region);
	}

	private List<RTreeNode> searchImpl(RTreeNode node, RTreeRegion region) {
		ArrayList<RTreeNode> result = new ArrayList<>();
		if (node == null)
			return result;
		boolean isLeaf = db.isLeaf(node.getId());
		List<RTreeNode> children = db.getAllChildren(node.getId());
		for (var child : children) {
			if (SAT(child.getMbr(), region)) {
				if (isLeaf)
					result.add(child);
				else
					result.addAll(searchImpl(child, region));
			}
		}
		return result;
	}

	public boolean delete(RTreeNode entry) {
		RTreeNode L = findLeaf(entry);
		if (db.deleteChildById(L.getId(), entry.getId()))
			return false;
		L = db.getNode(L.getId());
		condenseTree(L);
		if (getRoot() != null && this.getRoot().getChildren().size() == 1) { // growing tree smaller because root is virtually empty
			this.root = db.getNode(getRoot().getChildren().get(0).getId());
			this.root.setParent(null);
			this.root.setParentId(0);
			db.saveNewRoot(root);
		}
		return true;
	}

	private RTreeNode getRoot() {
		this.root = db.getRoot();
		return this.root;
	}

	private RTreeNode findLeaf(RTreeNode entry) {
		return findLeafImpl(getRoot(), entry);
	}

	private RTreeNode findLeafImpl(RTreeNode node, RTreeNode entry) {
		if (db.isLeaf(node.getId())) {
			return node; // null means not found
		}
		List<RTreeNode> children = db.getAllChildren(node.getId());
		RTreeNode bestChild = getBestEnlargedChildContaining(children, entry.getMbr());
		if (bestChild == null)
			throw new DatabaseException("Given node has no children. *Should never happen.* NodeID=" + node.getId() + " Entry=" + entry + " Node=" + node);
		return findLeafImpl(bestChild, entry);
	}

	public RTreeNode chooseNode(RTreeRegion entry, boolean increasingDepth) {
		return chooseNodeImpl(getRoot(), new RTreeNode(- 1, entry), increasingDepth); // inserting new node, se we need to calculate the depth, should be true
	}

	public RTreeNode chooseNode(RTreeNode entry, boolean increasingDepth) {
		return chooseNodeImpl(getRoot(), entry, increasingDepth); // inserting node that already has depth set
	}


	private RTreeNode chooseNodeImpl(RTreeNode node, RTreeNode entry, boolean increasingDepth) {
		if (db.isLeaf(node.getId()))
			return node;
		if (! increasingDepth) {
			if (node.getDepth() >= entry.getDepth())
				return node;
		} else {
			entry.setDepth(node.getDepth() + 1);
		}
		List<RTreeNode> children = db.getAllChildren(node.getId());
		RTreeNode bestChild = getBestEnlargedChildContaining(children, entry.getMbr());
		if (bestChild == null)
			throw new DatabaseException("Given node has no children. NodeID=" + node.getId() + " Entry=" + entry + " Node=" + node);
		return chooseNodeImpl(bestChild, entry, increasingDepth);
	}

	// first measures leastEnlarged, then leastTotalArea
	protected static RTreeNode getBestEnlargedChildContaining(List<RTreeNode> children, RTreeRegion entry) {
		double bestAreaDelta = Double.MAX_VALUE;
		double bestTotalArea = Double.MAX_VALUE;
		RTreeNode bestChild = null;
		for (var child : children) {
			Pair<Double, Double> areaPair = child.getAreaDeltaContaining(entry);
			if (areaPair.getFirst() < bestAreaDelta) {
				bestAreaDelta = areaPair.getFirst();
				bestTotalArea = areaPair.getSecond();
				bestChild = child;
			} else if (areaPair.getFirst() == bestAreaDelta && areaPair.getSecond() < bestTotalArea) {
				bestTotalArea = areaPair.getSecond();
				bestChild = child;
			}
		}
		return bestChild;
	}

	public Pair<RTreeNode, RTreeNode> splitNode(RTreeNode node, RTreeNode newChild, boolean isLeaf) {
		List<RTreeNode> children = new ArrayList<>(List.copyOf(node.getChildren()));
		children.add(newChild);
		Pair<RTreeNode, RTreeNode> seeds = linPickSeeds(children);
		Pair<RTreeNode, RTreeNode> newNodes = new Pair<>(new RTreeNode(node.getId()), new RTreeNode(db.getNextId()));
		newNodes.getFirst().addChild(new RTreeNode(seeds.getFirst().getId(), seeds.getFirst().getMbr().copy())); // TODO: check if copy of children is needed. likely not as we are not modifying the children
		newNodes.getSecond().addChild(new RTreeNode(seeds.getSecond().getId(), seeds.getSecond().getMbr().copy()));
		if(!isLeaf){
			RTreeNode DBFirstSeed = db.getNode(seeds.getFirst().getId());
			RTreeNode DBSecondSeed = db.getNode(seeds.getSecond().getId());
			DBFirstSeed.setParent(newNodes.getFirst());
			DBSecondSeed.setParent(newNodes.getSecond());
			db.updateNodeHeader(DBFirstSeed);
			db.updateNodeHeader(DBSecondSeed);
		}
		while (children.size() > 0) {
			if(isAnyOfGroupTooSmall(newNodes,children)) {
				addAllToGroupNeeded(children, newNodes, isLeaf);
				return newNodes;
			}
			node = linPickNext(children);
			RTreeNode parent = pickParent(node, newNodes);
			parent.addChild(node);
			if(!isLeaf){
				// no need to set parent. already done in line above ... addChild(node)
				db.updateNodeHeader(node);
			}
		}
		return newNodes;
	}

	private void addAllToGroupNeeded(List<RTreeNode> children, Pair<RTreeNode, RTreeNode> newNodes, boolean isLeaf) {
		RTreeNode smallestNode = newNodes.getFirst().getChildren().size() > newNodes.getSecond().getChildren().size() ? newNodes.getSecond() : newNodes.getFirst();
		for(RTreeNode node : children){
			smallestNode.addChild(node);
			if(!isLeaf){
				db.updateNodeHeader(node);
			}
		}
	}

	private boolean isAnyOfGroupTooSmall(Pair<RTreeNode, RTreeNode> newNodes, List<RTreeNode> children) {
		return newNodes.getFirst().getChildren().size() + children.size() == config.getMinNodeEntries() ||
				newNodes.getSecond().getChildren().size() + children.size() == config.getMinNodeEntries();
	}

	private RTreeNode pickParent(RTreeNode node, Pair<RTreeNode, RTreeNode> newNodes) {
		Pair<Double, Double> areaPairFirst = newNodes.getFirst().getAreaDeltaContaining(node.getMbr());
		Pair<Double, Double> areaPairSecond = newNodes.getSecond().getAreaDeltaContaining(node.getMbr());
		if (areaPairFirst.getFirst() < areaPairSecond.getFirst()) {
			return newNodes.getFirst();
		} else if (areaPairFirst.getFirst() > areaPairSecond.getFirst()) {
			return newNodes.getSecond();
		} else if (areaPairFirst.getSecond() < areaPairSecond.getSecond()) {
			return newNodes.getFirst();
		} else if (areaPairFirst.getSecond() > areaPairSecond.getSecond()) {
			return newNodes.getSecond();
		} else {
			return newNodes.getSecond().getChildren().size() > newNodes.getFirst().getChildren().size() ? newNodes.getFirst() : newNodes.getSecond();
		}
	}

	public Pair<RTreeNode, RTreeNode> linPickSeeds(List<RTreeNode> entries) {
		List<Pair<RTreeNode, RTreeNode>> extremePairs = new ArrayList<>();
		Double totalDelta = calculateExtremePairs(entries, extremePairs);
		Pair<Pair<RTreeNode, RTreeNode>, Double> extremePair = new Pair<>(extremePairs.get(0), 0d);
		for (int dimCnt = 0; dimCnt < config.getDimension(); ++ dimCnt) {
			double normalizedDelta = getDeltaInDimension(extremePairs.get(dimCnt), dimCnt) / totalDelta;
			if (normalizedDelta > extremePair.getSecond())
				extremePair = new Pair<>(extremePairs.get(dimCnt), normalizedDelta);
		}
		entries.remove(extremePair.getFirst().getFirst());
		entries.remove(extremePair.getFirst().getSecond());
		return extremePair.getFirst();
	}

	private double getDeltaInDimension(Pair<RTreeNode, RTreeNode> extremePair, int dimCnt) {
		return extremePair.getSecond().getMbr().getBoundingRect().getMax().getCoordinateByIndex(dimCnt)
				- extremePair.getFirst().getMbr().getBoundingRect().getMin().getCoordinateByIndex(dimCnt);
	}

	/**
	 * Returns list of pairs which are extreme in some dimension. first in pair is min, second is max
	 *
	 * @param entries
	 * @return
	 */
	protected double calculateExtremePairs(List<RTreeNode> entries, List<Pair<RTreeNode, RTreeNode>> extremePairs) {
		double totalDelta = 0;
		for (int dimCnt = 0; dimCnt < config.getDimension(); ++ dimCnt) {
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			RTreeNode lowestHighSide = null;
			RTreeNode highestLowSide = null;
			double tempDelta;
			for (RTreeNode entry : entries) {
				double tempMin = entry.getMbr().getBoundingRect().getMax().getCoordinateByIndex(dimCnt);
				if (tempMin < min) {
					lowestHighSide = entry;
					min = tempMin;
				}
				double tempMax = entry.getMbr().getBoundingRect().getMin().getCoordinateByIndex(dimCnt);
				if (tempMax > max) {
					highestLowSide = entry;
					max = tempMax;
				}
				tempDelta = max - min;
				if (tempDelta > totalDelta) {
					totalDelta = tempDelta;
				}
			}
			extremePairs.add(new Pair<>(lowestHighSide, highestLowSide));
		}
		return totalDelta;
	}

	public RTreeNode linPickNext(List<RTreeNode> group) {
		RTreeNode ret = group.get(group.size() - 1);
		group.remove(group.size() - 1);
		return ret;
	}

	// saves to db and returns true if any split needed to be done
	public void adjustTree(Pair<RTreeNode, RTreeNode> pair) {
		RTreeNode N = pair.getFirst();
		RTreeNode NN = pair.getSecond();
		if(N.getId() == getRoot().getId() || N.getParentId() == 0) {
			return;
		}
		RTreeNode parent = db.getNode(N.getParentId());
		parent.updateChildMbrInParent(N);
		db.updateChildInParent(parent, N);
		parent = db.getNode(parent.getId());
		RTreeNode parent2 = null;
		if (NN != null) {
			if (parent.getChildren().size() < config.getMaxNodeEntries()) {
				db.putChild(parent.getId(), NN);
				parent = db.getNode(parent.getId());
			} else {
				Pair<RTreeNode, RTreeNode> pair2 = splitNode(parent, NN, db.isLeaf(parent.getId()));
				if(parent.getId() == getRoot().getId()){
					root = new RTreeNode(db.getNextId());
					root.addChild(pair2.getFirst());
					root.addChild(pair2.getSecond());
					db.saveNewRoot(root);
					db.deleteIndexRecord(parent);
					db.putNode(pair2.getFirst());
					db.putNode(pair2.getSecond());
					return; // no need to adjust. root reached
				}
				db.deleteIndexRecord(parent);
				pair.getFirst().setParent(parent.getParent());
				pair.getSecond().setParent(parent.getParent());
				parent = pair2.getFirst();
				parent2 = pair2.getSecond();
				db.putNode(parent);
				db.putNode(parent2);
			}
		}
		adjustTree(new Pair<>(parent, parent2));
	}

	public void condenseTree(RTreeNode leafNode) {
		RTreeNode N = leafNode;
		List<RTreeNode> Q = new ArrayList<>();
		while (! N.equals(getRoot())) {
			RTreeNode parent = N.getParent();
			if (N.getChildren().size() < config.getMinNodeEntries()) {
//				db.deleteIndexRecord(N.getId());    we want the index record to stay in db, we will just change its parent id once we find the new parent
				db.deleteChildById(parent.getId(), N.getId());
				parent = db.getNode(parent.getId());
				Q.add(N);
			} else {
				parent.updateChildMbrInParent(N);
				db.updateChildInParent(parent, N);
			}
			N = parent;
		}
		for (var node : Q) {
			insert(node, false);
		}
	}
}
