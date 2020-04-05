	package org.fc.hdm;

import java.io.PrintStream;
import java.io.Serializable;

/**
 * Vettore di bytes Java
 * 
 */

public class ByteArray implements GenericByteArray, Serializable, Cloneable {
	static final long serialVersionUID = 0;

	public final static short PIC_X = 0;
	public final static short PIC_9 = 1;
	public final static short COMP = 0;
	public final static short COMP_3 = 1;

	byte[] bytes;
	private boolean bigEndian;

	public static boolean isBigEndian() {
		if (System.getProperty("os.arch", "").equals("i386")) {
			return false;
		}
		return true;
	}

	public ByteArray() {
		bigEndian = ByteArray.isBigEndian();
	}

	public ByteArray(int l) {
		this();
		bytes = new byte[l];
	}

	/**
	 * Creates a ByteArray around the given byte array
	 */
	public ByteArray(byte[] b) {
		this();
		bytes = b;
	}

	/**
	 * Generate a copy of specified ByteArray
	 */
	public ByteArray(ByteArray ba) {
		this(ba, 0, ba.length());
	}

	/**
	 * Generate a copy of the subset in specified ByteArray
	 */
	public ByteArray(ByteArray ba, int o, int l) {
		this(ba.getBytes(), o, l);
	}

	/**
	 * Generate a copy of the subset in specified byte array
	 */
	public ByteArray(byte[] v, int o, int l) {
		this(l);
		System.arraycopy(v, o, bytes, 0, l);
	}

	public byte[] getBytes() {
		return bytes;
	}

	public int length() {
		return bytes.length;
	}

	public void setBytes(byte[] b) throws ArrayIndexOutOfBoundsException {
		setBytes(b, 0, b.length);
	}

	public void setBytes(byte[] b, int offset, int len) throws ArrayIndexOutOfBoundsException {
		// checkBounds(offset, len);
		System.arraycopy(b, 0, bytes, offset, len);
	}

	public void set(int b, int index) throws ArrayIndexOutOfBoundsException {
		bytes[index] = (byte) b;
	}

	public int get(int index) throws ArrayIndexOutOfBoundsException {
		return DataFormat.byte2int(bytes[index]);
	}

	/**
	 * BIG ENDIAN / MAINFRAME LIKE
	 */
	public int getUnsignedShortInt(int index) {
		/** restituisce 16 bit NON segnati */
		return ((bytes[index] & 0xff) << 8) | (bytes[index + 1] & 0xff);
	}

	public int getShortInt(int index) {
		/** restituisce 16 bit segnati */
		return (bytes[index] << 8) | (bytes[index + 1] & 0xff);
	}

	public void setUnsignedShortInt(int value, int offset) {
		setShortInt(value, offset);
	}

	public void setShortInt(int value, int index) {
		bytes[index] = (byte) (value >>> 8);
		bytes[index + 1] = (byte) value;
	}

	public int getInt(int index) {
		return (bytes[index] << 24) | ((bytes[index + 1] & 0xff) << 16) | ((bytes[index + 2] & 0xff) << 8)
				| (bytes[index + 3] & 0xff);
	}

	public void setInt(int value, int index) {
		bytes[index] = (byte) (value >>> 24);
		bytes[index + 1] = (byte) (value >>> 16);
		bytes[index + 2] = (byte) (value >>> 8);
		bytes[index + 3] = (byte) value;
	}

	/**
	 * LITTLE ENDIAN
	 */
	public int getUnsignedShortIntLE(int index) {
		/** restituisce 16 bit NON segnati */
		return ((bytes[index + 1] & 0xff) << 8) | (bytes[index] & 0xff);
	}

	public int getShortIntLE(int index) {
		/** restituisce 16 bit segnati */
		return (bytes[index + 1] << 8) | (bytes[index] & 0xff);
	}

	public void setUnsignedShortIntLE(int value, int offset) {
		setShortIntLE(value, offset);
	}

	public void setShortIntLE(int value, int index) {
		bytes[index + 1] = (byte) (value >>> 8);
		bytes[index] = (byte) value;
	}

	public int getIntLE(int index) {
		return (bytes[index + 3] << 24) | ((bytes[index + 2] & 0xff) << 16) | ((bytes[index + 1] & 0xff) << 8)
				| (bytes[index] & 0xff);
	}

	public void setIntLE(int value, int index) {
		bytes[index + 3] = (byte) (value >>> 24);
		bytes[index + 2] = (byte) (value >>> 16);
		bytes[index + 1] = (byte) (value >>> 8);
		bytes[index] = (byte) value;
	}

	public int getHostUnsignedShortInt(int index) {
		if (bigEndian) {
			return getUnsignedShortInt(index);
		} else {
			return getUnsignedShortIntLE(index);
		}
	}

	public void setHostUnsignedShortInt(int value, int offset) {
		if (bigEndian) {
			setUnsignedShortInt(value, offset);
		} else {
			setUnsignedShortIntLE(value, offset);
		}
	}

	/** 2 bytes (short int) --> S9(4) COMP-5 */
	public int getHostShortInt(int offset) {
		if (bigEndian) {
			return getShortInt(offset);
		} else {
			return getShortIntLE(offset);
		}
	}

	public void setHostShortInt(int value, int offset) {
		if (bigEndian) {
			setShortInt(value, offset);
		} else {
			setShortIntLE(value, offset);
		}
	}

	/** 4 bytes (int) --> S9(8) COMP-5 */
	public int getHostInt(int offset) {
		if (bigEndian) {
			return getInt(offset);
		} else {
			return getIntLE(offset);
		}
	}

