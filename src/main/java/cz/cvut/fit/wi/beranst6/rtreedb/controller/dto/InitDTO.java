package cz.cvut.fit.wi.beranst6.rtreedb.controller.dto;

public record InitDTO(int id, byte nodeCapacity, byte dimension, int cacheSize, String dbName) {
}
