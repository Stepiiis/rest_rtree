package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class InMemoryDatabase implements DatabaseInterface {
    private final Map<Integer, RTreeNode> database = new TreeMap<>();

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
    public RTreeRegion[] getChildren(int id, int index) {
        return new RTreeRegion[0];
    }

    @Override
    public RTreeNode getChildOfNodeByIndex(int id, int index) {
        return database.get(id).getChildByIndex(index);
    }

    @Override
    public int put(int id, RTreeRegion object) {
        if (database.containsKey(id)) {
            database.get(id).addChild(object);
        }
        return object.getId();
    }
}
