package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;

import java.util.Map;

public class InMemoryDatabase implements DatabaseInterface {
    private Map<Integer, RTreeNode> database;

    @Override
    public RTreeNode getNode(int id) {
        throw new Error("not implemented");
    }

    @Override
    public void editBoundingBox(int id, RTreeRegion mbr) {
        throw new Error("not implemented");
    }

    @Override
    public void deleteChildByIndex(int id, int index) {
        throw new Error("not implemented");
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
