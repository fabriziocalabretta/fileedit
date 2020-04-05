package org.fc.hdm;

import java.io.PrintWriter;
import java.io.StringWriter;

public class DataFormat {
	public static long getPacked(byte[] bytes, int offset, int len) {
		int rc;
		StringBuffer b = new StringBuffer();
		for (int i = 0; i < len; i++) {
			String s = Integer.toHexString(byte2int(bytes[offset + i]));
			if (s.trim().length() < 2) {
				s = "0" + s;
			}
			b.append(s);
		}
		int segno = ((b.charAt(b.length() - 1) == 'd') ? -1 : 1);
		return (Long.parseLong(b.substring(0, b.length() - 1)) * segno);
	}

	public static void setPacked(long v, byte[] bytes, int offset, int len, boolean signed) {
		StringBuffer n = new StringBuffer();
		n.append(Math.abs(v));
		if (signed) {
			n.append(v < 0 ? 'D' : 'C');
		} else {
			n.append('F');
		}
		int slen = len * 2;
		while (n.length() < slen) {
			n.insert(0, '0');
		}
		for (int i = 0; i < len; i++) {
			bytes[offset + i] = (byte) Integer.parseInt(n.substring(i * 2, i * 2 + 2), 16);
		}
	}

	/**
	 * Sets a signed packed
	 */
	public static void setPacked(double v, byte[] bytes, int offset, int len, boolean signed, int decimal) {
		/**
		 * sono costretto a castare in float altrimenti il double mi arrotonda
		 * ad minchiam
		 */
		// long l=(long)((float)v*(float)Math.pow(10, decimal));
		byte[] a = Double.toString(v).getBytes();
		byte[] b = new byte[(len * 2) + 1];
		int decimals = -1;
		int bi = 0;
		for (int i = 0; i < a.length; i++) {
			if (decimals < 0) {
				if (a[i] == '.') {
					decimals = 0;
					continue;
				}
				b[bi++] = a[i];
			} else {
				if (decimals >= decimal)
					break;
				b[bi++] = a[i];
				decimals++;
			}
		}
		while (decimals < decimal) {
			b[bi++] = '0';
			decimals++;
		}
		long l = Long.parseLong(new String(b, 0, bi));
		// System.out.println("b=["+new String(b,0,bi)+"] l="+l);
		DataFormat.setPacked(l, bytes, offset, len, signed);
	}

	public static double getPacked(byte[] bytes, int offset, int len, int decimal) {
		long l = DataFormat.getPacked(bytes, offset, len);
		return (double) (l / Math.pow(10, decimal));
	}

	public static long getZoned(byte[] bytes, int offset, int len) {
		byte[] a = new byte[len];
		System.arraycopy(bytes, offset, a, 0, len);
		int sign = 1;
		if ((a[a.length - 1] & 0x40) != 0) {
			a[a.length - 1] &= 0xBF;
			sign = -1;
		}
		return (Long.parseLong(new String(a)) * sign);
	}

	public static void setZoned(long v, byte[] bytes, int offset, int len, boolean signed) {
		StringBuffer b = new StringBuffer();
		b.append(Math.abs(v));
		while (b.length() < len) {
			b.insert(0, '0');
		}
		byte[] a = b.toString().getBytes();
		if (signed && v < 0) {
			a[a.length - 1] |= 0x40;
		}
		System.arraycopy(a, 0, bytes, offset, len);
	}

	public final static int byte2int(byte b) {
		int s = (int) b;
		if (s < 0) {
			s = 256 + b;
		}
		return s;
	}

	public final static String dump(byte[] b, int l) {
		return DataFormat.dump(b, l, 0);
	}

	public final static String dump(byte[] b, int l, int off) {
		StringWriter sb = new StringWriter();
		PrintWriter ps = new PrintWriter(sb);
		int i = off;
		int n = 0;
		while (i < l) {
			String hex = new String();
			String txt = new String();
			for (n = 0; n < 16 && i < l; n++, i++) {
				String hh = Integer.toHexString(DataFormat.byte2int(b[i])) + " ";
				if (hh.trim().length() < 2) {
					hh = "0" + hh;
				}
				hex += hh;
				if (b[i] >= 32 && b[i] <= 125) {
					txt += (char) b[i];
				} else {
					txt += ".";
				}
			}
			ps.println(hex + "  " + txt);
		}
		return sb.toString();
	}
}
