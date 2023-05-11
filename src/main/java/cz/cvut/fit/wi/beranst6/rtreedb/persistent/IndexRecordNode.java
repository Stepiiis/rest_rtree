package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

import cz.cvut.fit.wi.beranst6.rtreedb.config.Constants;
import cz.cvut.fit.wi.beranst6.rtreedb.modules.utils.Coordinate;

public class IndexRecordNode {
    private byte statusByte;
    private int id;
    private byte headerSize;
    private Coordinate[] mbr;

    IndexRecordNode(byte statusByte, int id, byte headerSize, Coordinate... mbr) {
        this.statusByte = statusByte;
        this.id = id;
        this.headerSize = headerSize;
        this.mbr = mbr;
    }

    public byte getStatusByte() {
        return statusByte;
    }

    public void setStatusByte(byte statusByte) {
        this.statusByte = statusByte;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public byte getHeaderSize() {
        return headerSize;
    }

    public void setHeaderSize(byte headerSize) {
        this.headerSize = headerSize;
    }

    public Coordinate[] getMbr() {
        return mbr;
    }

    public void setMbr(Coordinate[] mbr) {
        this.mbr = mbr;
    }
}
