package cz.cvut.fit.wi.beranst6.rtreedb.persistent.util;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.IndexRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;
import static cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase.isNodeInvalid;

public class FileHandlingUtil {
	public static boolean loadRecordHeaderFromFile(IndexRecord record, RandomAccessFile file, TreeConfig config) throws IOException {
		file.seek(INDEX_HEADER_NODE_COUNT_POS);
		record.setNodeCount(file.readInt());
		file.seek(INDEX_HEADER_STATUS_POS);
		record.setStatusByte(file.readByte());
		if (isNodeInvalid(record.getStatusByte()))
			return false;
		file.seek(INDEX_HEADER_SIZE_POS);
		record.setHeaderSize(file.readByte());
		file.seek(INDEX_HEADER_DIMENSION_POS);
		record.setDimension(file.readByte());
		file.seek(INDEX_HEADER_NODE_CAPACITY_POS);
		record.setCapacity(file.readByte());
		file.seek(INDEX_HEADER_NODE_SIZE_POS);
		record.setNodeSize(file.readInt());
		file.seek(INDEX_HEADER_NODE_ID_POS);
		record.setId(file.readInt());
		file.seek(INDEX_HEADER_PARENT_ID_POS);
		record.setParentId(file.readInt());
		file.seek(INDEX_HEADER_MBR_SIZE_POS);
		record.setMbrSize(file.readInt());
		RTreeRegion mbr;
		if(!isMBRValid(record.getStatusByte()))
			mbr = null;
		else
			mbr = loadMBR(record.getHeaderSize(), file, config);
		record.setMbr(mbr);
		return true;
	}

	private static boolean isMBRValid(byte statusByte) {
		return (statusByte & 0b00000010) != 0;
	}

	public static byte invalidateMBR(byte statusByte) {
		return (byte) (statusByte & 0b11111101);
	}

	public static void writeChildNodeToFile(long offset, RTreeNode child, RandomAccessFile file, TreeConfig config) throws IOException {
		file.seek(offset + CHILD_NODE_STATUS_POS);
		file.writeByte(1);
		file.seek(offset + CHILD_NODE_ID_POS);
		file.writeInt(child.getId());
		file.seek(offset + CHILD_NODE_HEADER_SIZE_POS);
		file.writeByte(CHILD_NODE_HEADER_SIZE);
		putMBR(offset + CHILD_NODE_HEADER_SIZE, child.getMbr(), file, config);
	}

	// writes whole header including MBR of index whole record
	public static void putNewNodeHeader(RTreeNode node, RandomAccessFile file, TreeConfig config) throws IOException {
		writeStaticHeader(file, node.getId(), config);
		file.seek(INDEX_HEADER_NODE_COUNT_POS);
		file.writeInt(node.getChildren().size());
		updateIndexHeader(node, file, config);
	}

	public static void updateIndexHeader(RTreeNode node, RandomAccessFile file, TreeConfig config) throws IOException {
		file.seek(INDEX_HEADER_STATUS_POS);
		file.writeByte(node.getStatusByte());
		file.seek(INDEX_HEADER_PARENT_ID_POS);
		file.writeInt(node.getParentId());
		if(isMBRValid(node.getStatusByte()))
			putMBR(INDEX_HEADER_SIZE, node.getMbr(), file, config);
	}

	public static void writeStaticHeader(RandomAccessFile file, int nodeId, TreeConfig config) throws IOException {
		file.seek(INDEX_HEADER_MAGIC_POS);
		file.write(INDEX_HEADER_MAGIC);
		file.seek(INDEX_HEADER_SIZE_POS);
		file.writeByte(INDEX_HEADER_SIZE);
//		file.seek(INDEX_HEADER_DIMENSION_POS);
		file.writeByte(config.getDimension());
//		file.seek(INDEX_HEADER_NODE_CAPACITY_POS);
		file.writeByte(config.getMaxNodeEntries());
//		file.seek(INDEX_HEADER_NODE_SIZE_POS);
		file.writeInt(config.getIndexChildRecordSize());
//		file.seek(INDEX_HEADER_NODE_ID_POS);
		file.writeInt(nodeId);
		file.seek(INDEX_HEADER_MBR_SIZE_POS);
		file.writeInt(config.getMBRSize());
	}

	public static void putNodeCount(int nodeCount, RandomAccessFile fos) throws IOException {
		fos.seek(INDEX_HEADER_NODE_COUNT_POS);
		fos.writeInt(nodeCount);
	}

	public static void putMBR(long blockStart, RTreeRegion mbr, RandomAccessFile file, TreeConfig config) throws IOException {
		List<Coordinate> coords = mbr.getMBRasList(); // FIRST MIN PROJECTION VALUES THEN MAX
		for (int u = 0; u < 2; ++ u) {
			putCoordinate(blockStart + (long) u * config.getCoordinateSize(), coords.get(u), file, config);
		}
	}

	public static void putCoordinate(long blockStart, Coordinate coord, RandomAccessFile file, TreeConfig config) throws IOException {
		file.seek(blockStart);
		for (int i = 0; i < config.getDimension(); ++ i) {
			file.writeDouble(coord.getCoordinates()[i]);
		}
	}

	public static Coordinate getCoordinateFromFile(int blockStart, RandomAccessFile file, TreeConfig config) throws IOException {
		Coordinate coord = new Coordinate(config.getDimension());
		file.seek(blockStart);
		for (int i = 0; i < config.getDimension(); ++ i) {
			coord.getCoordinates()[i] = file.readDouble();
		}
		return coord;
	}

