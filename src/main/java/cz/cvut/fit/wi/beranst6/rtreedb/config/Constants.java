package cz.cvut.fit.wi.beranst6.rtreedb.config;

public class Constants {
    public static final String API_PREFIX = "/api";
    public static final String API_VERSION = "/v1";
    public static final String API_BASE_URL = API_PREFIX + API_VERSION;
    public static final String API_R_TREE = "/rtree";
    public static final Integer MAX_DIMENSION = 3;
    public static final Integer COORDINATE_SIZE = 3 * Double.BYTES;
    public static final Integer BIN_FILE_HEADER_SIZE = 32 + COORDINATE_SIZE * MAX_DIMENSION; // one status byte and 4 bytes for number of nodes
    public static final Integer BIN_FILE_NODE_ID_SIZE = 8;
    public static final Integer BIN_FILE_NODE_COORDINATES_SIZE = 3 * COORDINATE_SIZE;
    public static final Integer BIN_FILE_NODE_SIZE = 1 + BIN_FILE_NODE_ID_SIZE + + BIN_FILE_NODE_COORDINATES_SIZE; // first one byte is status byte specifying validity and .
}
