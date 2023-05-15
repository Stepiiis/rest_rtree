package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.InMemorySequenceGenerator;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.SequenceGeneratorInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class InMemoryDatabase implements DatabaseInterface {
	private final Map<Integer, RTreeNode> database = new TreeMap<>();
	private final SequenceGeneratorInterface sequenceService;
	private int rootId;
	private TreeConfig config;

	@Autowired
	public InMemoryDatabase(InMemorySequenceGenerator sequenceService) {
		this.sequenceService = sequenceService;
	}

	public void setConfig(TreeConfig config) {
		this.config = config;
	}

	@Override
	public RTreeNode getNode(int id) {
		if(id == 0)
			return null;
		return database.get(id);
	}

	@Override
	public void updateNode(RTreeNode node) {
		database.put(node.getId(), node);
	}

	@Override
	public void putNode(RTreeNode object) {
		database.put(object.getId(), object);
	}

	@Override
	public boolean deleteChildById(int parentId, int childId) {
		RTreeNode node = database.get(parentId);
		if (node != null)
			return node.deleteChildById(childId);
		return false;
	}


	@Override
	public void deleteIndexRecord(int id) {
		database.remove(id);
	}

	@Override
	public void deleteIndexRecord(RTreeNode node) {
		database.remove(node.getId());
	}

	@Override
	public RTreeNode getChild(int id, int childIndex) {
		return database.get(id).getChildByIndex(childIndex);
	}

	@Override
	public List<RTreeNode> getAllChildren(int idNode) {
		return database.get(idNode).getChildren();
	}

	@Override
	public RTreeNode getChildOfNodeByIndex(int id, int index) {
		return database.get(id).getChildById(index);
	}

	@Override
	public boolean updateChildInParent(RTreeNode parent, RTreeNode child) {
		database.get(parent.getId()).updateChildMbrInParent(child);
		return true;
	}

	@Override
	public boolean putChild(int id, RTreeNode object) {
		if (database.containsKey(id)) {
			database.get(id).addChild(object.getId(),object.getMbr().copy());
			return true;
		}
		return false;
	}

	@Override
	public void setChildren(int id, RTreeNode... children) {
		RTreeNode node = new RTreeNode(id, List.of(children));
		database.put(id, node);
	}

	@Override
	public boolean isLeaf(int id) {
		return ! database.containsKey(id);
	}

    @Override
    public boolean isIndex(int id) {
        return database.containsKey(id);
    }

    @Override
	public int getNextId() {
		return sequenceService.getAndIncrease();
	}

	@Override
	public void invalidateCached(int id) {
		// do nothing as cache not present
	}

	@Override
	public void updateNodeHeader(RTreeNode node) {
		if(!database.containsKey(node.getId()))
			return;
		RTreeNode record = database.get(node.getId());
		record.setMbr(node.getMbr().copy());
		record.setParent(node.getParent());
	}

	@Override
	public void saveNewRoot(RTreeNode root) {
		database.put(root.getId(),root);
		rootId = root.getId();
	}

	@Override
	public RTreeNode getRoot() {
		return null;
	}

	@Override
	public void clearDatabase() {
		database.clear();
	}
}
