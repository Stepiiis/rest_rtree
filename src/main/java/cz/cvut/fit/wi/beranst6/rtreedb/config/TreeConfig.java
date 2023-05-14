package cz.cvut.fit.wi.beranst6.rtreedb.config;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;

public class TreeConfig {
	private final byte maxNodeEntries;
	private final byte dimension;
	private final int coordinateSize;
	private final int totalIndexHeaderSize;
	private final int indexMBRSize;
	private final int indexChildRecordSize; // size of index node in bytes
	private final int minNodeEntries;

	public TreeConfig(byte maxNodeEntries, byte dimension) {
		this.maxNodeEntries = maxNodeEntries;
		this.minNodeEntries = (int) Math.ceil(maxNodeEntries / 2.0);
		this.dimension = dimension;
		coordinateSize = calculateCoordinateSize(dimension);
		indexMBRSize = calculateIndexNodeMBRSize(coordinateSize);
		totalIndexHeaderSize = calculateTotalIndexHeaderSize(INDEX_HEADER_SIZE, indexMBRSize);
		indexChildRecordSize = calculateIndexNodeSize(indexMBRSize, CHILD_NODE_HEADER_SIZE);
	}

	public int calculateIndexNodeSize(int indexNodeMBRSize, int childNodeHeaderSize) {
		return indexNodeMBRSize + childNodeHeaderSize;
	}

	public int calculateCoordinateSize(int dimension) {
		return dimension * Double.BYTES;
	}

	public int calculateIndexNodeMBRSize(int coordSize) {
		return 2 * coordSize;
	}

	public int calculateTotalIndexHeaderSize(int indexHeaderSize, int MBRSize) {
		return indexHeaderSize + MBRSize;
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

	public int getMBRSize() {
		return indexMBRSize;
	}

	public int getIndexChildRecordSize() {
		return indexChildRecordSize;
	}
	public int getMinNodeEntries() {
		return minNodeEntries;
	}
}

