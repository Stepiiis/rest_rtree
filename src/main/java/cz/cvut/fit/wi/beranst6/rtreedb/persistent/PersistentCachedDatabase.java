package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.*;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.SequenceGeneratorInterface;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static cz.cvut.fit.wi.beranst6.rtreedb.config.Constants.*;
import static cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil.*;

public class PersistentCachedDatabase implements DatabaseInterface {

	Logger LOGG = Logger.getLogger(PersistentCachedDatabase.class.getName());
	private final RTreeNode[] cachedDB;
	private final TreeConfig config;
	private final SequenceGeneratorInterface sequenceGen;
	private final String indexFolderPath;
	private Integer rootId = null;

	public PersistentCachedDatabase(int cacheSize, TreeConfig config, SequenceGeneratorInterface sequenceGen, String indexFolder) {
		cachedDB = new RTreeNode[cacheSize];
		this.config = config;
		this.sequenceGen = sequenceGen;
		this.indexFolderPath = "db/" + indexFolder;
		nullifyCache();
		try {
			Files.createDirectories(Paths.get(this.indexFolderPath));
		} catch (IOException e) {
			LOGG.severe("Could not create directory for index files");
			throw new DatabaseException("Could not create directory for index files");
		}
	}

	@Override
	public RTreeNode getChildOfNodeByIndex(int id, int index) {
		RTreeNode node = getNodeFromCache(id);
		if (node.getId() == id)
			node = node.getChildById(index);
		else node = null;
		return (node == null ? getNodeFromFile(id).getChildById(index) : node);
	}

	@Override
	public List<RTreeNode> getAllChildren(int id) {
		RTreeNode node = getNode(id);
		return node.getChildren();
	}

	@Override
	public void setChildren(int id, RTreeNode... children) {
		throw new Error("Not implemented yet");
	}

	private String getNodeFilePath(int id) {
		return this.indexFolderPath + "/index_" + config.getDimension() + "d_" + id + ".bin";
	}

	public void saveToCache(int id, RTreeNode node) {
		int index = id % cachedDB.length;
		cachedDB[index] = node;
	}

	public RTreeNode getNodeFromFile(int id) {
		String fileName = getNodeFilePath(id);
		return FileHandlingUtil.handleFileOperation(fileName, "r", this.LOGG, rFile -> {
			ArrayList<RTreeNode> children = new ArrayList<>();
			RTreeNode foundNode = new RTreeNode(id);
			IndexRecord record = new IndexRecord();
			if (! loadRecordHeaderFromFile(record, rFile, config)) {
				throw new DatabaseException("Index record in file " + fileName + " is invalid");
			}
			foundNode.setParentId(record.getParentId());
			for (int i = record.getHeaderSize() + record.getMbrSize(); i < rFile.length(); i += record.getNodeSize()) {
				// Child only contains bounding boxes of child nodes along with their ids. Further loading of child nodes has to be done manually if needed
				RTreeNode loadedChild = loadChild(rFile, i, config);
				if (loadedChild != null) { // means that record is invalid
					loadedChild.setParent(foundNode);
					children.add(loadedChild);
				}
			}
			foundNode.setChildren(children, record.getMbr());
			foundNode.calculateArea();
			saveToCache(id, foundNode);
			return foundNode;
		});
	}

	public void invalidateCached(int id) {
		if (cachedDB[id % cachedDB.length].getId() == id)
			cachedDB[id % cachedDB.length] = null;
	}

	@Override
	public void updateNodeHeader(RTreeNode node) {
		String fileName = getNodeFilePath(node.getId());
		handleFileOperation(fileName, "rw", LOGG, (file) -> {
			IndexRecord record = new IndexRecord();
			if (!loadRecordHeaderFromFile(record, file, config))
				throw new DatabaseException("Index record in file " + fileName + " is invalid");
			file.seek(0);
			updateIndexHeader(node, file, config);
			return null;
		});
		updateCache(node);
	}

