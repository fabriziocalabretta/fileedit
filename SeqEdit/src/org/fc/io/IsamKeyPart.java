package org.fc.io;

public class IsamKeyPart {
	int offset;
	int len;

	public IsamKeyPart(int o, int l) {
		offset = o;
		len = l;
	}

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return len;
	}
}
