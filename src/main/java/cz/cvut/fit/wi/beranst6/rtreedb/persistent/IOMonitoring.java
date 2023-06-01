package cz.cvut.fit.wi.beranst6.rtreedb.persistent;

public class IOMonitoring {
	int readCount = 0;
	int writeCount = 0;
	public void hitRead(){
		readCount++;
	}
	public void hitWrite(){
		writeCount++;
	}

	public int getReadCount(){
		return readCount;
	}
	public int getWriteCount(){
		return writeCount;
	}
	public int getTotalIO(){
		return readCount + writeCount;
	}

}
