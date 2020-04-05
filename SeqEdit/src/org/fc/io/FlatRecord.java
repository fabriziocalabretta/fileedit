package org.fc.io;

public class FlatRecord extends RrdsRecord {
	static final long serialVersionUID = 0;

	public FlatRecord(int l, RrdsKey r) {
		super(l, r);
	}

	public FlatRecord(byte[] b, int l, long r) {
		super(b, l, r);
	}
}