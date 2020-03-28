package org.fc.utils;

public class Ebcdic2AsciiConverter implements ByteConverter {
	public byte convert(byte b) {
		return DataConversionToolkit.Ebcdic2Ascii(b);
	}
}