package cz.cvut.fit.wi.beranst6.rtreedb.persistent.util;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.IndexRecord;

import java.io.*;
import java.util.ArrayList;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;
import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.CHILD_NODE_HEADER_SIZE_POS;
import static cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase.isNodeInvalid;

public class FileHandlingUtil {
    public static boolean loadRecordHeaderFromFile(IndexRecord record, BufferedInputStream fis) throws IOException {
        byte[] data = new byte[Constants.INDEX_FILE_TOTAL_HEADER_SIZE];
        fis.read(data);
        record.setNodeCount(getIntegerFromByteArray(data, INDEX_HEADER_NODE_COUNT_POS));
        record.setStatusByte(data[INDEX_HEADER_STATUS_POS]);
        if(isNodeInvalid(record.getStatusByte()))
            return false;
        record.setHeaderSize(data[INDEX_HEADER_SIZE_POS]);
        record.setDimension(data[INDEX_HEADER_DIMENSION_POS]);
        record.setCapacity(data[INDEX_HEADER_NODE_CAPACITY_POS]);
        record.setNodeSize(getIntegerFromByteArray(data, INDEX_HEADER_NODE_SIZE_POS));
        record.setId(getIntegerFromByteArray(data,INDEX_HEADER_ROOT_NODE_ID_POS));
        record.setParentId(getIntegerFromByteArray(data,INDEX_HEADER_PARENT_NODE_ID_POS));
        RTreeRegion mbr = getAllCoordsInRegion(record.getId(), record.getHeaderSize(), data);
        record.setMbr(mbr);
        return true;
    }

    public static void writeChildNodeToFile(int offset, RTreeNode child, BufferedOutputStream fos) throws IOException {
        fos.write(new byte[] {(byte)1}, offset + CHILD_NODE_STATUS_POS, CHILD_NODE_STATUS_BYTES);
        fos.write(getByteArrayFromInteger(child.getId()), offset + CHILD_NODE_ID_POS, CHILD_NODE_ID_SIZE);
        fos.write(new byte[] { CHILD_NODE_HEADER_SIZE}, offset + CHILD_NODE_HEADER_SIZE_POS, 1);
        putAllCordsFromCoordArray(offset+CHILD_NODE_HEADER_SIZE, child.getMbrArr(), fos);
    }

    public static void writeHeaderOfFile(RTreeNode node, BufferedOutputStream fos) throws IOException{
        fos.write(INDEX_HEADER_MAGIC, INDEX_HEADER_MAGIC_POS, INDEX_HEADER_MAGIC_BYTES);
        fos.write(getByteArrayFromInteger(node.getChildren().length), INDEX_HEADER_NODE_COUNT_POS, INDEX_HEADER_NODE_COUNT_BYTES);
        fos.write(new byte[] {(byte)1}, INDEX_HEADER_STATUS_POS, INDEX_HEADER_STATUS_BYTE_BYTES);
        fos.write(new byte[] {INDEX_HEADER_SIZE}, INDEX_HEADER_SIZE_POS, INDEX_HEADER_SIZE_BYTES);
        fos.write(new byte[] {CURR_DIMENSION}, INDEX_HEADER_DIMENSION_POS, INDEX_HEADER_DIMENSION_BYTES);
        fos.write(new byte[] {CURR_MAX_CAPACITY}, INDEX_HEADER_NODE_CAPACITY_POS, INDEX_HEADER_NODE_CAPACITY_BYTES);
        fos.write(getByteArrayFromInteger(INDEX_FILE_NODE_SIZE), INDEX_HEADER_NODE_SIZE_POS, INDEX_HEADER_NODE_SIZE_BYTES);
        fos.write(getByteArrayFromInteger(node.getId()), INDEX_HEADER_ROOT_NODE_ID_POS, INDEX_HEADER_ROOT_NODE_ID_BYTES);
        fos.write(getByteArrayFromInteger(node.getParentId()), INDEX_HEADER_PARENT_NODE_ID_POS, INDEX_HEADER_PARENT_NODE_ID_BYTES);
    }

    public static void putNodeCount(int nodeCount, BufferedOutputStream fos) throws IOException {
        fos.write(getByteArrayFromInteger(nodeCount), INDEX_HEADER_NODE_COUNT_POS, INDEX_HEADER_NODE_COUNT_BYTES);
    }

    public static int getNodeId(byte[] data){
        return getIntegerFromByteArray(data, 0);
    }
    public static int getNodeSize(byte[] data){
        return getIntegerFromByteArray(data, 4);
    }

    public static void putAllCordsFromCoordArray(int blockStart, Coordinate[] coords, BufferedOutputStream fos) throws IOException {
        for(int u = 0; u < CURR_DIMENSION; ++u){
            putCoordinate(blockStart + u * COORDINATE_SIZE, coords[u], fos);
        }
    }

    public static void putCoordinate(int blockStart, Coordinate coord, BufferedOutputStream fos) throws IOException {
        for(int i = 0; i < CURR_DIMENSION; ++i){
            fos.write(getByteArrayFromDouble(coord.getCoordinates()[i]), blockStart + i * COORDINATE_SIZE, Double.BYTES);
        }
    }

    public static RTreeRegion getAllCoordsInRegion(int id, int blockStart, byte[] data){
        Coordinate[] coords = new Coordinate[CURR_DIMENSION];
        for(int u = 0; u< CURR_DIMENSION; ++u){
            double[] point = new double[CURR_DIMENSION];
            for(int i = 0; i < CURR_DIMENSION; ++i){
                point[i] = getDoubleFromByteArray(data, blockStart + i * Double.BYTES);
            }
            coords[u] = new Coordinate(point);
        }
        return new RTreeRegion(coords);
    }

    // assumes big endian
    public static int getIntegerFromByteArray(byte[] data, int offset){
        int temp = data[offset];
        temp |= data[offset + 1]>>8;
        temp |= data[offset + 2]>>16;
        temp |= data[offset + 3]>>24;
        return temp;
    }

    public static byte getByteFromLong(long data, int offset){
        return (byte) ((data & ( 0xFF << offset*8))>>offset*8);
    }

    // ASSUMES BIG ENDIAN
    public static long getLongFromByteArray(byte[] data, int offset){
        long temp = 0;
        for(int i = 0; i < 8 ; i++){
            temp |= data[offset + i]>>(8*i);
        }
        return temp;
    }

    // SAVES IN BIG ENDIAN
    public static byte[] getByteArrayFromLong(long data){
        byte[] arr = new byte[8];
        for( int i = 0; i < 8 ; i ++)
            arr[i] = getByteFromLong(data, i);
        return arr;
    }

    // uses big endian, last byte represents MSB of data
    public static byte[] getByteArrayFromInteger(int data){
        byte[] arr = new byte[4];
        for( int i = 0; i < 4 ; i ++)
            arr[i] = getByteFromLong(data, i);
        return arr;
    }
    public static byte[] getByteArrayFromDouble(double data){
        long doubleBits = Double.doubleToLongBits(data);
        byte[] arr = new byte[8];
        for( int i = 0; i < 8 ; i ++)
            arr[i] = getByteFromLong(doubleBits, i);
        return arr;
    }

    public static double getDoubleFromByteArray(byte[] data, int offset){
        long temp = getIntegerFromByteArray(data, offset);
        temp |= ((long)getIntegerFromByteArray(data, offset+4) >> 32);
        return Double.longBitsToDouble(temp);
    }
}
