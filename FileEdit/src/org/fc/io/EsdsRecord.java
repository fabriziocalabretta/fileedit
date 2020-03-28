package org.fc.io;

public class EsdsRecord extends Record {
	static final long serialVersionUID = 0;

	public EsdsRecord(int l) {
		super(l);
	}

	public EsdsRecord(int l, EsdsKey k) {
		super(l);
		setRba(k.getRba());
	}

	public EsdsRecord(byte[] b, int l, long r) {
		super(b, l);
		setRba(r);
	}

	public RecordKey getKey() {
		return new EsdsKey(getRba());
	}

	public long getRba() {
		return getRecordNumber();
	}

	public void setRba(long l) {
		setRecordNumber(l);
	}

}