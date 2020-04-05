package org.fc.io;

public class KsdsRecord extends Record {
	static final long serialVersionUID = 0;

	public KsdsRecord(int l) {
		super(l);
	}

	public KsdsRecord(byte[] b, int l) {
		super(b, l);
	}

	public RecordKey getKey() {
		return new KsdsKey(this);
	}
}