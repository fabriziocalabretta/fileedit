package org.fc.io;

import org.fc.hdm.ByteArray;
import org.fc.hdm.DataFormat;

public abstract class Record extends ByteArray {
	long recno;

	public Record(byte[] b) {
		super(b);
	}

	public Record(int l) {
		super(l);
	}

	/**
	 * crea un nuovo record dal vettore per i primi l bytes
	 */
	public Record(byte[] b, int l) {
		super(l);
		setBytes(b, 0, l);
	}

	public char[] getChars() {
		byte[] b = getBytes();
		int l = b.length;
		char[] c = new char[l];
		for (int i = 0; i < l; i++) {
			c[i] = (char) b[i];
		}
		return c;
	}

	public String toString() {
		String s = getClass().getName() + " recno=" + getRecordNumber() + "\n";
		s += DataFormat.dump(getBytes(), length());
		return s;
	}

	// public boolean hasKey() { return false; }
	// public boolean inKey(int i) { return false; }

	public abstract RecordKey getKey();

	public void setRecordNumber(long l) {
		recno = l;
	}

	public long getRecordNumber() {
		return recno;
	}

}
