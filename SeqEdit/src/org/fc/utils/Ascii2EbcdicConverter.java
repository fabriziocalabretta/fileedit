package org.fc.utils;

public class Ascii2EbcdicConverter implements ByteConverter {
	public byte convert(byte b) {
		return DataConversionToolkit.Ascii2Ebcdic(b);
	}
}