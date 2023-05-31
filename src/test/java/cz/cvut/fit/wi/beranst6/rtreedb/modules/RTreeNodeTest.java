package cz.cvut.fit.wi.beranst6.rtreedb.modules;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Pair;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RTreeNodeTest {

	@Test
	void testGetAreaDeltaContaining() {
		RTreeNode testFitNode = new RTreeNode(-1, new RTreeRegion(new Coordinate(5d,5d), new Coordinate(8d,8d)));

		List<RTreeNode> nodes = new ArrayList<>(List.of(
				new RTreeNode(-1, new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(10d, 10d))),
				new RTreeNode(-1, new RTreeRegion(new Coordinate(0d, 0d), new Coordinate(3d, 3d))),
				new RTreeNode(-1, new RTreeRegion(new Coordinate(10d, 10d), new Coordinate(15d, 15d)))
		));
		for(RTreeNode node : nodes){
			node.addChild(node);
		}
		List<Pair<Double,Double>> resList = new ArrayList<>();
		resList.addAll(List.of(
				new Pair<>(0d,100d),
				new Pair<>(55d,64d),
				new Pair<>(75d,100d)
		));
		for(int i = 0; i < nodes.size(); i++){
			assertEquals(resList.get(i), nodes.get(i).getAreaDeltaContaining(testFitNode.getMbr()));
		}

		assertEquals(nodes.get(0),RTree.getBestEnlargedChildContaining(nodes, testFitNode.getMbr()));


	}

}