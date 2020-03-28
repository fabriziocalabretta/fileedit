package org.fc.hdm;

public class NativePointer extends NativeByteArray {
	public NativePointer() {
		super(NativeByteArray.getPointerSize());
	}

	public long getValue() {
		return getPointer(0);
	}

	public void setValue(long l) {
		setPointer(l, 0);
	}
}