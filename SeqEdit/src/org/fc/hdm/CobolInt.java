package org.fc.hdm;

/**
 * incapsula 4 bytes S9(8) COMP
 */
public class CobolInt extends ByteArray {
	static final long serialVersionUID = 0;

	public CobolInt() {
		super(INT_SIZE);
	}

	public CobolInt(int i) {
		this();
		setValue(i);
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