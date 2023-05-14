package cz.cvut.fit.wi.beranst6.rtreedb.config;

import org.springframework.beans.factory.annotation.Value;

public class Constants {

	@Value(value = "${node.maxNodeSize}")
	public static final int NODE_CAPACITY = 3;

	// inner node constants
	public static final Integer CHILD_NODE_ID_SIZE = Integer.BYTES;
	public static final Integer CHILD_NODE_ID_POS = 1;
	public static final byte CHILD_NODE_HEADER_SIZE = 16;
	public static final Integer CHILD_NODE_HEADER_SIZE_POS = 5;
	public static final Integer CHILD_NODE_STATUS_POS = 0;
	public static final Integer CHILD_NODE_STATUS_BYTES = 1;

	// index header constants
	public static final byte[] INDEX_HEADER_MAGIC = new byte[]{'S', 'T', 'E', 'P', 'I', 'S', 'S', ')'};
	public static final Integer INDEX_HEADER_MAGIC_BYTES = 8;
	public static final Integer INDEX_HEADER_MAGIC_POS = 0;
	public static final Integer INDEX_HEADER_NODE_COUNT_BYTES = 4;
	public static final Integer INDEX_HEADER_NODE_COUNT_POS = 8;
	public static final Integer INDEX_HEADER_STATUS_BYTE_BYTES = 1;
	public static final Integer INDEX_HEADER_STATUS_POS = 12;
	public static final byte INDEX_HEADER_SIZE = 32;
	public static final Integer INDEX_HEADER_SIZE_BYTES = 1;
	public static final Integer INDEX_HEADER_SIZE_POS = 13;
	public static final Integer INDEX_HEADER_DIMENSION_BYTES = 1;
	public static final Integer INDEX_HEADER_DIMENSION_POS = 14;
	public static final Integer INDEX_HEADER_NODE_CAPACITY_BYTES = 1;
	public static final Integer INDEX_HEADER_NODE_CAPACITY_POS = 15;
	public static final Integer INDEX_HEADER_ROOT_NODE_ID_BYTES = 4;
	public static final Integer INDEX_HEADER_NODE_SIZE_POS = 16;
	public static final Integer INDEX_HEADER_NODE_SIZE_BYTES = 4;
	public static final Integer INDEX_HEADER_NODE_ID_POS = 20;
	public static final Integer INDEX_HEADER_PARENT_NODE_ID_BYTES = 4;
	public static final Integer INDEX_HEADER_PARENT_ID_POS = 24;
	public static final Integer INDEX_HEADER_MBR_SIZE_POS = 28;
}
