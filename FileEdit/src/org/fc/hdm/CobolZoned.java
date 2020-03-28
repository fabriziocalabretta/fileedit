package org.fc.hdm;

/**
 * incapsula ZONATO (USAGE DISPLAY)
 */
public class CobolZoned extends ByteArray {
	static final long serialVersionUID = 0;

	boolean signed;

	/**
	 * Allocates a size bytes zoned
	 * 
	 * @param size
	 *            bytes to allocate
	 * @param s
	 *            signed if true
	 */

	public CobolZoned(int size, boolean s) {
		this(0, size, s);
	}

	public CobolZoned(int value, int size, boolean s) {
		super(size);
		signed = s;
		setValue(value);
	}

	public long getValue() {
		return getZoned(0, length());
	}

	public void setValue(long v) throws ArrayIndexOutOfBoundsException {
		setZoned(v, 0, length(), signed);
	}

	public String toString() {
		return Long.toString(getValue());
	}
}