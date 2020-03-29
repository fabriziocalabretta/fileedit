package org.fc.io;

import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.ProgressMonitor;

public class FlatFile extends DataFile {
	byte[] ioBuffer;
	RandomAccessFile raf;
	String[] modes = { "r", "w", "rw" };
	protected LinkedList index;
	protected int pointer = 0;
	protected int recordLength;

	boolean located = false;

	public FlatFile(File f, int reclen) {
		this(new DataFileInfo(f, DataFileInfo.TYPE_FLAT, reclen));
	}

	public FlatFile(DataFileInfo fi) {
		fileInfo = fi;
	}

	/**
	 * metodi publici
	 */


	public void open(int mode) throws IOException {
		recordLength = fileInfo.getMaxRecordLength();
		ioBuffer = new byte[recordLength];
		index = new LinkedList();
		pointer = 0;
		located = false;
		openMode = mode;
		try {
			reopen();
		} catch (IOException e) {
			openMode = CLOSED;
			throw e;
		}
		System.out.println("file opened mode=" + mode + " ro=" + isReadOnly());
	}

	void reopen() throws IOException {
		System.out.println("opening	: " + getFile());
		raf = new RandomAccessFile(getFile(), modes[openMode]);
		setModified(false);
		System.out.println("length	: " + raf.length());
		long fileLength = raf.length();
		// Runtime rt=Runtime.getRuntime();
		ProgressMonitor pm = getProgressMonitor();
		if (pm != null) {
			progressMonitor.setProgress(0);
			progressMonitor.setMaximum((int) (fileLength / recordLength));
			// progressMonitor.setNote("opening");
		}
		System.out.println("inizio " + new Date());
		for (long rba = 0; rba < fileLength; rba += recordLength) {
			if (pm != null) {
				// progressMonitor.setNote(NumberFormat.getInstance().format(rba)+"
				// bytes");
				progressMonitor.setProgress((int) (rba / recordLength));
				if (progressMonitor.isCanceled()) {
					throw new IOException("operation cancelled by user");
				}
			}
			index.add(new Long(rba));
		}
		System.out.println("fine " + new Date());
	}

	public void close() throws IOException {
		if (isModified()) {
			save();
		}
		raf.close();
		openMode = CLOSED;
		index = null;
	}

	public boolean isEof() {
		return (pointer == index.size());
	}

	public boolean isBof() {
		return (pointer == 0);
	}

	public Record readNext() throws IOException {
		if (isEof())
			throw new EOFException();
		Record r = _read(pointer++);
		return r;
	}

	public Record readPrevious() throws IOException {
		if (isBof())
			throw new EOFException();
		return _read(--pointer);
	}

	public void write(Record r) throws IOException {
		RrdsKey k = (RrdsKey) r.getKey();
		int p = (int) k.getRrn();
		index.add(p, r);
		setModified();
	}

	public void rewrite(Record r) throws IOException {
		RrdsKey k = (RrdsKey) r.getKey();
		int p = (int) k.getRrn();
		index.set(p, r);
		setModified();
	}

	public void locate(int position) throws IOException {
		// si mette a eof o bof
		switch (position) {
		case DataFile.FIRST:
			pointer = 0;
			break;
		case DataFile.LAST:
			pointer = index.size();
			break;
		default:
			throw new IOException("locate: wrong position specified");
		}
	}

	/**
	 * metodi publici astratti
	 */
	public Record read(RecordKey k, int mode) throws IOException {
		// System.out.println("read dir di "+k);
		seek(k, mode);
		return _read(pointer);
	}

	public void seek(RecordKey k, int mode) throws IOException {
		long p = ((RrdsKey) k).getRrn();
		// System.out.println("seek di "+p+" modo="+mode);
		if (mode == DataFile.GREATER) {
			p++;
		}
		pointer = (int) p;

		return;
	}

	public void delete(RecordKey k) throws IOException {
		long p = ((RrdsKey) k).getRrn();
		index.remove((int) p);
		setModified();
	}

	Record _read(int idx) throws IOException {
		Object ptr = index.get(idx);
		FlatRecord r;
		if (ptr instanceof Long) {
			raf.seek(((Long) ptr).longValue());
			raf.read(ioBuffer);
			r = new FlatRecord(ioBuffer, recordLength, idx);
		} else {
			r = (FlatRecord) ptr;
			r.setRrn(idx);
		}
		return r;
	}

	public File getFile() {
		return new File(fileInfo.getFilename());
	}

	public void save() throws IOException {
		setModified(false);
		System.out.println("SAVE*****+");
		String tmp = getFile().toString() + ".jedi";
		System.out.println("genero tmp :" + tmp);

		File f = new File(tmp);
		FileOutputStream out = new FileOutputStream(f);
		ListIterator li = index.listIterator();
		while (li.hasNext()) {
			Object o = li.next();
			byte[] v;
			if (o instanceof Long) {
				raf.seek(((Long) o).longValue());
				raf.read(ioBuffer);
				v = ioBuffer;
			} else {
				FlatRecord r = (FlatRecord) o;
				v = r.getBytes();
			}
			out.write(v, 0, recordLength);
		}
		out.close();
		raf.close();
		System.out.println("f=" + f);
		System.out.println("gf=" + getFile());
		if (getFile().delete()) {
			System.out.println("cancellato " + getFile());
		} else {
			System.out.println("NON cancellato " + getFile());
		}
		System.out.println("f=" + f);
		System.out.println("gf=" + getFile());
		if (f.renameTo(getFile())) {
			System.out.println("RINOMINATO " + f.getAbsolutePath());
		} else {
			System.out.println("NON POSSO RINOMINARE " + f.getAbsolutePath());
			throw new IOException("cannot rename " + f + " to " + getFile());
		}
		reopen();
	}

	public void revertChanges() throws IOException {
		System.out.println("REVERT*****+");
		setModified(false);
		reopen();
	}

	public String toString() {
		File f = getFile();
		if (f == null) {
			return super.toString();
		}
		return f.getAbsolutePath();
	}
}
