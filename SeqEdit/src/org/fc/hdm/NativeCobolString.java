package org.fc.hdm;

public class NativeCobolString extends NativeByteArray {
	public NativeCobolString(int s) {
		super(s);
	}

	public String getValue() {
		return getString(0, length());
	}

	public void setValue(String s) throws ArrayIndexOutOfBoundsException {
		setString(s, 0, length());
	}

	public String toString() {
		return getValue();
	}

	/*
	 * protected void finalize() throws Throwable { System.out.println("FINAL");
	 * super.finalize(); }
	 */
}