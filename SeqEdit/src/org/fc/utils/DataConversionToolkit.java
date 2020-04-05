package org.fc.utils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.StringTokenizer;

public class DataConversionToolkit {
	static byte[] DBCS_TABLE_A2E_SRC = null;

	static byte[] DBCS_TABLE_A2E_DST = null;

	static byte[] DBCS_TABLE_E2A_SRC = null;

	static byte[] DBCS_TABLE_E2A_DST = null;

	public static byte Ebcdic2Ascii(byte b) {
		int s = byte2int(b);
		return ((byte) EBCDIC_ASCII[s]);
	}

	public static byte Ascii2Ebcdic(byte b) {
		int s = byte2int(b);
		return ((byte) ASCII_EBCDIC[s]);
	}

	public static final int byte2int(byte b) {
		int s = (int) b;
		if (s < 0) {
			s = 256 + b;
		}
		return (s);
	}

	public static final byte int2byte(int i) {
		return ((byte) i);
	}

	public static final void overrideAsc2Ebc(byte[] v) {
		DataConversionToolkit.backupDefaultCodepage();
		for (int i = 0; i < DataConversionToolkit.ASCII_EBCDIC.length; i++) {
			DataConversionToolkit.ASCII_EBCDIC[i] = v[i];
		}
	}

	public static final void overrideEbc2Asc(byte[] v) {
		DataConversionToolkit.backupDefaultCodepage();
		for (int i = 0; i < DataConversionToolkit.EBCDIC_ASCII.length; i++) {
			DataConversionToolkit.EBCDIC_ASCII[i] = v[i];
		}
	}

	public final static void importCodepage(InputStream is) throws Exception {
		DataConversionToolkit.backupDefaultCodepage();
		try {
			BufferedReader f = new BufferedReader(new InputStreamReader(is));
			String line = null;
			int lineno = 0;
			int idx = 0;
			byte[] ebcdic = new byte[256];
			byte[] ascii = new byte[256];
			for (int i = 0; i < 256; i++) {
				ebcdic[i] = 0x00;
				ascii[i] = 0x00;
			}
			LINE: while ((line = f.readLine()) != null) {
				lineno++;
				line.trim();
				if (line.startsWith("#")) {
					continue LINE;
				}
				if (line.length() == 0) {
					continue LINE;
				}

				StringTokenizer st = new StringTokenizer(line);
				if (st.countTokens() != 17) {
					throw new Exception("wrong number of elements at line " + lineno);
				}
				st.nextToken();
				for (int i = 0; i < 16; i++) {
					short v = 0x00;
					String e = st.nextToken();
					switch (e.length()) {
					case 1:
						v = (short) e.charAt(0);
						break;
					case 2:
						v = (short) Integer.parseInt(e, 16);
						break;
					default:
						throw new Exception("malformed element " + (i + 1) + " at line " + lineno);
					}
					System.out.println("e=" + e);
					System.out.println("v=" + v);
					System.out.println("idx=" + idx);
					DataConversionToolkit.EBCDIC_ASCII[idx] = v;
					DataConversionToolkit.ASCII_EBCDIC[v] = (short) idx;
					idx++;
				}
			}
		} catch (Exception ex) {
			DataConversionToolkit.restoreDefaultCodepage();
			throw ex;
		}
	}

	public final static void importDBCS(InputStream is) throws Exception {
		DataConversionToolkit.backupDefaultCodepage();
		try {
			int lineno = 0;
			BufferedReader f = new BufferedReader(new InputStreamReader(is));
			String line = null;
			LinkedList Ascii2Ebcdic = new LinkedList();
			LinkedList Ebcdic2Ascii = new LinkedList();

			LINE: while ((line = f.readLine()) != null) {
				lineno++;
				line.trim();
				if (line.startsWith("#")) {
					continue LINE;
				}
				if (line.length() == 0) {
					continue LINE;
				}

				StringTokenizer st = new StringTokenizer(line);
				if (st.countTokens() != 4) {
					throw new Exception("wrong number of elements at line " + lineno);
				}
				String eas = st.nextToken();
				String ead = st.nextToken();
				String aes = st.nextToken();
				String aed = st.nextToken();

				if (!aes.equals("----") && !aed.equals("----")) {
					Ascii2Ebcdic.add(new DbcsItem(aes, aed));
				}
				if (!eas.equals("----") && !ead.equals("----")) {
					Ebcdic2Ascii.add(new DbcsItem(eas, ead));
				}
			}
			is.close();
			Collections.sort(Ascii2Ebcdic);
			Collections.sort(Ebcdic2Ascii);
			DBCS_TABLE_A2E_SRC = DataConversionToolkit.loadDBCSTable(Ascii2Ebcdic, true);
			DBCS_TABLE_A2E_DST = DataConversionToolkit.loadDBCSTable(Ascii2Ebcdic, false);
			DBCS_TABLE_E2A_SRC = DataConversionToolkit.loadDBCSTable(Ebcdic2Ascii, true);
			DBCS_TABLE_E2A_DST = DataConversionToolkit.loadDBCSTable(Ebcdic2Ascii, false);

		} catch (Exception ex) {
			DataConversionToolkit.restoreDefaultCodepage();
			throw ex;
		}
	}

