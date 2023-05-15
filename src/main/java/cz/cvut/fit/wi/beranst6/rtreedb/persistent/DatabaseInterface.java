package cz.cvut.fit.wi.beranst6.rtreedb.persistent;


import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;

import java.util.List;

public interface DatabaseInterface {
	/**
	 * returns object from database
	 */
	RTreeNode getChildOfNodeByIndex(int idNode, int childIndex);

	// assumes that parent already holds the updated MBR and does not need to be updated.
	// returns the offset of the child node in the parent node
	// -1 if child not found in parent
	public boolean updateChildInParent(RTreeNode parent, RTreeNode child);
	/**
	 * returns object from database
	 */
	RTreeNode getNode(int id);

	/**
	 * updates object in database
	 *
	 * @param node
	 */
	void updateNode(RTreeNode node);

	/**
	 * updates MBR of the node
	 */
	void putNode(RTreeNode object);

	boolean putChild(int idNode, RTreeNode child);

	/**
	 * if db persistent and cached, then invalidate records. If db is not persistent, then it deletes the object from
	 * the node, UpdateMBR nonetheless
	 *
	 * @return
	 */
	boolean deleteChildById(int id, int childId);

	/**
	 * removes object from database
	 */
	void deleteIndexRecord(int nodeId);

	void deleteIndexRecord(RTreeNode node);

	/**
	 * returns children of the object
	 */
	RTreeNode getChild(int idNode, int childIndex);

	List<RTreeNode> getAllChildren(int idNode);

	void setChildren(int id, RTreeNode... children);

	boolean isLeaf(int id);

	boolean isIndex(int id);

	int getNextId();

	void invalidateCached(int id);

	void updateNodeHeader(RTreeNode node);

	void saveNewRoot(RTreeNode root);

	RTreeNode getRoot();

	void clearDatabase();
}
