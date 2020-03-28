package org.fc.hdm;

/**
 * Interfacci che definisce un vettore di bytes
 * 
 * AbstractByteArray | +-- ByteArray Array di bytes Java +-- NativeByteArray
 * Array di bytes NATIVI (se ne riduce all'osso l'utilizzo)
 * 
 */
public interface GenericByteArray {
	public static final byte DEFAULT_FILLER = (byte) ' ';

	public static final int SHORT_SIZE = 2;
	public static final int INT_SIZE = 4;

	public final static short PIC_X = 0;
	public final static short PIC_9 = 1;
	public final static short COMP = 0;
	public final static short COMP_3 = 1;

	public byte[] getBytes();

	public void setBytes(byte[] b) throws ArrayIndexOutOfBoundsException;

	public void setBytes(byte[] b, int offset, int len) throws ArrayIndexOutOfBoundsException;

	public int length();

	public void set(int b, int index) throws ArrayIndexOutOfBoundsException;

	public int get(int index) throws ArrayIndexOutOfBoundsException;

	/** 2 bytes COMP --> 9(4) COMP */
	public int getUnsignedShortInt(int index);

	public void setUnsignedShortInt(int value, int offset);

	/** 2 bytes COMP --> S9(4) COMP */
	public int getShortInt(int offset);

	public void setShortInt(int value, int offset);

	/** 4 bytes COMP --> S9(8) COMP */
	public int getInt(int offset);

	public void setInt(int value, int offset);

	/** 2 bytes (int) --> 9(4) COMP-5 */
	public int getHostUnsignedShortInt(int index);

	public void setHostUnsignedShortInt(int value, int offset);

	/** 2 bytes (short int) --> S9(4) COMP-5 */
	public int getHostShortInt(int offset);

	public void setHostShortInt(int value, int offset);

	/** 4 bytes (int) --> S9(8) COMP-5 */
	public int getHostInt(int offset);

	public void setHostInt(int value, int offset);

	/** n bytes [S]9(n) COMP-3 */
	public void setPacked(long v, int offset, int len, boolean signed);

	public long getPacked(int offset, int len);

	public void setPacked(double v, int offset, int len, boolean signed, int decimal);

	public double getPacked(int offset, int len, int decimal);

	/** n bytes [S]9(n) USAGE DISPLAY */
	public void setZoned(long v, int offset, int len, boolean signed);

	public long getZoned(int offset, int len);

	public String getString(int offset, int size);

	public void setString(String s, int offset, int len);

	public void setString(String s, int offset, int len, byte filler);

}