	public final static byte[] loadDBCSTable(LinkedList l, boolean source) {
		byte[] v = new byte[l.size() * 2];
		int i = 0;
		// System.out.println("Table");
		ListIterator li = l.listIterator();
		while (li.hasNext()) {
			DbcsItem di = (DbcsItem) li.next();
			String s = (source ? di.getSrc() : di.getDst());
			String b1 = s.substring(0, 2);
			String b2 = s.substring(2, 4);
			// System.out.println("-->"+s+"["+b1+"] ["+b2+"]");
			v[i] = (byte) Integer.parseInt(b1, 16);
			v[i + 1] = (byte) Integer.parseInt(b2, 16);
			i += 2;
		}
		return v;
	}

	public static byte toUpperCaseEbcdic(byte b) {
		// int s=byte2int(b);
		char a = (char) EBCDIC_ASCII[byte2int(b)];
		if (a >= 'a' && a <= 'z') {
			char c = Character.toUpperCase(a);
			return ((byte) ASCII_EBCDIC[(int) c]);
		}
		return b;
	}

	static short[] EBCDIC_ASCII_DEFAULT = null;

	static short[] ASCII_EBCDIC_DEFAULT = null;

	private final static void backupDefaultCodepage() {
		if (ASCII_EBCDIC_DEFAULT == null) {
			ASCII_EBCDIC_DEFAULT = new short[256];
			System.arraycopy(ASCII_EBCDIC, 0, ASCII_EBCDIC_DEFAULT, 0, 256);
		}
		if (EBCDIC_ASCII_DEFAULT == null) {
			EBCDIC_ASCII_DEFAULT = new short[256];
			System.arraycopy(EBCDIC_ASCII, 0, EBCDIC_ASCII_DEFAULT, 0, 256);
		}
	}

	public final static void restoreDefaultCodepage() {
		if (ASCII_EBCDIC_DEFAULT != null) {
			System.arraycopy(ASCII_EBCDIC_DEFAULT, 0, ASCII_EBCDIC, 0, 256);
		}
		if (EBCDIC_ASCII_DEFAULT != null) {
			System.arraycopy(EBCDIC_ASCII_DEFAULT, 0, EBCDIC_ASCII, 0, 256);
		}
	}

