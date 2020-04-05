package org.fc.io;

import java.io.IOException;

/**
 * Issued when defined key is not appliable in the requested operation for the
 * specified file
 */
public class KeyTypeException extends IOException {
	static final long serialVersionUID = 0;

	public KeyTypeException(String s) {
		super(s);
	}
}
