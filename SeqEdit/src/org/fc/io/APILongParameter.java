package org.fc.io;

public class APILongParameter {
	long value;

	public APILongParameter() {
		value = 0;
	}

	public APILongParameter(long n) {
		setValue(n);
	}

	public void setValue(long n) {
		value = n;
	}

	public long getValue() {
		return value;
	}
}