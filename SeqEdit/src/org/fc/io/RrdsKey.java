package org.fc.io;

/**
 * Incapsula chiave di RBA (ESDS)
 */
public class RrdsKey implements RecordKey {
	long rrn;

	public RrdsKey(long l) {
		rrn = l;
	}

	public long getRrn() {
		return rrn;
	}

	public void setRrn(int l) {
		rrn = l;
	}
}