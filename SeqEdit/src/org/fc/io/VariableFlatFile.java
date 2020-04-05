package org.fc.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

import javax.swing.ProgressMonitor;

import org.fc.hdm.ByteArray;

public class VariableFlatFile extends FlatFile {
	boolean LEHeader=false;
	
	public VariableFlatFile(File f, int reclen, boolean vlle) {
		super(f, reclen);
		LEHeader=vlle;
	}
	public VariableFlatFile(DataFileInfo fi) {
		super(fi);
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
			progressMonitor.setMaximum((int) fileLength );
			// progressMonitor.setNote("opening");
		}
		System.out.println("inizio " + new Date());
		long rba=0;
		try {
			ByteArray h=new ByteArray(4);
			byte[] r=new byte[32*1024];
			int rc=1;
			while (rc>0) {
				rc=raf.read(h.getBytes());
				if (rc<=0) break;
				index.add(new Long(rba));
			
				int len=(LEHeader?h.getShortIntLE(0):h.getShortInt(0));
				rc=raf.read(r, 0, len-4);
				if (rc<=0) break;
				rba+=(len);
				
				progressMonitor.setProgress((int) (rba));
				if (progressMonitor.isCanceled()) {
					throw new IOException("operation cancelled by user");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("fine " + new Date());
	}

	Record _read(int idx) throws IOException {
		Object ptr = index.get(idx);
		FlatRecord r;
		ByteArray h=new ByteArray(4);
		if (ptr instanceof Long) {
			for (int i=0;i<ioBuffer.length;i++) {
				ioBuffer[i]=0x00;
			}
			
			raf.seek(((Long) ptr).longValue());
			raf.read(ioBuffer,0,4);
			h.setBytes(ioBuffer, 0, 4);
			int len=(LEHeader?h.getShortIntLE(0):h.getShortInt(0));
			raf.read(ioBuffer,4,len);
			
			r = new FlatRecord(ioBuffer, recordLength, idx);
		} else {
			r = (FlatRecord) ptr;
			r.setRrn(idx);
		}
		return r;
	}
}
