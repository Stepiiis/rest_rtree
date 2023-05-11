package cz.cvut.fit.wi.beranst6.rtreedb.config;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.CHILD_NODE_HEADER_SIZE;
import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.INDEX_HEADER_SIZE;

public class TreeConfig{
    private byte maxNodeEntries;
    private byte dimension;
    private int coordinateSize;
    private int totalIndexHeaderSize;
    private int indexNodeCoordinateSize;
    private int indexNodeSize;// first one byte is status byte specifying validity and .

    public TreeConfig(byte maxNodeEntries, byte dimension){
        this.maxNodeEntries = maxNodeEntries;
        this.dimension = dimension;
        coordinateSize=dimension * Double.BYTES;
        totalIndexHeaderSize =INDEX_HEADER_SIZE + coordinateSize * dimension;
        indexNodeCoordinateSize=dimension*coordinateSize;
        indexNodeSize = indexNodeCoordinateSize  + CHILD_NODE_HEADER_SIZE;
    }

    public byte getMaxNodeEntries() {
        return maxNodeEntries;
    }

    public byte getDimension() {
        return dimension;
    }

    public int getCoordinateSize() {
        return coordinateSize;
    }

    public int getTotalIndexHeaderSize() {
        return totalIndexHeaderSize;
    }

    public int getIndexNodeCoordinateSize() {
        return indexNodeCoordinateSize;
    }

    public int getIndexNodeSize() {
        return indexNodeSize;
    }
}

