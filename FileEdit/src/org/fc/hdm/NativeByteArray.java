package org.fc.hdm;

public class NativeByteArray implements GenericByteArray {
	static {
		System.loadLibrary("hdm");
	}
	public static final long NULL = 0;

	long pointer;
	int size;
	boolean releasable;

	public NativeByteArray() {
		pointer = 0;
		releasable = true;
	}

	/**
	 * costruttore per area preesistente
	 * 
	 * @param l
	 *            puntatore ad area nativa
	 * @param s
	 *            dimensione dell'area
	 */
	public NativeByteArray(long l, int s) {
		this();
		pointer = l;
		size = s;
		releasable = false;
		// retrieve();
	}

	/**
	 * costruttore per area nuova
	 * 
	 * @param l
	 *            puntatore ad area nativa
	 * @param s
	 *            dimensione dell'area
	 */
	public NativeByteArray(int s) {
		this();
		pointer = calloc(s);
		size = s;
		// retrieve();
	}

	public int length() {
		return size;
	}

	public boolean isAllocated() {
		return (pointer != 0);
	}

	public long getAddress() {
		return pointer;
	}

	public byte[] getBytes() {
		return getBytes(0, length());
	}

	public byte[] getBytes(int offset, int len) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, len);
		byte[] b = new byte[len];
		retrieve(pointer, b, offset, len);
		return b;
	}

	public void setBytes(byte[] b) throws ArrayIndexOutOfBoundsException {
		setBytes(b, 0, (b.length < length() ? b.length : length()));
	}

	public void setBytes(byte[] b, int offset, int len) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, len);
		release(pointer, b, offset, len);
	}

	public void set(int value, int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, 1);
		setByte(pointer, value, offset);
	}

	public int get(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, 1);
		return getByte(pointer, offset);
	}

	public int getShortInt(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, SHORT_SIZE);
		return get2ByteBinary(pointer, offset);
	}

	public void setShortInt(int value, int offset) {
		set2ByteBinary(pointer, value, offset);
	}

	public int getUnsignedShortInt(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, SHORT_SIZE);
		return get2ByteBinaryUnsigned(pointer, offset);
	}

	public void setUnsignedShortInt(int value, int offset) {
		set2ByteBinaryUnsigned(pointer, value, offset);
	}

	public int getInt(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, INT_SIZE);
		return get4ByteBinary(pointer, offset);
	}

	public void setInt(int value, int offset) {
		set4ByteBinary(pointer, value, offset);
	}

	/**
	 * Gets 2 bytes integer in host dependant byte order
	 */
	public int getHostShortInt(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, SHORT_SIZE);
		return get2ByteHostBinary(pointer, offset);
	}

	/**
	 * Sets 2 bytes integer in host dependant byte order
	 */
	public void setHostShortInt(int value, int offset) {
		set2ByteHostBinary(pointer, value, offset);
	}

	/**
	 * Gets 2 bytes unsigned integer in host dependant byte order
	 */
	public int getHostUnsignedShortInt(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, SHORT_SIZE);
		return get2ByteHostBinaryUnsigned(pointer, offset);
	}

	/**
	 * Sets 2 bytes unsigned integer in host dependant byte order
	 */
	public void setHostUnsignedShortInt(int value, int offset) {
		set2ByteHostBinaryUnsigned(pointer, value, offset);
	}

	/**
	 * Gets 4 bytes integer in host dependant byte order
	 */
	public int getHostInt(int offset) throws ArrayIndexOutOfBoundsException {
		checkBounds(offset, INT_SIZE);
		return get4ByteHostBinary(pointer, offset);
	}

	/**
	 * Sets 2 bytes integer in host dependant byte order
	 */
	public void setHostInt(int value, int offset) {
		set4ByteHostBinary(pointer, value, offset);
	}

	public String getString(int offset, int len) throws ArrayIndexOutOfBoundsException {
		return new String(getBytes(offset, len));
	}

	public void setString(String s, int offset, int len) throws ArrayIndexOutOfBoundsException {
		setString(s, offset, len, DEFAULT_FILLER);
	}

	public void setString(String s, int offset, int len, byte filler) throws ArrayIndexOutOfBoundsException {
		int sl = s.length();
		setBytes(s.getBytes(), offset, (sl < len ? sl : len));
		if (sl < len) {
			fill(offset + sl, len - sl, filler);
		}
	}

	public void setPacked(long v, int offset, int len, boolean signed) {
		byte[] bytes = new byte[len];
		DataFormat.setPacked(v, bytes, 0, len, signed);
		setBytes(bytes, offset, len);
	}

	public long getPacked(int offset, int len) {
		return DataFormat.getPacked(getBytes(offset, len), 0, len);
	}

	public void setPacked(double v, int offset, int len, boolean signed, int decimal) {
		byte[] bytes = new byte[len];
		DataFormat.setPacked(v, bytes, 0, len, signed, decimal);
		setBytes(bytes, offset, len);
	}

	public double getPacked(int offset, int len, int decimal) {
		return DataFormat.getPacked(getBytes(offset, len), 0, len, decimal);
	}

	public void setZoned(long v, int offset, int len, boolean signed) {
		byte[] bytes = new byte[len];
		DataFormat.setZoned(v, bytes, offset, len, signed);
		setBytes(bytes, offset, len);
	}

	public long getZoned(int offset, int len) {
		return DataFormat.getZoned(getBytes(offset, len), offset, len);
	}

	public long getPointer(int offset) {
		return getPointer(pointer, offset);
	}

	public void setPointer(long p, int offset) {
		setPointer(pointer, p, offset);
	}

	/**
	 * verifica i bounds rispetto all'area nativa
	 */
	void checkBounds(int off, int n) throws ArrayIndexOutOfBoundsException, NullPointerException {
		if (!isAllocated()) {
			throw new NullPointerException();
		}
		if (off < 0)
			throw new ArrayIndexOutOfBoundsException();
		if (off + n > length())
			throw new ArrayIndexOutOfBoundsException();
	}

	protected void finalize() throws Throwable {
		System.out.println("finalized");
		free();
		super.finalize();
	}

	public void fill(int offset, int len, byte filler) {
		memset(pointer, offset, len, filler);
	}

	public void free() {
		if (pointer != 0 && releasable) {
			free(pointer);
		}
		pointer = 0;
	}

	/**
	 * metdoi nativi
	 */
	native void retrieve(long ptr, byte[] dest, int start, int len);

	native void release(long ptr, byte[] src, int start, int len);

	native long calloc(int size);

	native void free(long ptr);

	native void memset(long ptr, int start, int len, byte filler);

	native int getByte(long pointer, int offset);

	native void setByte(long pointer, int value, int offset);

	native int get2ByteBinary(long pointer, int offset);

	native void set2ByteBinary(long pointer, int value, int offset);

	native int get4ByteBinary(long pointer, int offset);

	native void set4ByteBinary(long pointer, int value, int offset);

	native int get2ByteBinaryUnsigned(long pointer, int offset);

	native void set2ByteBinaryUnsigned(long pointer, int value, int offset);

	native int get4ByteBinaryUnsigned(long pointer, int offset);

	native void set4ByteBinaryUnsigned(long pointer, int value, int offset);

	/**
	 * big/little endian
	 */
	native int get2ByteHostBinary(long pointer, int offset);

	native void set2ByteHostBinary(long pointer, int value, int offset);

	native int get2ByteHostBinaryUnsigned(long pointer, int offset);

	native void set2ByteHostBinaryUnsigned(long pointer, int value, int offset);

	native int get4ByteHostBinary(long pointer, int offset);

	native void set4ByteHostBinary(long pointer, int value, int offset);

	native long getPointer(long pointer, int offset);

	native void setPointer(long pointer, long value, int offset);

	/**
	 * Puntatore a 32/64 bit
	 */
	public native static int getPointerSize();

	public native static boolean isBigEndian();
	// native String getString(long pointer, int offset, int len);

}
