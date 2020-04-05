package org.fc.io;

import java.io.IOException;

import javax.swing.ProgressMonitor;

public abstract class DataFile {
	public final static int INPUT_MODE = 0;
	public final static int OUTPUT_MODE = 1;
	public final static int INPUT_OUTPUT_MODE = 2;
	public final static int CLOSED = 9;

	public final static int LOCK_EXCLUSIVE = 0;
	public final static int LOCK_MANUAL = 1;
	public final static int LOCK_AUTOMATIC = 2;

	public final static int EQUAL = 0;
	public final static int GTEQ = 1;
	public final static int GREATER = 2;
	public final static int FIRST = 3;
	public final static int LAST = 4;

	boolean modified;

	ProgressMonitor progressMonitor = null;

	int fileDescriptor;

	int openMode;
	int lockMode;

	DataFileInfo fileInfo;

	public void open(boolean readOnly) throws IOException {
		open(readOnly ? INPUT_MODE : INPUT_OUTPUT_MODE);
	}

	public void open() throws IOException {
		open(INPUT_MODE);
	}

	public int getType() {
		return fileInfo.getType();
	}

	public int getMinRecordLength() {
		return fileInfo.getMinRecordLength();
	}

	public int getMaxRecordLength() {
		return fileInfo.getMaxRecordLength();
	}

	public String getTypeDescription() {
		return fileInfo.getTypeDescription();
	}

	public KeyInfo getKeyInfo() {
		return fileInfo.getKeyInfo();
	}

	public DataFileInfo getFileInfo() {
		return fileInfo;
	}

	public boolean isModified() {
		return modified;
	}

	public void setModified() {
		modified = true;
	}

	public void setModified(boolean b) {
		modified = b;
	}

	public boolean hasIndex() {
		return fileInfo.hasIndex();
	}

	/**
	 * controllo degli stati
	 */
	public int getOpenMode() {
		return openMode;
	}

	public boolean isReadOnly() {
		return (openMode == INPUT_MODE);
	}

	public boolean isOpened() {
		return openMode != CLOSED;
	}

	public abstract void open(int mode) throws IOException;

	public abstract void close() throws IOException;

	public abstract void save() throws IOException;

	public abstract void revertChanges() throws IOException;

	public abstract Record readNext() throws IOException;

	public abstract Record readPrevious() throws IOException;

	public abstract Record read(RecordKey k, int mode) throws IOException;

	/*
	 * public abstract write(Record r) throws IOException; public abstract
	 * rewrite(Record r) throws IOException; public abstract delete(Record r)
	 * throws IOException; public abstract delete() throws IOException;
	 */
	public abstract void locate(int position) throws IOException;

	public abstract void seek(RecordKey k, int mode) throws IOException;

	public abstract void write(Record r) throws IOException;

	public abstract void rewrite(Record r) throws IOException;

	public abstract void delete(RecordKey k) throws IOException;

	public void setProgressMonitor(ProgressMonitor p) {
		progressMonitor = p;
	}

	public ProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

}