	final static short[] ASCII_EBCDIC = {
	/* 00 */0x00, /*                  */
	/* 01 */0x01, /*                  */
	/* 02 */0x02, /*                  */
	/* 03 */0x03, /*                  */
	/* 04 */0x04, /*                  */
	/* 05 */0x05, /*                  */
	/* 06 */0x06, /*                  */
	/* 07 */0x07, /*                  */
	/* 08 */0x08, /*                  */
	/* 09 */0x09, /*                  */
	/* 0a */0x0a, /*                  */
	/* 0b */0x0b, /*                  */
	/* 0c */0x0c, /*                  */
	/* 0d */0x0d, /*                  */
	/* 0e */0x0e, /*                  */
	/* 0f */0x0f, /*                  */
	/* 10 */0x10, /*                  */
	/* 11 */0x11, /*                  */
	/* 12 */0x12, /*                  */
	/* 13 */0x13, /*                  */
	/* 14 */0x14, /*                  */
	/* 15 */0x15, /*                  */
	/* 16 */0x16, /*                  */
	/* 17 */0x17, /*                  */
	/* 18 */0x18, /*                  */
	/* 19 */0x19, /*                  */
	/* 1a */0x1a, /*                  */
	/* 1b */0x1b, /*                  */
	/* 1c */0x1c, /*                  */
	/* 1d */0x1d, /*                  */
	/* 1e */0x1e, /*                  */
	/* 1f */0x1f, /** ***************** */
	/* 20 */0x40, /* SP */
	/* 21 */0x5a, /* ! */
	/* 22 */0x7f, /* " */
	/* 23 */0x7b, /* # */
	/* 24 */0x5b, /* $ */
	/* 25 */0x6c, /* % */
	/* 26 */0x50, /* & */
	/* 27 */0x7d, /* ' */
	/* 28 */0x4d, /* ( */
	/* 29 */0x5d, /* ) */
	/* 2a */0x5c, /* * */
	/* 2b */0x4e, /* + */
	/* 2c */0x6b, /* , */
	/* 2d */0x60, /* - */
	/* 2e */0x4b, /* . */
	/* 2f */0x61, /* / */
	/* 30 */0xf0, /* 0 */
	/* 31 */0xf1, /* 1 */
	/* 32 */0xf2, /* 2 */
	/* 33 */0xf3, /* 3 */
	/* 34 */0xf4, /* 4 */
	/* 35 */0xf5, /* 5 */
	/* 36 */0xf6, /* 6 */
	/* 37 */0xf7, /* 7 */
	/* 38 */0xf8, /* 8 */
	/* 39 */0xf9, /* 9 */
	/* 3a */0x7a, /* : */
	/* 3b */0x5e, /* ; */
	/* 3c */0x4c, /* < */
	/* 3d */0x7e, /* = */
	/* 3e */0x6e, /* > */
	/* 3f */0x6f, /* ? */
	/* 40 */0x7c, /* @ */
	/* 41 */0xc1, /* A */
	/* 42 */0xc2, /* B */
	/* 43 */0xc3, /* C */
	/* 44 */0xc4, /* D */
	/* 45 */0xc5, /* E */
	/* 46 */0xc6, /* F */
	/* 47 */0xc7, /* G */
	/* 48 */0xc8, /* H */
	/* 49 */0xc9, /* I */
	/* 4a */0xd1, /* J */
	/* 4b */0xd2, /* K */
	/* 4c */0xd3, /* L */
	/* 4d */0xd4, /* M */
	/* 4e */0xd5, /* N */
	/* 4f */0xd6, /* O */
	/* 50 */0xd7, /* P */
	/* 51 */0xd8, /* Q */
	/* 52 */0xd9, /* R */
	/* 53 */0xe2, /* S */
	/* 54 */0xe3, /* T */
	/* 55 */0xe4, /* U */
	/* 56 */0xe5, /* V */
	/* 57 */0xe6, /* W */
	/* 58 */0xe7, /* X */
	/* 59 */0xe8, /* Y */
	/* 5a */0xe9, /* Z */
	/* 5b */0x63, /* [ */
	/* 5c */0xe0, /* \ */
	/* 5d */0x64, /* ] */
	/* 5e */0x6a, /* ^ */
	/* 5f */0x6d, /* _ */
	/* 60 */0x79, /* ` */
	/* 61 */0x81, /* a */
	/* 62 */0x82, /* b */
	/* 63 */0x83, /* c */
	/* 64 */0x84, /* d */
	/* 65 */0x85, /* e */
	/* 66 */0x86, /* f */
	/* 67 */0x87, /* g */
	/* 68 */0x88, /* h */
	/* 69 */0x89, /* i */
	/* 6a */0x91, /* j */
	/* 6b */0x92, /* k */
	/* 6c */0x93, /* l */
	/* 6d */0x94, /* m */
	/* 6e */0x95, /* n */
	/* 6f */0x96, /* o */
	/* 70 */0x97, /* p */
	/* 71 */0x98, /* q */
	/* 72 */0x99, /* r */
	/* 73 */0xa2, /* s */
	/* 74 */0xa3, /* t */
	/* 75 */0xa4, /* u */
	/* 76 */0xa5, /* v */
	/* 77 */0xa6, /* w */
	/* 78 */0xa7, /* x */
	/* 79 */0xa8, /* y */
	/* 7a */0xa9, /* z */
	/* 7b */0xfb, /* { */
	/* 7c */0x4f, /* | */
	/* 7d */0xfd, /* } */
	/* 7e */0xa1, /* ~ */
	/* 7f */0x5f, /*                  */
	/* 80 */0x20, /*                  */
	/* 81 */0x21, /*                  */
	/* 82 */0x22, /*                  */
	/* 83 */0x23, /*                  */
	/* 84 */0x24, /*                  */
	/* 85 */0x25, /*                  */
	/* 86 */0x26, /*                  */
	/* 87 */0x27, /*                  */
	/* 88 */0x28, /*                  */
	/* 89 */0x29, /*                  */
	/* 8a */0x2a, /*                  */
	/* 8b */0x2b, /*                  */
	/* 8c */0x2c, /*                  */
	/* 8d */0x2d, /*                  */
	/* 8e */0x2e, /*                  */
	/* 8f */0x2f, /*                  */
	/* 90 */0x30, /*                  */
	/* 91 */0x31, /*                  */
	/* 92 */0x32, /*                  */
	/* 93 */0x33, /*                  */
	/* 94 */0x34, /*                  */
	/* 95 */0x35, /*                  */
	/* 96 */0x36, /*                  */
	/* 97 */0x37, /*                  */
	/* 98 */0x38, /*                  */
	/* 99 */0x39, /*                  */
	/* 9a */0x3a, /*                  */
	/* 9b */0x3b, /*                  */
	/* 9c */0x3c, /*                  */
	/* 9d */0x3d, /*                  */
	/* 9e */0x3e, /*                  */
	/* 9f */0x3f, /*                  */
	/* a0 */0x4a, /*                  */
	/* a1 */0x41, /*                  */
	/* a2 */0x42, /*                  */
	/* a3 */0x43, /*                  */
	/* a4 */0x44, /*                  */
	/* a5 */0x45, /*                  */
	/* a6 */0x46, /*                  */
	/* a7 */0x47, /*                  */
	/* a8 */0x48, /*                  */
	/* a9 */0x49, /*                  */
	/* aa */0xaa, /*                  */
	/* ab */0xab, /*                  */
	/* ac */0xac, /*                  */
	/* ad */0xad, /*                  */
	/* ae */0xae, /*                  */
	/* af */0xaf, /*                  */
	/* b0 */0xb0, /*                  */
	/* b1 */0xb1, /*                  */
	/* b2 */0xb2, /*                  */
	/* b3 */0xb3, /*                  */
	/* b4 */0xb4, /*                  */
	/* b5 */0xb5, /*                  */
	/* b6 */0xb6, /*                  */
	/* b7 */0xb7, /*                  */
	/* b8 */0xb8, /*                  */
	/* b9 */0xb9, /*                  */
	/* ba */0xba, /*                  */
	/* bb */0xbb, /*                  */
	/* bc */0xbc, /*                  */
	/* bd */0xbd, /*                  */
	/* be */0xbe, /*                  */
	/* bf */0xbf, /*                  */
	/* c0 */0x80, /*                  */
	/* c1 */0x51, /*                  */
	/* c2 */0x52, /*                  */
	/* c3 */0x53, /*                  */
	/* c4 */0x54, /*                  */
	/* c5 */0x55, /*                  */
	/* c6 */0x56, /*                  */
	/* c7 */0x57, /*                  */
	/* c8 */0x58, /*                  */
	/* c9 */0x59, /*                  */
	/* ca */0x62, /*                  */
	/* cb */0x65, /*                  */
	/* cc */0x66, /*                  */
	/* cd */0x67, /*                  */
	/* ce */0x68, /*                  */
	/* cf */0x69, /*                  */
	/* d0 */0x70, /*                  */
	/* d1 */0x71, /*                  */
	/* d2 */0x72, /*                  */
	/* d3 */0x73, /*                  */
	/* d4 */0x74, /*                  */
	/* d5 */0x75, /*                  */
	/* d6 */0x76, /*                  */
	/* d7 */0x77, /*                  */
	/* d8 */0x78, /*                  */
	/* d9 */0xe1, /*                  */
	/* da */0xda, /*                  */
	/* db */0xdb, /*                  */
	/* dc */0xdc, /*                  */
	/* dd */0xdd, /*                  */
	/* de */0xde, /*                  */
	/* df */0xdf, /*                  */
	/* e0 */0x8a, /*                  */
	/* e1 */0x8b, /*                  */
	/* e2 */0x8c, /*                  */
	/* e3 */0x8d, /*                  */
	/* e4 */0x8e, /*                  */
	/* e5 */0x8f, /*                  */
	/* e6 */0x90, /*                  */
	/* e7 */0xfa, /*                  */
	/* e8 */0xc0, /*                  */
	/* e9 */0xfc, /*                  */
	/* ea */0xea, /*                  */
	/* eb */0xeb, /*                  */
	/* ec */0xec, /*                  */
	/* ed */0xed, /*                  */
	/* ee */0xee, /*                  */
	/* ef */0xef, /*                  */
	/* f0 */0x9a, /*                  */
	/* f1 */0x9b, /*                  */
	/* f2 */0x9c, /*                  */
	/* f3 */0x9d, /*                  */
	/* f4 */0x9e, /*                  */
	/* f5 */0x9f, /*                  */
	/* f6 */0xa0, /*                  */
	/* f7 */0xd0, /*                  */
	/* f8 */0xfe, /*                  */
	/* f9 */0xff, /*                  */
	/* fa */0xca, /*                  */
	/* fb */0xcb, /*                  */
	/* fc */0xcc, /*                  */
	/* fd */0xcd, /*                  */
	/* fe */0xce, /*                  */
	/* ff */0xcf, /* FF */
	};

