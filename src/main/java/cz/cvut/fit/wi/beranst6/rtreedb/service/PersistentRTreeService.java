package cz.cvut.fit.wi.beranst6.rtreedb.service;

import cz.cvut.fit.wi.beranst6.rtreedb.config.TreeConfig;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.InitDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.RegionDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.ResultRegionDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.kNNDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTree;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeRegion;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.kNNType;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.DatabaseInterface;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.PersistentCachedDatabase;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.PersistentSequenceGenerator;
import cz.cvut.fit.wi.beranst6.rtreedb.persistent.sequence.SequenceGeneratorInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PersistentRTreeService {
	private final Map<Integer,RTree> treeMap = new ConcurrentHashMap<>();
	private final Random r = new Random(System.currentTimeMillis());
	public ResponseEntity<Integer> initTree(InitDTO dto){
		int id = r.nextInt(99999);
		if(dto.id() == -1) {
			while (treeMap.containsKey(id)) {
				id = r.nextInt(99999);
			}
		}else
			id = dto.id();
		TreeConfig config = new TreeConfig(dto.nodeCapacity(),dto.dimension());
		SequenceGeneratorInterface sequence = new PersistentSequenceGenerator(dto.dbName());
		DatabaseInterface db = new PersistentCachedDatabase(dto.cacheSize(), config, sequence, dto.dbName()+id);
		RTree tree = new RTree(config, db);
		treeMap.put(id, tree);
		return ResponseEntity.ok(id);
	}

	public ResponseEntity<Integer> insert(int id, RegionDTO region) {
		if(treeMap.containsKey(id)){
			return ResponseEntity.ok(treeMap.get(id).insert(mapRegionDTOtoBoundingBox(region)));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
	public ResponseEntity<List<Integer>> insert(int id, List<RegionDTO> regions) {
		if(treeMap.containsKey(id)){
			List<Integer> resIDs = new ArrayList<>(regions.size());
			regions.forEach(region->{
				resIDs.add(treeMap.get(id).insert(mapRegionDTOtoBoundingBox(region)));
			});
			return ResponseEntity.ok(resIDs);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	public ResponseEntity<Integer> delete(int id, RegionDTO region) {
		if(treeMap.containsKey(id)){
			treeMap.get(id).delete(new RTreeRegion(mapRegionDTOtoBoundingBox(region)));
			return ResponseEntity.ok(id);
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	public BoundingBox mapRegionDTOtoBoundingBox(RegionDTO region){
		return new BoundingBox(new Coordinate(region.min()),new Coordinate(region.max()));
	}
	public ResponseEntity<List<ResultRegionDTO>> rangeQuery(int id, RegionDTO region) {
		if(treeMap.containsKey(id)){
			return ResponseEntity.ok(mapRTreeNodeToResultDTO(treeMap.get(id).rangeQuery(new RTreeRegion(mapRegionDTOtoBoundingBox(region)))));
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}

	public ResponseEntity<List<ResultRegionDTO>> knn(int id, kNNDTO dto) {
		if(!treeMap.containsKey(id))
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		return ResponseEntity.ok(mapRTreeNodeToResultDTO(treeMap.get(id).kNN(new RTreeRegion(mapRegionDTOtoBoundingBox(dto.region())), dto.k(), kNNType.BETTER_BFS)));
	}

	private List<ResultRegionDTO> mapRTreeNodeToResultDTO(List<RTreeNode> res){
		return res.stream().map(node-> new ResultRegionDTO(new RegionDTO(Arrays.asList(node.getMbr().getBoundingRect().getMin().getCoordinates()),List.of(node.getMbr().getBoundingRect().getMax().getCoordinates())),node.getId())).toList();
	}

	public ResponseEntity<String> clear(int id) {
		if(treeMap.containsKey(id)){
			treeMap.get(id).clearDB();
			return ResponseEntity.ok("OK");
		}
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
	}
}
