package org.fc.io;

/**
 * Encapsulate chiave di byte (es. KSDS)
 */
public class KsdsKey implements RecordKey {
	Record record;

	public KsdsKey(Record r) {
		record = r;
	}

	/**
	 * ritorna la COPIA della chiave
	 */
	public byte[] getBytes() {
		byte[] b = new byte[record.length()];
		getBytes(b);
		return b;
	}

	public int getBytes(byte[] b) {
		System.arraycopy(record.getBytes(), 0, b, 0, record.length());
		return record.length();
	}

}
