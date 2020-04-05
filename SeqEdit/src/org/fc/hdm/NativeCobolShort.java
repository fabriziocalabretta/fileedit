package org.fc.hdm;

/**
 * incapsula 4 bytes S9(8) COMP
 */
public class NativeCobolShort extends NativeByteArray {
	public NativeCobolShort() {
		super(SHORT_SIZE);
	}

	public int getValue() {
		return getShortInt(0);
	}

	public void setValue(int i) throws ArrayIndexOutOfBoundsException {
		setShortInt(i, 0);
	}

	public String toString() {
		return Integer.toString(getValue());
	}
}