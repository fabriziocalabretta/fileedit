package org.fc.hdm;

/**
 * incapsula 4 bytes S9(8) COMP
 */
public class NativeCobolInt extends NativeByteArray {
	public NativeCobolInt() {
		super(INT_SIZE);
	}

	public int getValue() {
		return getInt(0);
	}

	public void setValue(int i) throws ArrayIndexOutOfBoundsException {
		setInt(i, 0);
	}

	public String toString() {
		return Integer.toString(getValue());
	}
}