	public static RTreeRegion loadMBR(int blockStart, RandomAccessFile file, TreeConfig config) throws IOException {
		Coordinate min = getCoordinateFromFile(blockStart, file, config);
		Coordinate max = getCoordinateFromFile(blockStart + config.getCoordinateSize(), file, config);
		return new RTreeRegion(new BoundingBox(min, max));
	}

	// assumes big endian
	public static int getIntegerFromByteArray(byte[] data, int offset) {
		int temp = data[offset];
		temp |= data[offset + 1] >> 8;
		temp |= data[offset + 2] >> 16;
		temp |= data[offset + 3] >> 24;
		return temp;
	}

	public static byte getByteFromLong(long data, int offset) {
		return (byte) ((data & (0xFF << offset * 8)) >> offset * 8);
	}

	// ASSUMES BIG ENDIAN
	public static long getLongFromByteArray(byte[] data, int offset) {
		long temp = 0;
		for (int i = 0; i < 8; i++) {
			temp |= data[offset + i] >> (8 * i);
		}
		return temp;
	}

	// SAVES IN BIG ENDIAN
	public static byte[] getByteArrayFromLong(long data) {
		byte[] arr = new byte[8];
		for (int i = 0; i < 8; i++)
			arr[i] = getByteFromLong(data, i);
		return arr;
	}

	// uses big endian, last byte represents MSB of data
	public static byte[] getByteArrayFromInteger(int data) {
		byte[] arr = new byte[4];
		for (int i = 0; i < 4; i++)
			arr[i] = getByteFromLong(data, i);
		return arr;
	}

	public static byte[] getByteArrayFromDouble(double data) {
		long doubleBits = Double.doubleToLongBits(data);
		byte[] arr = new byte[8];
		for (int i = 0; i < 8; i++)
			arr[i] = getByteFromLong(doubleBits, i);
		return arr;
	}

	public static double getDoubleFromByteArray(byte[] data, int offset) {
		long temp = getIntegerFromByteArray(data, offset);
		temp |= ((long) getIntegerFromByteArray(data, offset + 4) >> 32);
		return Double.longBitsToDouble(temp);
	}

	public static <T> T handleFileOperation(String fileName, String mode, Logger LOGG, FileFunction<RandomAccessFile, T> foo) {
		try (RandomAccessFile raf = new RandomAccessFile(fileName, mode)) {
			return foo.apply(raf);
		} catch (FileNotFoundException e) {
			if (LOGG != null)
				LOGG.severe("File could not be opened/found: " + fileName);
			throw new DatabaseException("File could not be opened/found: " + fileName);
		} catch (IOException e) {
			if (LOGG != null)
				LOGG.severe("Error reading from file during creation rutine: " + fileName);
			throw new DatabaseException("Error reading from file during creation rutine: " + fileName);
		}
	}

	public static <T> T handleFileOperation(String fileName, String mode, FileFunction<RandomAccessFile, T> foo) {
		return handleFileOperation(fileName, mode, null, foo);
	}

	/**
	 * recursively deletes all files and folders in a directory - rm -rf style. use with caution
	 *
	 * @param indexFolder
	 */
	public static void deleteDirectory(String indexFolder, boolean leaveRoot, boolean leaveSequence) {
		File file = new File(indexFolder);
		if (file.isDirectory()) {
			for (File f : Objects.requireNonNull(file.listFiles())) {
				if (f.isDirectory())
					deleteDirectory(f.getAbsolutePath(), false, leaveSequence);
				if(leaveSequence)
					if(f.getName().equals("sequence.bin"))
						continue;
				if (! f.delete())
					throw new DatabaseException("Could not delete file: " + f.getAbsolutePath());
			}
		}
		if (! leaveRoot)
			if (file.exists())
				if (! file.delete())
					throw new DatabaseException("Could not delete file: " + file.getAbsolutePath());
	}

	public static List<RTreeRegion> loadObjectsIntoArray(String filePath, int dimension) {
		List<RTreeRegion> objects = new ArrayList<>();
		try (BufferedReader bis = new BufferedReader(new FileReader(filePath))) {
			String dim = bis.readLine().substring(2);
			int dimInt = Integer.parseInt(dim);
			if (dimInt != dimension)
				throw new DatabaseException("Dimension of data does not match dimension of tree");
			int count = Integer.parseInt(bis.readLine().substring(2));
			for (int u = 0; u < count; u++) {
				Coordinate min = new Coordinate(dimension);
				Coordinate max = new Coordinate(dimension);
				String coordPair = bis.readLine();
				String[] coords = coordPair.split("], ");
				String[] cleanedCoords = new String[coords.length];
				for (int i = 0; i < coords.length; ++ i) {
					cleanedCoords[i] = cleanWhitespace(cleanBraces(coords[i]));
				}
				String[] minCoords = cleanedCoords[0].split(",");
				String[] maxCoords = cleanedCoords[1].split(",");
				for (int i = 0; i < dimension; i++) {
					min.getCoordinates()[i] = Double.parseDouble(minCoords[i]);
					max.getCoordinates()[i] = Double.parseDouble(maxCoords[i]);
				}
				objects.add(new RTreeRegion(new BoundingBox(min, max)));
			}
		} catch (FileNotFoundException e) {
			throw new DatabaseException("File not found: " + filePath);
		} catch (IOException e) {
			throw new DatabaseException("Error reading from file: " + filePath);
		}
		return objects;
	}

	public static String cleanBraces(String s) {
		return s.replaceAll("\\[", "").replaceAll("]", "");
	}

	public static String cleanWhitespace(String s) {
		return s.replaceAll("\\s+", "");
	}
}
