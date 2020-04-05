package org.fc.hdm;

/**
 * incapsula 4 bytes S9(8) COMP
 */
public class CobolShort extends ByteArray {
	static final long serialVersionUID = 0;

	public CobolShort() {
		super(SHORT_SIZE);
	}

	public CobolShort(int i) {
		this();
		setValue(i);
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