package org.fc.hdm;

/**
 * incapsula Un campo packed COMP-3
 */
public class CobolPacked extends ByteArray {
	static final long serialVersionUID = 0;

	boolean signed;

	/**
	 * Allocates a size byets packed
	 * 
	 * @param size
	 *            bytes to allocate
	 * @param s
	 *            signed if true
	 */
	public CobolPacked(int size, boolean s) {
		this(0, size, s);
	}

	public CobolPacked(int value, int size, boolean s) {
		super(size);
		signed = s;
		setValue(value);
	}

	public long getValue() {
		return getPacked(0, length());
	}

	public void setValue(long v) throws ArrayIndexOutOfBoundsException {
		setPacked(v, 0, length(), signed);
	}

	public String toString() {
		return Long.toString(getValue());
	}
}