	private void updateCache(RTreeNode node) {
		RTreeNode cachedNode = getNodeFromCache(node.getId());
		if (cachedNode != null && cachedNode.getId() == node.getId()) {
			cachedNode.setParent(node.getParent());
		}
	}

	public String getRootFileName() {
		return indexFolderPath + "/index_" + config.getDimension() + "_root.bin";
	}

	@Override
	public void saveNewRoot(RTreeNode root) {
		this.rootId = root.getId();
		putNode(root);
		String fileName = getRootFileName();
		handleFileOperation(fileName, "rw", LOGG, (file) -> {
			file.seek(0);
			file.writeInt(rootId);
			return null;
		});
	}

	@Override
	public RTreeNode getRoot() {
		try {
			if (rootId == null)
				loadRootIdFromFile();
		} catch (DatabaseException e) {
			LOGG.info("Creating new index root...");
			return null;
		}
		return getNode(rootId);
	}

	@Override
	public void clearDatabase() {
		FileHandlingUtil.deleteDirectory(indexFolderPath);
		nullifyCache();
		rootId = null;
	}

	private void nullifyCache() {
		Arrays.fill(cachedDB, null);
	}

	private void loadRootIdFromFile() {
		String fileName = getRootFileName();
		handleFileOperation(fileName, "r", LOGG, (file) -> {
			file.seek(0);
			rootId = file.readInt();
			return null;
		});
	}

	private byte getValidStatus(byte statusByte) {
		return (byte) (statusByte | 1);
	}

	public static RTreeNode loadChild(RandomAccessFile file, int readStartPos, TreeConfig config) throws IOException {
		file.seek(readStartPos);
		byte status = file.readByte();
		if (isNodeInvalid(status)) return null;
		file.seek(readStartPos + CHILD_NODE_ID_POS);
		int temp_id = file.readInt();
		file.seek(readStartPos + CHILD_NODE_HEADER_SIZE_POS);
		int headSize = file.readByte();
		RTreeRegion region = loadMBR(readStartPos + headSize, file, config);
		return new RTreeNode(temp_id, region);
	}

	@Override
	public RTreeNode getNode(int id) {
		if(id == 0)
			return null;
		RTreeNode node = getNodeFromCache(id);
		if (node == null || node.getId() != id)
			return getNodeFromFile(id);
		return node;
	}

	@Override
	public void updateNode(RTreeNode node) {
		// todo optimize
		deleteIndexRecord(node.getId());
		putNode(node);
	}

	// assumes that all child elements which have been changed are tagged
//	public void updateNodeInDb(RTreeNode node, Map<Integer, RTreeNode> changedChildren) throws IndexRecordInvalidException {
//		String fileName = getNodeFilePath(node.getId());
//		File file = new File(fileName);
//		byte[] fileData = new byte[(int) file.length() - config.getTotalIndexHeaderSize()]; // assumes standard header size
//		IndexRecord record = new IndexRecord();
//
//		// read header from file
//		if (handleFileOperation(fileName, "r", this.LOGG, randFile -> {
//			if (! loadRecordHeaderFromFile(record, randFile, config))
//				return false;
//			record.setMbr(node.getMbr());
//			randFile.seek(config.getTotalIndexHeaderSize());
//			randFile.read(fileData);
//			return true;
//		})) {
//			throw new IndexRecordInvalidException("index stored in " + fileName + " is invalid.");
//		}
//		handleFileOperation(fileName, "w", this.LOGG, (randFile) -> {
//			putMBR(record.getHeaderSize(), node.getMbr(), randFile, config);
//			final int totalHeaderOffset = record.getHeaderSize() + config.getMBRSize();
//			final int childNodeHeaderSize = fileData[totalHeaderOffset + CHILD_NODE_HEADER_SIZE_POS];
//			for (int i = totalHeaderOffset; i < fileData.length; i += config.getIndexNodeSize()) {
//				int childId = getIntegerFromByteArray(fileData, i + CHILD_NODE_ID_POS);
//				if (! changedChildren.containsKey(childId)) {
//					continue;
//				}
//				changedChildren.remove(childId);
//				putMBR(childNodeHeaderSize, node.getChildById(childId).getMbr(), randFile, config);
//			}
//			if (changedChildren.size() > 0) {
//				// todo first overwrite invalidated records inside file and then append new ones
//				for (int childId : changedChildren.keySet()) {
//					int offsetFromStart = record.getHeaderSize() + record.getNodeCount() * (record.getNodeSize() + childNodeHeaderSize);
//					putMBR(offsetFromStart, node.getChildById(childId).getMbr(), randFile, config);
//					record.setNodeCount(record.getNodeCount() + 1);
//				}
//				putNodeCount(record.getNodeCount(), randFile);
//			}
//			return null;
//		});
//		saveToCache(node);
//	}

