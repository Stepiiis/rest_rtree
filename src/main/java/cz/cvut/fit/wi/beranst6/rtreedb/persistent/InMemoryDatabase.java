package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
public class InMemoryDatabase implements DatabaseInterface {
    private final Map<Integer, RTreeNode> database = new TreeMap<>();
    private final SequenceGeneratorInterface sequenceService;
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
        return database.get(id);
    }

    @Override
    public void updateBoundingBox(int id, Set<Integer> changedChildren) {
        database.get(id).updateMBR();
    }

    @Override
    public int putNode(int idNode, RTreeNode object) {
        database.put(idNode, new RTreeNode(idNode, object));
        return idNode;
    }

    @Override
    public void deleteChildByIndex(int id, int childIndex){
        RTreeNode child = database.get(id).getChildByIndex(childIndex);
        database.get(id).deleteChildByIndex(childIndex);
        database.remove(child.getId());
    }

    @Override
    public void delete(int id) {
        throw new Error("not implemented");
    }

    @Override
    public RTreeRegion getChild(int id, int index) {
        return database.get(id).getChildByIndex(index).getMbr();
    }

    @Override
    public List<RTreeNode> getAllChildren(int idNode) {
        return database.get(idNode).getChildren();
    }

    @Override
    public RTreeNode getChildOfNodeByIndex(int id, int index) {
        return database.get(id).getChildByIndex(index);
    }

    @Override
    public int putChild(int id, RTreeNode object) {
        if (database.containsKey(id)) {
            database.get(id).addChild(object.getId(), object.getMbr());
        }
        return id;
    }

    @Override
    public void setChildren(int id, RTreeNode... children) {
        RTreeNode node = new RTreeNode(id, children);
        database.put(id, node);
    }

    @Override
    public boolean isLeaf(int id) {
        return !database.containsKey(id);
    }

    @Override
    public int getNextId() {
        return sequenceService.getAndIncrease();
    }
}
