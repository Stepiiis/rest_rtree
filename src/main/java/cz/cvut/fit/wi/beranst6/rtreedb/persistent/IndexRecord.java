package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTree;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;

public class IndexRecord {
    private int nodeCount;
    private int id;
    private int parentId;
    private byte statusByte;
    private byte headerSize;
    private byte dimension;
    private byte capacity;
    private int nodeSize;
    private RTreeRegion mbr;
    private IndexRecordNode[] children = new IndexRecordNode[Constants.NODE_CAPACITY];

    public IndexRecord(){}
    public IndexRecord(int nodeCount, int id, int parentId, byte statusByte, byte headerSize, byte dimension, byte capacity, int nodeSize, RTreeRegion mbr, IndexRecordNode[] children) {
        this.nodeCount = nodeCount;
        this.id = id;
        this.parentId = parentId;
        this.statusByte = statusByte;
        this.headerSize = headerSize;
        this.dimension = dimension;
        this.capacity = capacity;
        this.nodeSize = nodeSize;
        this.mbr = mbr;
        this.children = children;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getId() {
        return id;
    }

    public int getParentId() {
        return parentId;
    }

    public byte getStatusByte() {
        return statusByte;
    }

    public byte getHeaderSize() {
        return headerSize;
    }

    public byte getDimension() {
        return dimension;
    }

    public byte getCapacity() {
        return capacity;
    }

    public RTreeRegion getMbr() {
        return mbr;
    }

    public void setNodeCount(int nodeCount) {
        this.nodeCount = nodeCount;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public void setStatusByte(byte statusByte) {
        this.statusByte = statusByte;
    }

    public void setHeaderSize(byte headerSize) {
        this.headerSize = headerSize;
    }

    public void setDimension(byte dimension) {
        this.dimension = dimension;
    }

    public void setCapacity(byte capacity) {
        this.capacity = capacity;
    }

    public void setMbr(RTreeRegion mbr) {
        this.mbr = mbr;
    }

    public void setChildren(IndexRecordNode[] children) {
        this.children = children;
    }

    public IndexRecordNode[] getChildren() {
        return children;
    }

    public int getNodeSize() {
        return nodeSize;
    }

    public void setNodeSize(int nodeSize) {
        this.nodeSize = nodeSize;
    }
}

