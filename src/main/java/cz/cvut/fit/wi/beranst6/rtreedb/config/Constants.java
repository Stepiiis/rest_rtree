package cz.cvut.fit.wi.beranst6.rtreedb.config;

import org.springframework.beans.factory.annotation.Value;

public class Constants {

    @Value(value = "${node.maxNodeSize}")
    public static final int NODE_CAPACITY = 3;
    public static byte CURR_DIMENSION = 3; // NOT REALLY A CONSTANT, BUT IS USED AS ONE
    public static byte CURR_MAX_CAPACITY; // NOT REALLY A CONSTANT, BUT IS USED AS ONE

    // inner node constants
    public static final Integer CHILD_NODE_ID_SIZE = Integer.BYTES;
    public static final Integer CHILD_NODE_ID_POS = 1;
    public static final byte CHILD_NODE_HEADER_SIZE = 16;
    public static final Integer CHILD_NODE_HEADER_SIZE_POS = 5;
    public static final Integer CHILD_NODE_STATUS_POS = 0;
    public static final Integer CHILD_NODE_STATUS_BYTES = 1;

    // index header constants
    public static final byte[] INDEX_HEADER_MAGIC =new byte[]{'S','T','E','P','I','S','S'};
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
    public static final Integer INDEX_HEADER_ROOT_NODE_ID_POS = 20;
    public static final Integer INDEX_HEADER_PARENT_NODE_ID_BYTES = 4;
    public static final Integer INDEX_HEADER_PARENT_NODE_ID_POS = 24;
    public static final Integer INDEX_HEADER_RESERVED_BYTES = 4; // 4 empty bytes for reserve
    public static final Integer COORDINATE_SIZE = CURR_DIMENSION * Double.BYTES;
    public static final Integer INDEX_FILE_TOTAL_HEADER_SIZE = INDEX_HEADER_SIZE + COORDINATE_SIZE * CURR_DIMENSION; // HEADER and bounding box of this node
    public static final Integer INDEX_FILE_NODE_COORDINATES_SIZE = CURR_DIMENSION * COORDINATE_SIZE;
    public static final Integer INDEX_FILE_NODE_SIZE = CHILD_NODE_HEADER_SIZE + INDEX_FILE_NODE_COORDINATES_SIZE; // first one byte is status byte specifying validity and .
}
