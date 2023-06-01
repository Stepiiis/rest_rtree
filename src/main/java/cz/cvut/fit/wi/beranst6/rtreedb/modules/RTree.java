package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.ObjectFittingUtil;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Pair;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.*;

import java.util.*;

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

	public List<RTreeNode> kNN(Coordinate point, int k, kNNType implementation) {
		if (point.getDimension() < config.getDimension()) {
			Double[] newCoords = new Double[config.getDimension()];
			for (int i = 0; i < point.getDimension(); i++) {
				newCoords[i] = point.getCoordinateByIndex(i);
			}
			point = new Coordinate(newCoords);
		}
		RTreeRegion region = new RTreeRegion(point, point);
		return kNN(region, k, implementation);
	}

	public List<RTreeNode> kNN(RTreeRegion region, int k, kNNType implementation) {
		return switch (implementation) {
			case BETTER_BFS -> kNNBFS(region, k);
			case M_TREE_LIKE -> kNNMTreeLike(region, k);
			default -> throw new DatabaseException("Unknown kNN implementation");
		};
	}

	//
	private List<RTreeNode> kNNMTreeLike(RTreeRegion queryRegion, int k) {
		throw new UnsupportedOperationException("Not implemented. Use BETTER_BFS.");
//        PriorityQueue<RTreeNode> q = new PriorityQueue<>(Comparator.comparingDouble(val -> calculateMinDist(queryRegion,val.getMbr()))); // lowest distance to region stored first
//        List<RTreeNode> kNearest = new ArrayList<>(k);
//        q.add(getRoot());
//        while (!q.isEmpty()) {
//            RTreeNode nextNode = q.poll();
//            kNNSearchNode(nextNode, queryRegion, q, kNearest);
//        }
//        return kNearest;
	}

//    private void kNNSearchNode(RTreeNode nextNode, RTreeRegion queryRegion, PriorityQueue<RTreeNode> q, List<RTreeNode> kNearest) {
//        RTreeNode parent = db.getNode(nextNode.getParentId());
//        double parentDistance = 0;
//        if (parent != null) // nextNode is root
//            parentDistance = calculateMinDist(queryRegion, parent.getMbr());
//        if (db.isLeaf(nextNode.getId())) {
//            for (RTreeNode child : nextNode.getChildren()) {
//
//            }
//        } else {
//
//        }
//    }


	public List<RTreeNode> kNNBFS(RTreeRegion queryRegion, int k) {
		PriorityQueue<Pair<RTreeNode, Double>> q = new PriorityQueue<>(Comparator.comparingDouble(Pair::getSecond)); // lowest distance to region stored first
		PriorityQueue<Pair<RTreeNode, Double>> result = new PriorityQueue<>(Comparator.comparing(Pair::getSecond, Comparator.reverseOrder())); // highest distance region stored first
		if (getRoot() == null)
			return new ArrayList<>(0);  // empty tree
		q.add(new Pair<>(getRoot(), calculateMinDist(queryRegion, getRoot().getMbr())));
		while (! q.isEmpty()) {
			RTreeNode currNode = q.poll().getFirst();
			for (RTreeNode child : currNode.getChildren().parallelStream().sorted(Comparator.comparingDouble(val -> calculateMinDist(queryRegion, val.getMbr()))).toList()) {
				double currMax = getCurrentMaxInRes(result);
				double childDistance = calculateMinDist(queryRegion, currNode.getMbr());
				if (childDistance > currMax && result.size() == k) // jakmile dalsi deti jsou vetsi nez soucasne max z K dětí, tak nemusíme prohledávat dále.
					break;
				searchNode(child, childDistance, k, q, result);
			}
		}
		return result.stream().map(Pair::getFirst).toList();
	}


	private void searchNode(RTreeNode child, double childDistance, int k, PriorityQueue<Pair<RTreeNode, Double>> q, PriorityQueue<Pair<RTreeNode, Double>> result) {
		if (db.isIndex(child.getId())) {
			q.add(new Pair<>(db.getNode(child.getId()), childDistance));
			return;
		} else if (db.isLeaf(child.getId())) {
			if (result.size() < k)
				result.add(new Pair<>(child, childDistance));
			else {
				Pair<RTreeNode, Double> currentMax = result.peek();
				if (currentMax != null && currentMax.getSecond() > childDistance) {
					result.poll();
					result.add(new Pair<>(child, childDistance));
				}
			}
			return;
		}
		throw new UnsupportedOperationException("Node neither index nor leaf. Should never happen!");
	}

	public double getCurrentMaxInRes(PriorityQueue<Pair<RTreeNode, Double>> resArray) {
		Pair<RTreeNode, Double> currentMax = resArray.peek();
		if (currentMax == null)
			return Double.MAX_VALUE;
		return currentMax.getSecond();
	}