	public void setHostInt(int value, int offset) {
		if (bigEndian) {
			setInt(value, offset);
		} else {
			setIntLE(value, offset);
		}
	}

	public String getString(int offset, int size) {
		return new String(bytes, offset, size);
	}

	public void setString(String s, int offset) {
		setString(s, offset, s.length(), DEFAULT_FILLER);
	}

	public void setString(String s, int offset, int len) {
		setString(s, offset, len, DEFAULT_FILLER);
	}

	public void setString(String s, int offset, int len, byte filler) {
		int sl = s.length();
		setBytes(s.getBytes(), offset, (sl < len ? sl : len));
		if (sl < len) {
			fill(offset + sl, len - sl, filler);
		}
	}

	public void setPacked(long v, int offset, int len, boolean signed) {
		DataFormat.setPacked(v, bytes, offset, len, signed);
	}

	public long getPacked(int offset, int len) {
		return DataFormat.getPacked(bytes, offset, len);
	}

	public void setPacked(double v, int offset, int len, boolean signed, int decimal) {
		DataFormat.setPacked(v, bytes, offset, len, signed, decimal);
	}

	public double getPacked(int offset, int len, int decimal) {
		return DataFormat.getPacked(bytes, offset, len, decimal);
	}

	public void setZoned(long v, int offset, int len, boolean signed) {
		DataFormat.setZoned(v, bytes, offset, len, signed);
	}

	public long getZoned(int offset, int len) {
		return DataFormat.getZoned(bytes, offset, len);
	}

	public void fill(int offset, int len, byte filler) {
		int d = offset + len;
		for (int i = offset; i < d; i++) {
			bytes[i] = filler;
		}
	}

	public Object clone() throws CloneNotSupportedException {
		return (super.clone());
	}

	public static void main(String[] argv) {
		ByteArray t = new ByteArray(64);
		double d = 17.9;
		t.setPacked(d, 34, 4, true, 2);
		t.dump("-->", t.getBytes(), t.length());

		d = 0;
		t.setPacked(d, 34, 4, true, 2);
		t.dump("-->", t.getBytes(), t.length());

		d = -132.567;
		t.setPacked(d, 34, 4, true, 2);
		t.dump("-->", t.getBytes(), t.length());

		d = 10.1;
		t.setPacked(d, 34, 4, true, 2);

		t.dump("-->", t.getBytes(), t.length());
		System.exit(0);
		ByteArray b = new ByteArray(32);
		b.setPacked(123, 0, 2, true);
		b.setPacked(-123, 2, 2, true);
		b.setPacked(123, 4, 2, false);
		b.setZoned(127, 8, 6, false);
		b.setZoned(127, 14, 6, true);
		b.setZoned(-127, 20, 6, true);
		b.dump("--", b.getBytes(), b.length());
		System.out.println("riletto " + b.getPacked(0, 2));
		System.out.println("riletto " + b.getPacked(2, 2));
		System.out.println("riletto " + b.getPacked(4, 2));
		System.out.println("riletto " + b.getZoned(8, 6));
		System.out.println("riletto " + b.getZoned(14, 6));
		System.out.println("riletto " + b.getZoned(20, 6));
		CobolPacked pk = new CobolPacked(8, true);
		long l = (long) 3269348234000L;
		System.out.println("longmax=" + Long.MAX_VALUE);
		System.out.println("long=" + l);
		pk.setValue(l);
		System.out.println(DataFormat.dump(pk.getBytes(), 8));
		long l2 = pk.getValue();
		System.out.println("get=" + l2);
	}

	public void dump(String msg, byte[] b, int l) {
		ByteArray.dump(System.out, msg, b, l);
	}

	public static void dump(PrintStream out, String msg, byte[] b, int l) {
		out.println(msg + "(" + l + " bytes)");
		int i = 0;
		int n = 0;
		while (i < l) {
			String hex = new String();
			String txt = new String();
			for (n = 0; n < 16 && i < l; n++, i++) {
				hex += Integer.toHexString(DataFormat.byte2int(b[i])) + " ";
				txt += (char) b[i];
			}
			out.println(hex + "  " + txt);
		}
		out.println("--------------------------");
	}

	public void setCString(String s, int offset) {
		setCString(s, offset, s.length());
	}

	public void setCString(String s, int offset, int len) {
		setString(s, offset, len, (byte) 0x00);
	}

	public ByteArray toUpperCase() {
		ByteArray ba = new ByteArray(this);
		for (int i = 0; i < length(); i++) {
			char c = (char) ba.get(i);
			if (Character.isLowerCase(c)) {
				ba.set((int) Character.toUpperCase(c), i);
			}
		}
		return ba;
	}

	public int indexOf(ByteArray ba, int n) {
		return indexOf(ba.getBytes(), n);
	}

	public int indexOf(byte[] v, int n) {
		int sl = length() - v.length;
		SEARCH: for (int i = n; i < sl; i++) {
			for (int p = 0; p < v.length; p++) {
				byte b = (byte) get(i + p);
				if (b != v[p]) {
					continue SEARCH;
				}
			}
			return i;
		}
		return -1;
	}

	public int lastIndexOf(ByteArray ba, int n) {
		return lastIndexOf(ba.getBytes(), n);
	}

	public int lastIndexOf(byte[] v, int n) {
		int sl = v.length - 1;
		SEARCH: for (int i = n; i >= sl; i--) {
			for (int p = 0; p < v.length; p++) {
				byte b = (byte) get(i + p);
				if (b != v[p]) {
					continue SEARCH;
				}
			}
			return i;
		}
		return -1;
	}

	public void setBigEndian(boolean bigEndian) {
		this.bigEndian = bigEndian;
	}
}
