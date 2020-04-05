package org.fc.seqedit;

public class DeleteMultipleCommand implements IEditorCommand {
	long from;
	long to;
	
	public DeleteMultipleCommand(long from, long to) {
		super();
		this.from = from;
		this.to = to;
	}
	
	public long getFrom() {
		return from;
	}
	public long getTo() {
		return to;
	}
	
}
