package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.exception.DatabaseException;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.SequenceGeneratorInterface;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.util.FileHandlingUtil;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class LinearDB {
	private final TreeConfig config;
	private final SequenceGeneratorInterface sequence;
	private final String dbPath;
	private final IOMonitoring monitoring;

	public LinearDB(TreeConfig config, String dbName, SequenceGeneratorInterface sequence, IOMonitoring monitoring) {
		this.config = config;
		this.dbPath = "db/" + dbName;
		this.sequence = sequence;
		this.monitoring = monitoring;
		if (!Files.isDirectory(Paths.get(dbName)))
			create();
	}

	public int getDbSize() {
		return sequence.getCurrentValue();
	}

	private void create() {
		try {
			Files.createDirectory(Paths.get(dbPath));
		} catch (IOException e) {
			throw new DatabaseException("Could not create db directory.");
		}
	}

	public RTreeRegion getNode(int id) {
		monitoring.hitRead();
		return FileHandlingUtil.handleFileOperation(dbPath + "/" + id + ".bin", "r", this::readNode);
	}

	public int putNode(RTreeRegion region) {
		monitoring.hitWrite();
		int id = sequence.getAndIncrease();
		FileHandlingUtil.handleFileOperation(dbPath + "/" + id + ".bin", "rw", file -> {
			writeNode(region, file);
			return 0;
		});
		return id;
	}

	private RTreeRegion readNode(RandomAccessFile file) throws IOException {
		return new RTreeRegion(readMBR(file));
	}

	private BoundingBox readMBR(RandomAccessFile file) throws IOException {
		List<Double> coordinates = new ArrayList<>();
		List<Coordinate> coordinatesList = new ArrayList<>();
		file.seek(0);
		for (int i = 0; i < 2; ++ i) {
			coordinates.clear();
			for (int j = 0; j < config.getDimension(); ++ j) {
				coordinates.add(file.readDouble());
			}
			coordinatesList.add(new Coordinate(coordinates.toArray(new Double[0])));
		}
		return new BoundingBox(coordinatesList.get(0), coordinatesList.get(1));
	}

	private void writeNode(RTreeRegion node, RandomAccessFile file) throws IOException {
		writeMBR(node.getMBRasList(), file);
	}

	private void writeMBR(List<Coordinate> boundingRect, RandomAccessFile file) throws IOException {
		file.seek(0);
		for (Coordinate coordinate : boundingRect) {
			for (Double value : coordinate.getCoordinates()) {
				file.writeDouble(value);
			}
		}
	}
}
