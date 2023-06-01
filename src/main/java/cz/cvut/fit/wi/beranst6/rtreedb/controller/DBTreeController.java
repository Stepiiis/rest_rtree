package cz.cvut.fit.wi.beranst6.rtreedb.controller;

import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.InitDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.RegionDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.ResultRegionDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.controller.dto.kNNDTO;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.RTreeNode;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.BoundingBox;
import cz.cvut.fit.wi.beranst6.rtreedb.service.PersistentRTreeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/rtreedb")
public class DBTreeController {
	private final PersistentRTreeService persistentRTreeService;

	public DBTreeController(PersistentRTreeService persistentRTreeService) {
		this.persistentRTreeService = persistentRTreeService;
	}

	// init new or existing connection. if DTO id is -1, new Tree is created, otherwise existing tree with given id is used
	@PostMapping("/init")
	public ResponseEntity<Integer> initTree(@RequestBody InitDTO dto){
		return persistentRTreeService.initTree(dto);
	}

	@PostMapping("/{id}/insert")
	public ResponseEntity<Integer> insert(@PathVariable int id, @RequestBody RegionDTO region){
		return persistentRTreeService.insert(id, region);
	}

	@PostMapping("/{id}/delete")
	public ResponseEntity<Integer> delete(@PathVariable int id, @RequestBody RegionDTO region){
		return persistentRTreeService.delete(id, region);
	}

	@PostMapping("/{id}/insert/list")
	public ResponseEntity<List<Integer>> insert(@PathVariable int id, @RequestBody List<RegionDTO> regions) {
		return persistentRTreeService.insert(id, regions);
	}
	@PostMapping("/{id}/query/range")
	public ResponseEntity<List<ResultRegionDTO>> rangeQuery(@PathVariable int id, @RequestBody RegionDTO region){
		return persistentRTreeService.rangeQuery(id, region);
	}

	@PostMapping("/{id}/query/knn")
	public ResponseEntity<List<ResultRegionDTO>> knn(@PathVariable int id, @RequestBody kNNDTO dto){
		return persistentRTreeService.knn(id, dto);
	}

	@DeleteMapping("/{id}/clear")
	public ResponseEntity<String> cleardb(@PathVariable int id){
		return persistentRTreeService.clear(id);
	}
}
