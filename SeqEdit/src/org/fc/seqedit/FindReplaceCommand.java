package org.fc.seqedit;

public class FindReplaceCommand implements IEditorCommand {
	int rangeTo;
	int rangeFrom;
	boolean ignoreCase;
	boolean forward;
	boolean onRange;
	byte[] what=null;
	byte[] with=null;
	
	public FindReplaceCommand(byte[] what, byte[] with, boolean ic, boolean fw, boolean on, int from, int to)
	{
		this.what=what;
		this.with=with;
		ignoreCase=ic;
		forward=fw;
		onRange=on;
		rangeTo=to;
		rangeFrom=from;
	}
	
	public int getRangeTo() {
		return rangeTo;
	}

	public int getRangeFrom() {
		return rangeFrom;
	}

	public boolean isIgnoreCase() {
		return ignoreCase;
	}

	public boolean isForward() {
		return forward;
	}

	public boolean isOnRange() {
		return onRange;
	}

	public byte[] getWhat() {
		return what;
	}

	public byte[] getWith() {
		return with;
	}

	
	
	
}
