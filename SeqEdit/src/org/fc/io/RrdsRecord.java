package org.fc.io;

public class RrdsRecord extends Record {
	static final long serialVersionUID = 0;

	public RrdsRecord(int l, RrdsKey r) {
		super(l);
		setRrn(r.getRrn());
	}

	public RrdsRecord(byte[] b, int l, long r) {
		super(b, l);
		setRrn(r);
	}

	public RecordKey getKey() {
		return new RrdsKey(getRrn());
	}

	public long getRrn() {
		return getRecordNumber();
	}

	public void setRrn(long l) {
		setRecordNumber(l);
	}

}