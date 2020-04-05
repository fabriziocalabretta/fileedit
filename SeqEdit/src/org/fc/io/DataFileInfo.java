package org.fc.io;

import java.io.File;

public class DataFileInfo {
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

	private String filename = null;

	KeyInfo keyInfo = null;

	public DataFileInfo(File f, int type, int reclen) {
		this(f.getAbsolutePath(), type, reclen);
	}

	public DataFileInfo(String f, int type, int reclen) {
		setType(TYPE_FLAT);
		setMinRecordLength(reclen);
		setMaxRecordLength(reclen);
		this.filename = f;
	}

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

	public String getFilename() {
		return filename;
	}

	public void setFilename(String s) {
		filename = s;
	}
}