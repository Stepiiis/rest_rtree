package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Service
public class InMemoryDatabase implements DatabaseInterface {
    private final Map<Integer, RTreeNode> database = new TreeMap<>();

    private final SequenceGeneratorInterface sequenceService;
    @Autowired
    public InMemoryDatabase(InMemorySequenceGenerator sequenceService) {
        this.sequenceService = sequenceService;
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
    public RTreeRegion[] getChild(int id, int index) {
        return new RTreeRegion[0];
    }

    @Override
    public RTreeNode[] getAllChildren(int idNode) {
        return new RTreeNode[0];
    }

    @Override
    public RTreeNode getChildOfNodeByIndex(int id, int index) {
        return database.get(id).getChildByIndex(index);
    }

    @Override
    public int put(int id, RTreeRegion object) {
        if (database.containsKey(id)) {
            database.get(id).addChild(object, sequenceService.getAndIncrease());
        }
        return id;
    }
}
