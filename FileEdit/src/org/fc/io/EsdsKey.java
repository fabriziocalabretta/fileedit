package org.fc.io;

/**
 * Incapsula chiave di RBA (ESDS)
 */
public class EsdsKey implements RecordKey {
	long rba;

	public EsdsKey(long l) {
		rba = l;
	}

	public long getRba() {
		return rba;
	}

	public void setRba(int l) {
		rba = l;
	}
}