	final static short[] EBCDIC_ASCII = {
	/* 00 */0x00, /*                  */
	/* 01 */0x01, /*                  */
	/* 02 */0x02, /*                  */
	/* 03 */0x03, /*                  */
	/* 04 */0x04, /*                  */
	/* 05 */0x05, /*                  */
	/* 06 */0x06, /*                  */
	/* 07 */0x07, /*                  */
	/* 08 */0x08, /*                  */
	/* 09 */0x09, /*                  */
	/* 0a */0x0a, /*                  */
	/* 0b */0x0b, /*                  */
	/* 0c */0x0c, /*                  */
	/* 0d */0x0d, /*                  */
	/* 0e */0x0e, /*                  */
	/* 0f */0x0f, /*                  */
	/* 10 */0x10, /*                  */
	/* 11 */0x11, /*                  */
	/* 12 */0x12, /*                  */
	/* 13 */0x13, /*                  */
	/* 14 */0x14, /*                  */
	/* 15 */0x15, /*                  */
	/* 16 */0x16, /*                  */
	/* 17 */0x17, /*                  */
	/* 18 */0x18, /*                  */
	/* 19 */0x19, /*                  */
	/* 1a */0x1a, /*                  */
	/* 1b */0x1b, /*                  */
	/* 1c */0x1c, /*                  */
	/* 1d */0x1d, /*                  */
	/* 1e */0x1e, /*                  */
	/* 1f */0x1f, /** ***************** */
	/* 20 */0x80, /*                  */
	/* 21 */0x81, /*                  */
	/* 22 */0x82, /*                  */
	/* 23 */0x83, /*                  */
	/* 24 */0x84, /*                  */
	/* 25 */0x85, /*                  */
	/* 26 */0x86, /*                  */
	/* 27 */0x87, /*                  */
	/* 28 */0x88, /*                  */
	/* 29 */0x89, /*                  */
	/* 2a */0x8a, /*                  */
	/* 2b */0x8b, /*                  */
	/* 2c */0x8c, /*                  */
	/* 2d */0x8d, /*                  */
	/* 2e */0x8e, /*                  */
	/* 2f */0x8f, /*                  */
	/* 30 */0x90, /*                  */
	/* 31 */0x91, /*                  */
	/* 32 */0x92, /*                  */
	/* 33 */0x93, /*                  */
	/* 34 */0x94, /*                  */
	/* 35 */0x95, /*                  */
	/* 36 */0x96, /*                  */
	/* 37 */0x97, /*                  */
	/* 38 */0x98, /*                  */
	/* 39 */0x99, /*                  */
	/* 3a */0x9a, /*                  */
	/* 3b */0x9b, /*                  */
	/* 3c */0x9c, /*                  */
	/* 3d */0x9d, /*                  */
	/* 3e */0x9e, /*                  */
	/* 3f */0x9f, /*                  */
	/* 40 */0x20, /* SP */
	/* 41 */0xa1, /*                  */
	/* 42 */0xa2, /*                  */
	/* 43 */0xa3, /*                  */
	/* 44 */0xa4, /*                  */
	/* 45 */0xa5, /*                  */
	/* 46 */0xa6, /*                  */
	/* 47 */0xa7, /*                  */
	/* 48 */0xa8, /*                  */
	/* 49 */0xa9, /*                  */
	/* 4a */0xa0, /*                  */
	/* 4b */0x2e, /* . */
	/* 4c */0x3c, /* < */
	/* 4d */0x28, /* ( */
	/* 4e */0x2b, /* + */
	/* 4f */0x7c, /* | */
	/* 50 */0x26, /* & */
	/* 51 */0xc1, /*                  */
	/* 52 */0xc2, /*                  */
	/* 53 */0xc3, /*                  */
	/* 54 */0xc4, /*                  */
	/* 55 */0xc5, /*                  */
	/* 56 */0xc6, /*                  */
	/* 57 */0xc7, /*                  */
	/* 58 */0xc8, /*                  */
	/* 59 */0xc9, /*                  */
	/* 5a */0x21, /* ! */
	/* 5b */0x24, /* $ */
	/* 5c */0x2a, /* * */
	/* 5d */0x29, /* ) */
	/* 5e */0x3b, /* ; */
	/* 5f */0x7f, /*                  */
	/* 60 */0x2d, /* - */
	/* 61 */0x2f, /* / */
	/* 62 */0xca, /*                  */
	/* 63 */0x5b, /* [ */
	/* 64 */0x5d, /* ] */
	/* 65 */0xcb, /*                  */
	/* 66 */0xcc, /*                  */
	/* 67 */0xcd, /*                  */
	/* 68 */0xce, /*                  */
	/* 69 */0xcf, /*                  */
	/* 6a */0x5e, /* ^ */
	/* 6b */0x2c, /* , */
	/* 6c */0x25, /* % */
	/* 6d */0x5f, /* _ */
	/* 6e */0x3e, /* > */
	/* 6f */0x3f, /* ? */
	/* 70 */0xd0, /*                  */
	/* 71 */0xd1, /*                  */
	/* 72 */0xd2, /*                  */
	/* 73 */0xd3, /*                  */
	/* 74 */0xd4, /*                  */
	/* 75 */0xd5, /*                  */
	/* 76 */0xd6, /*                  */
	/* 77 */0xd7, /*                  */
	/* 78 */0xd8, /*                  */
	/* 79 */0x60, /* ` */
	/* 7a */0x3a, /* : */
	/* 7b */0x23, /* # */
	/* 7c */0x40, /* @ */
	/* 7d */0x27, /* ' */
	/* 7e */0x3d, /* = */
	/* 7f */0x22, /* " */
	/* 80 */0xc0, /*                  */
	/* 81 */0x61, /* a */
	/* 82 */0x62, /* b */
	/* 83 */0x63, /* c */
	/* 84 */0x64, /* d */
	/* 85 */0x65, /* e */
	/* 86 */0x66, /* f */
	/* 87 */0x67, /* g */
	/* 88 */0x68, /* h */
	/* 89 */0x69, /* i */
	/* 8a */0xe0, /*                  */
	/* 8b */0xe1, /*                  */
	/* 8c */0xe2, /*                  */
	/* 8d */0xe3, /*                  */
	/* 8e */0xe4, /*                  */
	/* 8f */0xe5, /*                  */
	/* 90 */0xe6, /*                  */
	/* 91 */0x6a, /* j */
	/* 92 */0x6b, /* k */
	/* 93 */0x6c, /* l */
	/* 94 */0x6d, /* m */
	/* 95 */0x6e, /* n */
	/* 96 */0x6f, /* o */
	/* 97 */0x70, /* p */
	/* 98 */0x71, /* q */
	/* 99 */0x72, /* r */
	/* 9a */0xf0, /*                  */
	/* 9b */0xf1, /*                  */
	/* 9c */0xf2, /*                  */
	/* 9d */0xf3, /*                  */
	/* 9e */0xf4, /*                  */
	/* 9f */0xf5, /*                  */
	/* a0 */0xf6, /*                  */
	/* a1 */0x7e, /* ~ */
	/* a2 */0x73, /* s */
	/* a3 */0x74, /* t */
	/* a4 */0x75, /* u */
	/* a5 */0x76, /* v */
	/* a6 */0x77, /* w */
	/* a7 */0x78, /* x */
	/* a8 */0x79, /* y */
	/* a9 */0x7a, /* z */
	/* aa */0xaa, /*                  */
	/* ab */0xab, /*                  */
	/* ac */0xac, /*                  */
	/* ad */0xad, /*                  */
	/* ae */0xae, /*                  */
	/* af */0xaf, /*                  */
	/* b0 */0xb0, /*                  */
	/* b1 */0xb1, /*                  */
	/* b2 */0xb2, /*                  */
	/* b3 */0xb3, /*                  */
	/* b4 */0xb4, /*                  */
	/* b5 */0xb5, /*                  */
	/* b6 */0xb6, /*                  */
	/* b7 */0xb7, /*                  */
	/* b8 */0xb8, /*                  */
	/* b9 */0xb9, /*                  */
	/* ba */0xba, /*                  */
	/* bb */0xbb, /*                  */
	/* bc */0xbc, /*                  */
	/* bd */0xbd, /*                  */
	/* be */0xbe, /*                  */
	/* bf */0xbf, /*                  */
	/* c0 */0xe8, /*                  */
	/* c1 */0x41, /* A */
	/* c2 */0x42, /* B */
	/* c3 */0x43, /* C */
	/* c4 */0x44, /* D */
	/* c5 */0x45, /* E */
	/* c6 */0x46, /* F */
	/* c7 */0x47, /* G */
	/* c8 */0x48, /* H */
	/* c9 */0x49, /* I */
	/* ca */0xfa, /*                  */
	/* cb */0xfb, /*                  */
	/* cc */0xfc, /*                  */
	/* cd */0xfd, /*                  */
	/* ce */0xfe, /*                  */
	/* cf */0xff, /*                  */
	/* d0 */0xf7, /*                  */
	/* d1 */0x4a, /* J */
	/* d2 */0x4b, /* K */
	/* d3 */0x4c, /* L */
	/* d4 */0x4d, /* M */
	/* d5 */0x4e, /* N */
	/* d6 */0x4f, /* O */
	/* d7 */0x50, /* P */
	/* d8 */0x51, /* Q */
	/* d9 */0x52, /* R */
	/* da */0xda, /*                  */
	/* db */0xdb, /*                  */
	/* dc */0xdc, /*                  */
	/* dd */0xdd, /*                  */
	/* de */0xde, /*                  */
	/* df */0xdf, /*                  */
	/* e0 */0x5c, /* \ */
	/* e1 */0xd9, /*                  */
	/* e2 */0x53, /* S */
	/* e3 */0x54, /* T */
	/* e4 */0x55, /* U */
	/* e5 */0x56, /* V */
	/* e6 */0x57, /* W */
	/* e7 */0x58, /* X */
	/* e8 */0x59, /* Y */
	/* e9 */0x5a, /* Z */
	/* ea */0xea, /*                  */
	/* eb */0xeb, /*                  */
	/* ec */0xec, /*                  */
	/* ed */0xed, /*                  */
	/* ee */0xee, /*                  */
	/* ef */0xef, /*                  */
	/* f0 */0x30, /* 0 */
	/* f1 */0x31, /* 1 */
	/* f2 */0x32, /* 2 */
	/* f3 */0x33, /* 3 */
	/* f4 */0x34, /* 4 */
	/* f5 */0x35, /* 5 */
	/* f6 */0x36, /* 6 */
	/* f7 */0x37, /* 7 */
	/* f8 */0x38, /* 8 */
	/* f9 */0x39, /* 9 */
	/* fa */0xe7, /*                  */
	/* fb */0x7b, /* { */
	/* fc */0xe9, /*                  */
	/* fd */0x7d, /* } */
	/* fe */0xf8, /*                  */
	/* ff */0xf9, /* FF */
	};

	static class DbcsItem implements Comparable {
		String src = null;
		String dst = null;

		public DbcsItem(String s, String d) {
			src = s;
			dst = d;
		}

		public String getSrc() {
			return src;
		}

		public String getDst() {
			return dst;
		}

		public int compareTo(Object o) {
			String s2 = ((DbcsItem) o).getSrc();
			return src.compareTo(s2);
		}
	}
}