//    private List<RTreeNode> getOverlappingIndexesImpl(RTreeNode node, RTreeRegion region, int k) {
//        List<RTreeNode> resultList = new ArrayList<>();
//        for (RTreeNode child : node.getChildren().stream().sorted(Comparator.comparingDouble(val -> calculateMinDist(region, val.getMbr()))).toList()) {
//            if (resultList.size() >= k)
//                return resultList;
//            if (db.isIndex(child.getId())) {
//                resultList.addAll(getOverlappingIndexesImpl(db.getNode(child.getId()), region, k));
//            } else if (db.isLeaf(child.getId())) {
//                if (ObjectFittingUtil.SAT(region, child.getMbr()))
//                    resultList.add(child);
//            }
//        }
//        return resultList;
//    }

	private double calculateMinDist(RTreeRegion queryRegion, RTreeRegion region) {
		if (ObjectFittingUtil.SAT(region, queryRegion))
			return 0;
		double minDist = 0;
		for (int i = 0; i < config.getDimension(); ++ i) {
			double pointVal = queryRegion.getBoundingRect().getMin().getCoordinateByIndex(i);
			BoundingBox boundingBox = region.getBoundingRect();
			double ri = pointVal < boundingBox.getMinByAxis(i) ? boundingBox.getMinByAxis(i) : (Math.min(pointVal, boundingBox.getMaxByAxis(i)));
			minDist += Math.pow(Math.abs(pointVal - ri), 2);
		}
		return minDist;
	}

	private double calculateMinMaxDistance(RTreeRegion queryRegion, RTreeRegion region) {
		if (ObjectFittingUtil.SAT(queryRegion, region)) {
			return 0;
		}
		double maxDist = Double.MAX_VALUE;
		double S = 0;
		BoundingBox regionBox = region.getBoundingRect();
		for (int i = 0; i < config.getDimension(); ++ i) {
			double pointVal = queryRegion.getBoundingRect().getMaxByAxis(i);
			double rMi = getrMi(pointVal, regionBox.getMinByAxis(i), regionBox.getMaxByAxis(i));
			S += Math.pow(Math.abs(pointVal - rMi), 2);
		}
		for (int k = 0; k < config.getDimension(); ++ k) {
			double pointVal = queryRegion.getBoundingRect().getMaxByAxis(k);
			double rmk = getrmi(pointVal, regionBox.getMinByAxis(k), regionBox.getMaxByAxis(k));
			double rMk = getrMi(pointVal, regionBox.getMinByAxis(k), regionBox.getMaxByAxis(k));
			maxDist = Math.min(maxDist, S - Math.pow(Math.abs(pointVal - rMk), 2) - Math.pow(Math.abs(pointVal - rmk), 2));
		}
		return maxDist;
	}

	// viz page 4 Russopoulos et al https://dl.acm.org/doi/pdf/10.1145/223784.223794
	private double getrmi(double pointVal, double min, double max) {
		return pointVal <= ((min + max) / 2) ? min : max;
	}

	// viz page 4 Russopoulos et al https://dl.acm.org/doi/pdf/10.1145/223784.223794
	private double getrMi(double pointVal, double min, double max) {
		return pointVal >= ((min + max) / 2) ? min : max;
	}

	public int insert(RTreeRegion region) {
		int id = db.getNextId();
		insert(new RTreeNode(id, region), true);
		return id;
	}

	public int insert(BoundingBox box) {
		return insert(new RTreeRegion(box));
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
			node.setParentId(leaf.getId());
			db.putChild(leaf.getId(), node);
			leaf = db.getNode(leaf.getId());
			adjustTree(new Pair<>(leaf, null));
		} else {
			Pair<RTreeNode, RTreeNode> pair = splitNode(leaf, node, true);
			boolean splitRoot = false;
			if (isRoot(leaf)) {
				// split of root, remove old and create new one;
				root = new RTreeNode(db.getNextId());
				root.setDepth(leaf.getDepth() + 1);
				root.addChild(pair.getFirst());
				root.addChild(pair.getSecond());
				db.saveNewRoot(root);
				splitRoot = true;
			} else {
				pair.getFirst().setParentId(leaf.getParentId());
				pair.getSecond().setParentId(leaf.getParentId());
			}
			db.deleteIndexRecord(leaf);
			db.putNode(pair.getFirst());
			db.putNode(pair.getSecond());
			if (! splitRoot)
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
			if (ObjectFittingUtil.SAT(region, child.getMbr())) {
				if (isLeaf)
					result.add(child);
				else
					result.addAll(searchImpl(child, region));
			}
		}
		return result;
	}

	public List<RTreeNode> rangeQuery(RTreeRegion region) {
		return rangeQueryImpl(getRoot(), region);
	}

	private List<RTreeNode> rangeQueryImpl(RTreeNode node, RTreeRegion region) {
		ArrayList<RTreeNode> result = new ArrayList<>();
		if (node == null)
			return result;
		boolean isLeaf = db.isLeaf(node.getId());
		List<RTreeNode> children = db.getAllChildren(node.getId());
		for (var child : children) {
			if (isLeaf) {
				if (ObjectFittingUtil.SAT(child.getMbr(), region))
					result.add(child);
				continue;
			}
			if (ObjectFittingUtil.intersects(child.getMbr(), region)){
				result.addAll(rangeQueryImpl(child, region));
			}
		}
		return result;
	}

	public boolean delete(RTreeRegion entry) {
		RTreeNode L = findLeaf(entry);
		if (L == null)
			return false;
		L = db.getNode(L.getId());
		RTreeNode child = findChildContainingRegion(L.getChildren(), entry);

		if (child == null)
			return false;
		L.removeChild(child);
		if (! db.deleteChildById(L, child.getId()))
			return false;
		L = db.getNode(L.getId());
		int depth = getDepth(child);
		condenseTree(L, depth);
		if (getRoot() != null && this.getRoot().getChildren().size() == 1) { // growing tree smaller because root is virtually empty
			RTreeNode newRoot = getRoot().getChildren().get(0);
			this.root = newRoot;
			db.saveAsRoot(root);
		}
		return true;
	}

	private RTreeNode getRoot() {
		this.root = db.getRoot();
		return this.root;
	}

	private int getDepth(RTreeNode node) {
		int i = 0;
		if (node == null)
			return 0;
		while (node.getParentId() != 0) {
			i++;
			node = db.getNode(node.getParentId());
		}
		return i;
	}

	private RTreeNode findLeaf(RTreeRegion entry) {
		return findLeafImpl(getRoot(), entry);
	}

	private RTreeNode findLeafImpl(RTreeNode node, RTreeRegion entry) {
		if (db.isLeaf(node.getId())) {
			return node; // null means not found
		}
		List<RTreeNode> children = db.getAllChildren(node.getId());
		RTreeNode bestChild = findChildContainingRegion(children, entry);
		if (bestChild == null)
			throw new DatabaseException("Given node has no children. *Should never happen.* NodeID=" + node.getId() + " Entry=" + entry + " Node=" + node);
		return findLeafImpl(bestChild, entry);
	}

	private RTreeNode findChildContainingRegion(List<RTreeNode> children, RTreeRegion entry) {
		for (RTreeNode child : children) {
			if (ObjectFittingUtil.SAT(entry, child.getMbr()))
				return child;
		}
		return null;
	}


	public RTreeNode chooseNode(RTreeNode entry, boolean increasingDepth) {
		return chooseNodeImpl(getRoot(), entry, increasingDepth, 0);
	}


	private RTreeNode chooseNodeImpl(RTreeNode node, RTreeNode entry, boolean increasingDepth, int currDepth) {
		if (db.isLeaf(node.getId()))
			return node;
		if (! increasingDepth) { // if we are adding node that was previously removed, we need to put it into the same depth.
			if (currDepth == entry.getDepth() - 1) // this way we find parent node to which we will add the node so that the depth remains correct
				return node;
		} else {
			entry.setDepth(node.getDepth() + 1);
		}
		List<RTreeNode> children = db.getAllChildren(node.getId());
		RTreeNode bestChild = getBestEnlargedChildContaining(children, entry.getMbr());
		if (bestChild == null)
			throw new DatabaseException("Given node has no children. NodeID=" + node.getId() + " Entry=" + entry + " Node=" + node);
		return chooseNodeImpl(bestChild, entry, increasingDepth, currDepth + 1);
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
		newNodes.getFirst().setDepth(node.getDepth());
		newNodes.getSecond().setDepth(node.getDepth());
		newNodes.getFirst().addChild(new RTreeNode(seeds.getFirst().getId(), seeds.getFirst().getMbr().copy())); // TODO: check if copy of children is needed. likely not as we are not modifying the children
		newNodes.getSecond().addChild(new RTreeNode(seeds.getSecond().getId(), seeds.getSecond().getMbr().copy()));
		if (! isLeaf) {
			RTreeNode DBFirstSeed = db.getNode(seeds.getFirst().getId());
			RTreeNode DBSecondSeed = db.getNode(seeds.getSecond().getId());
			DBFirstSeed.setParentId(newNodes.getFirst().getId());
			DBSecondSeed.setParentId(newNodes.getSecond().getId());
			db.updateNodeHeader(DBFirstSeed);
			db.updateNodeHeader(DBSecondSeed);
		}
		while (children.size() > 0) {
			if (isAnyOfGroupTooSmall(newNodes, children)) {
				addAllToGroupNeeded(children, newNodes, isLeaf);
				return newNodes;
			}
			node = linPickNext(children);
			RTreeNode parent = pickParent(node, newNodes);
			parent.addChild(node);
			if (! isLeaf) {
				// no need to set parent. already done in line above ... addChild(node)
				db.updateNodeHeader(node);
			}
		}
		return newNodes;
	}

	private void addAllToGroupNeeded(List<RTreeNode> children, Pair<RTreeNode, RTreeNode> newNodes, boolean isLeaf) {
		RTreeNode smallestNode = newNodes.getFirst().getChildren().size() > newNodes.getSecond().getChildren().size() ? newNodes.getSecond() : newNodes.getFirst();
		for (RTreeNode node : children) {
			smallestNode.addChild(node);
			if (! isLeaf) {
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
			double max = - Double.MAX_VALUE;
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
	private void adjustTree(Pair<RTreeNode, RTreeNode> pair) {
		RTreeNode N = pair.getFirst();
		RTreeNode NN = pair.getSecond();
		RTreeNode parent = db.getNode(N.getParentId());
		if (parent == null) {
			// root reached and no split needed to be done
			return;
		}
		parent.updateChildMbrInParent(N);
		db.updateChildInParent(parent, N);
		parent = db.getNode(parent.getId());
		RTreeNode parent2 = null;
		if (NN != null) {
			if (parent.getChildren().size() < config.getMaxNodeEntries()) {
				db.putChild(parent.getId(), NN);
				parent = db.getNode(parent.getId());
				if (parent.getId() == getRoot().getId()) {
					return; // no need to adjust. root reached
				}
			} else {
				Pair<RTreeNode, RTreeNode> pair2 = splitNode(parent, NN, db.isLeaf(parent.getId()));
				db.deleteIndexRecord(parent);
				if (isRoot(parent)) {
					root = new RTreeNode(db.getNextId());
					root.setDepth(parent.getDepth() + 1);
					root.addChild(pair2.getFirst());
					root.addChild(pair2.getSecond());
					db.saveNewRoot(root);
					db.putNode(pair2.getFirst());
					db.putNode(pair2.getSecond());
					return; // no need to adjust. root reached
				}
				pair2.getFirst().setParentId(parent.getParentId());
				pair2.getSecond().setParentId(parent.getParentId());
				parent = pair2.getFirst();
				parent2 = pair2.getSecond();
				db.putNode(parent);
				db.putNode(parent2);
			}
		}
		adjustTree(new Pair<>(parent, parent2));
	}

	public int getNodeDepth(RTreeNode node) {
		if (node == null) {
			return 0;
		}
		int i = 0;
		while (node.getChildren().size() > 0) {
			i++;
			if (db.isIndex(node.getChildren().get(0).getId()))
				node = db.getNode(node.getChildren().get(0).getId()); // should not matter which children as all are at same depth
			else {
				break;
			}
		}
		return i;
	}

	public int getHeight(RTreeNode node) {
		int i = 0;
		db.getNode(node.getId());
		for (RTreeNode child : node.getChildren()) {
			if (db.isIndex(child.getId())) {
				i = Math.max(i, getHeight(child));
			}
		}
		return i;
	}

	public void condenseTree(RTreeNode leafNode, int totalDepth) {
		RTreeNode N = leafNode;
		int depth = totalDepth;
		List<RTreeNode> Q = new ArrayList<>();
		while (! isRoot(N)) {
			RTreeNode parent = db.getNode(N.getParentId());
			if (N.getChildren().size() < config.getMinNodeEntries()) {
				db.deleteIndexRecord(N.getId());   // we want the index record to be removed from db, as we will reinsert all children
//				RTreeRegion parentMbr = parent.getMbr();
				parent.removeChild(N);
//				RTreeRegion newParentMbr = parent.getMbr();
				db.deleteChildById(parent, N.getId());
				parent = db.getNode(parent.getId());
				N.setDepth(depth);
				Q.add(N);
//				if (parentMbr.equals(newParentMbr)) // if parent has changed, either to empty or smaller, we need to adjust
//					break;// MBR has not changed. no need to adjust any further
			} else {
//				RTreeRegion parentMbr = parent.getMbr();
				parent.updateChildMbrInParent(N);
//				RTreeRegion newParentMbr = parent.getMbr();
//				if (parentMbr.equals(newParentMbr))
//					break;
				db.updateChildInParent(parent, N);
			}
			N = parent;
			depth--;
		}
		for (var node : Q) {
			for (var child : node.getChildren()) {
				child.setDepth(node.getDepth() + 1);
				if (! db.isIndex(child.getId()))
					insert(child, true);
				else
					insert(node, false);
			}
		}
	}

	private boolean isRoot(RTreeNode node) {
		if (node == null) return false;
		return node.getParentId() == 0 || node.equals(getRoot());
	}

	public void clearDB() {
		db.clearDatabase(true,false);
	}
}