	private void putNewRecord(RTreeNode node) {
		String fileName = getNodeFilePath(node.getId());
		handleFileOperation(fileName, "rw", this.LOGG, (file) -> {
			putNewNodeHeader(node, file, config);
			for (int i = 0; i < node.getChildren().size(); i++)
				writeChildNodeToFile(config.getTotalIndexHeaderSize() + ((long) config.getIndexChildRecordSize() * i), node.getChildren().get(i), file, config);
			return null;
		});
	}

	public void putNode(RTreeNode object) {
		putNewRecord(object);
		saveToCache(object);
	}

	@Override
	public boolean putChild(int idNode, RTreeNode child) {
		String fileName = getNodeFilePath(idNode);
		// assumes standard header size
		IndexRecord record = loadIndexRecord(fileName);
		RTreeNode node;
		try {
			node = getNodeFromFile(idNode);
		} catch (DatabaseException e) {
			return false;
		}

		node.addChild(child);
		saveToCache(node);

		return handleFileOperation(fileName, "rw", this.LOGG, randFile -> {
			// try to overwrite invalidated record inside the file first
			for (int i = record.getHeaderSize() + record.getMbrSize(), u = 0; u < record.getNodeCount(); i += record.getNodeSize(), ++ u) {
				randFile.seek(i + CHILD_NODE_STATUS_POS);
				byte status = randFile.readByte();
				randFile.seek(i + CHILD_NODE_ID_POS);
				int temp_id = randFile.readInt();
				if (isNodeInvalid(status)) {
					writeChildNodeToFile(i, child, randFile, config);
					return true;
				}
				if (temp_id == child.getId()) {
					return false;
				}
			}
			// if not found, write a new record and increase nodeCount
			writeChildNodeToFile(randFile.length(), child, randFile, config);
			record.setNodeCount(record.getNodeCount() + 1);
			putNodeCount(record.getNodeCount(), randFile);
			putMBR(record.getHeaderSize(), node.getMbr(), randFile, config);
			return true;
		});
	}

	private IndexRecord loadIndexRecord(String fileName) {
		IndexRecord record = new IndexRecord();

		boolean valid = handleFileOperation(fileName, "r", this.LOGG, (randFile) -> {
			if (! loadRecordHeaderFromFile(record, randFile, config)) {
				return false;
			}
			return true;
		});

		if (! valid) {
			throw new DatabaseException("File " + fileName + " could not be read");
		}
		return record;
	}

