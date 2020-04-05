package org.fc.seqedit;

import java.util.LinkedList;

import org.fc.io.Record;

/**
 * Modello per i dati in memoria all'editor
 */

public class EditorModel extends LinkedList {
	int maxRecordLength;
	int maxRecords;
	EditorModelListener listener = null;

	// LinkedList records;
	/*
	 * public EditorModel (DataFile d, int n) { this(d.getMaxRecordLength(), n); }
	 */
	public EditorModel(int rl, int n) {
		maxRecordLength = rl;
		maxRecords = n;
		//  records=new LinkedList();
	}

	public void addListener(EditorModelListener l) {
		listener = l;
	}

	public void addFirst(Record r) {
		super.addFirst(r);
		if (size() > maxRecords) {
			super.removeLast();
		}
		dataChanged();
	}

	public void addLast(Record r) {
		super.addLast(r);
		if (size() > maxRecords) {
			super.removeFirst();
		}
		dataChanged();
	}

	public void insertRecord(Record r, int pos) {
		add(pos - 1, r);
		if (size() > maxRecords) {
			super.removeLast();
		}
		dataChanged();
	}

	public void setByte(int x, int y, byte b) {
		System.out.println("setto a " + x + "," + y);
		Record r = (Record) get(y);
		r.set(b, x);
		dataChanged();
	}

	public byte getByte(int x, int y) {
		Record r = (Record) get(y);
		return (byte) r.get(x);
	}

	public int getMaxRecordLength() {
		return maxRecordLength;
	}

	public void setMaxRecordLength(int i) {
		maxRecordLength = i;
		sizeChanged();
	}

	public int getMaxRecords() {
		return maxRecords;
	}

	public void setMaxRecords(int i) {
		maxRecords = i;
		sizeChanged();
	}

	void dataChanged() {
		// System.out.println("data changed");
		if (listener != null) {
			listener.dataChanged();
		}
	}

	void sizeChanged() {
		if (listener != null) {
			listener.sizeChanged();
		}
	}

	public void renum() {
		if (size() == 0)
			return;
		Record r = (Record) getFirst();
		long b = r.getRecordNumber();
		for (int i = 0; i < size(); i++) {
			r = (Record) get(i);
			r.setRecordNumber(b + i);
		}
		dataChanged();
	}
}
