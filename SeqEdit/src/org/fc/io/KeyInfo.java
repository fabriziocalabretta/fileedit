package org.fc.io;

public class KeyInfo {
	IsamKeyPart[] parts;

	public KeyInfo(int p) {
		parts = new IsamKeyPart[p];
	}

	public void setKeyPart(int n, IsamKeyPart k) {
		parts[n] = k;
	}

	public IsamKeyPart getKeyPart(int n) {
		return parts[n];
	}

	public int getPartNumber() {
		return parts.length;
	}

	public String toString() {
		String s = "KeyInfo parts=" + getPartNumber();
		for (int i = 0; i < getPartNumber(); i++) {
			IsamKeyPart kp = getKeyPart(i);
			s += " (" + kp.getOffset() + " " + kp.getLength() + ")";
		}
		return s;
	}
}