	// returns the offset of the child node in the parent node
	// -1 if child not found in parent
	@Override
	public boolean updateChildInParent(RTreeNode parent, RTreeNode child) {
		invalidateCached(parent.getId());
		String fileName = getNodeFilePath(parent.getId());
		IndexRecord record = loadIndexRecord(fileName);
		return handleFileOperation(fileName, "rw", this.LOGG, randFile -> {
			if (!record.getMbr().equals(parent.getMbr()))
				putMBR(INDEX_HEADER_SIZE, parent.getMbr(), randFile, config);
			// try to overwrite invalidated record inside the file first
			for (int i = record.getHeaderSize() + record.getMbrSize(), u = 0; u < record.getNodeCount(); i += record.getNodeSize(), ++ u) {
				randFile.seek(i);
				byte status = randFile.readByte();
				if (isNodeInvalid(status))
					continue;
				randFile.seek(i + CHILD_NODE_ID_POS);
				int id = randFile.readInt();
				if (id == child.getId()) {
					writeChildNodeToFile(i, child, randFile, config);
					saveToCache(parent);
					return true;
				}
			}
			return false;
		});
	}

	/**
	 * if db persistent and cached, then invalidate records
	 *
	 * @return
	 */
	@Override
	public boolean deleteChildById(int parentId, int childId) {
		String filePath = getNodeFilePath(parentId);
		RTreeNode parent = getNode(parentId);
		if (! parent.deleteChildById(childId))
			return false;
		return handleFileOperation(filePath, "rw", this.LOGG, file -> {
			IndexRecord record = new IndexRecord();
			loadRecordHeaderFromFile(record, file, config);
			putMBR(INDEX_HEADER_SIZE, parent.getMbr(), file, config);
			for (int i = 0; i < record.getNodeCount(); i += record.getNodeSize()) {
				int id = loadChildId(file, i);
				if (id == childId) {
					invalidateNode(file, i);
					return true;
				}
			}
			return false; // no need to return anything
		});
	}

	private void invalidateNode(RandomAccessFile file, int i) throws IOException {
		file.seek(i + CHILD_NODE_STATUS_POS);
		file.writeByte(getInvalidatedStatus(file.readByte())); //sets LSB to 0 => invalid node
	}

	private byte getInvalidatedStatus(byte statusByte) {
		return (byte) (statusByte & 0b00000001);
	}

	private int loadChildId(RandomAccessFile file, int start) throws IOException {
		file.seek(start + CHILD_NODE_ID_POS);
		return file.readInt();
	}

	@Override
	public void deleteIndexRecord(int id) {
		invalidateCached(id);
		getNodeFilePath(id);
		File file = new File(getNodeFilePath(id));
		if (file.exists())
			if (! file.delete())
				throw new DatabaseException("File " + file.getName() + " could not be deleted");
	}

	@Override
	public void deleteIndexRecord(RTreeNode node) {
		deleteIndexRecord(node.getId());
	}


	@Override
	public RTreeNode getChild(int id, int index) {
		RTreeNode node = getNode(id);
		return node.getChildByIndex(index);
	}

	public boolean isLeaf(int id) {
		try {
			RTreeNode node = getNodeFromFile(id); // if node is not in db, it is a leaf record of a leaf node only stored in parent node
			getNodeFromFile(node.getChildByIndex(0).getId()); // if child is not in db, it is a leaf node
		} catch (DatabaseException e) {
			return true;
		}
		return false;
	}

    @Override
    public boolean isIndex(int id) {
        try {
			RTreeNode node = getNodeFromFile(id); // if node is not in db, it is a leaf record or an indexed record only stored in parent node
		} catch (DatabaseException e) {
			return false;
		}
		return true;
    }

    @Override
	public int getNextId() {
		return sequenceGen.getAndIncrease();
	}

	// updates cache and returns the node

	public static boolean isNodeInvalid(int statusByte) {
		return (statusByte & 0x01) != 0x01;
	}

	protected static boolean isNodeInternal(int statusByte) {
		return (statusByte & 0x02) == 0x02;
	}


	private RTreeNode getChildFromCache(int id, int index) {
		return cachedDB[id % cachedDB.length].getChildById(index);
	}

	private RTreeNode getNodeFromCache(int id) {
		return cachedDB[id % cachedDB.length];
	}

	private void saveToCache(RTreeNode node) {
		cachedDB[node.getId() % cachedDB.length] = node;
	}


}
