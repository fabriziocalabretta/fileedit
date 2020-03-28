package org.fc.io;

import java.io.File;

public class FlatFileInfo extends DataFileInfo {
	File file = null;

	public FlatFileInfo(String p, int reclen) {
		this(new File(p), reclen);
	}

	public FlatFileInfo(File f, int reclen) {
		file = f;
		setType(TYPE_FLAT);
		setMinRecordLength(reclen);
		setMaxRecordLength(reclen);
	}

	public File getFile() {
		return file;
	}

}