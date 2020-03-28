package org.fc.io;

public abstract class DataFileInfo {
	public final static int TYPE_KSDS = 0;
	public final static int TYPE_ESDS = 1;
	public final static int TYPE_RSDS = 2;
	public final static int TYPE_IDX_UNIQUE = 3;
	public final static int TYPE_IDX_NON_UNIQUE = 4;
	public final static int TYPE_FLAT = 5;

	static String[] typeArray = { "KSDS", "ESDS", "RRDS", "IDX_UNIQUE", "IDX_NON_UNIQUE", "FLAT" };

	private int type;
	private int minRecordLength;
	private int maxRecordLength;
	
	KeyInfo keyInfo = null;

	public int getType() {
		return type;
	}

	public void setType(int i) {
		type = i;
	}

	public KeyInfo getKeyInfo() {
		return keyInfo;
	}

	public boolean hasIndex() {
		return false;
	}

	public String getTypeDescription() {
		return typeArray[type];
	}

	public int getMinRecordLength() {
		return minRecordLength;
	}

	public void setMinRecordLength(int i) {
		minRecordLength = i;
	}

	public int getMaxRecordLength() {
		return maxRecordLength;
	}

	public void setMaxRecordLength(int i) {
		maxRecordLength = i;
	}
}