package org.fc.io;

public class APIIntParameter {
	int value;

	public APIIntParameter() {
		value = 0;
	}

	public APIIntParameter(int n) {
		setValue(n);
	}

	public void setValue(int n) {
		value = n;
	}

	public int getValue() {
		return value;
	}
}