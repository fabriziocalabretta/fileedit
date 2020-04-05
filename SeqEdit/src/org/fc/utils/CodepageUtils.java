package org.fc.utils;

/**
 * @author Fabrizio Calabretta
 * @version 1.0
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class CodepageUtils {
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].equals("-convert")) {
				System.out.println("generating codepage");
				CodepageUtils.convert(args[1], args[2]);
			}
			if (args[0].equals("-dbcs")) {
				System.out.println("generating DBCS tables");
				CodepageUtils.convertDBCS(args[1], args[2]);
				System.exit(0);
			}
		}
		System.out.println("");
		System.out.println("ASCII -> EBCDIC table");
		System.out.println("=====================");
		CodepageUtils.dump(System.out, DataConversionToolkit.ASCII_EBCDIC, DataConversionToolkit.ASCII_EBCDIC.length);
		System.out.println("");
		System.out.println("EBCDIC -> ASCII table");
		System.out.println("=====================");
		CodepageUtils.dump(System.out, DataConversionToolkit.EBCDIC_ASCII, DataConversionToolkit.EBCDIC_ASCII.length);
		System.out.println("");
		System.out.println("ASCII <-> EBCDIC");
		System.out.println("================");
		for (int i = 0; i < 256; i++) {
			String s1 = Integer.toHexString(i);
			if (s1.length() < 2) {
				s1 = "0" + s1;
			}
			String s2 = Integer.toHexString(DataConversionToolkit.ASCII_EBCDIC[i]);
			if (s2.length() < 2) {
				s2 = "0" + s2;
			}
			String s = s1 + " " + s2;
			System.out.println(s.toUpperCase());
		}

	}

	public static void convert(String filename, String output) {
		try {
			File f = new File(filename);
			FileInputStream fis = new FileInputStream(f);
			DataConversionToolkit.importCodepage(fis);
			fis.close();
			FileOutputStream out = new FileOutputStream(output);
			for (int i = 0; i < 256; i++) {
				out.write(DataConversionToolkit.ASCII_EBCDIC[i]);
			}
			for (int i = 0; i < 256; i++) {
				out.write(DataConversionToolkit.EBCDIC_ASCII[i]);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void convertDBCS(String filename, String output) {
		try {
			File f = new File(filename);
			FileInputStream fis = new FileInputStream(f);
			DataConversionToolkit.importDBCS(fis);
			fis.close();
			FileOutputStream out = new FileOutputStream(output);
			for (int i = 0; i < DataConversionToolkit.DBCS_TABLE_A2E_SRC.length; i++) {
				out.write(DataConversionToolkit.DBCS_TABLE_A2E_SRC[i]);
			}
			for (int i = 0; i < DataConversionToolkit.DBCS_TABLE_A2E_DST.length; i++) {
				out.write(DataConversionToolkit.DBCS_TABLE_A2E_DST[i]);
			}
			out.write(0);
			out.write(0);
			for (int i = 0; i < DataConversionToolkit.DBCS_TABLE_E2A_SRC.length; i++) {
				out.write(DataConversionToolkit.DBCS_TABLE_E2A_SRC[i]);
			}
			for (int i = 0; i < DataConversionToolkit.DBCS_TABLE_E2A_DST.length; i++) {
				out.write(DataConversionToolkit.DBCS_TABLE_E2A_DST[i]);
			}
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void dump(PrintStream out, short[] b, int l) {
		int i = 0;
		int n = 0;
		int o = 0;
		while (i < l) {
			String hex = new String();
			String txt = new String();
			for (n = 0; n < 16 && i < l; n++, i++) {
				if (Character.isLetterOrDigit((char) b[i])) {
					hex += Character.toString((char) b[i]) + "  ";
				} else {
					hex += (b[i] <= 0x0f ? "0" : "") + Integer.toHexString(b[i]) + " ";
				}
			}
			String off = Integer.toHexString(o) + ": ";
			if (o == 0) {
				off = "00: ";
			}
			o += 16;

			out.println(off + " " + hex + "  " + txt);
			out.flush();
		}
	}

}
