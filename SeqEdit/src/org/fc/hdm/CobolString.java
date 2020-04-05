package org.fc.hdm;

public class CobolString extends ByteArray {
	static final long serialVersionUID = 0;

	public CobolString(int s) {
		super(s);
	}

	public CobolString(String s) {
		super(s.getBytes());
	}

	public CobolString(byte[] v) {
		super(v);
	}

	public String getValue() {
		return getString(0, length());
	}

	public void setValue(String s) throws ArrayIndexOutOfBoundsException {
		setString(s, 0, length());
	}

	public void setValue(CobolString cs) throws ArrayIndexOutOfBoundsException {
		setValue(cs.getValue());
	}

	public String toString() {
		return getValue();
	}

	/*
	 * protected void finalize() throws Throwable { System.out.println("FINAL");
	 * super.finalize(); }
	 */
}