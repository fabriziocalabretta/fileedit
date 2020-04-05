package org.fc.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class TRACE {
	public static PrintStream out = System.out;

	public final static int NONE = 0;
	public final static int LOG = 1;
	public final static int DEBUG = 2;

	static int level = LOG;

	public static int getLevel() {
		return (level);
	}

	public static void setLevel(int i) {
		level = i;
	}

	public static void openDebug(String filename) throws IOException {
		PrintStream o = new PrintStream(new FileOutputStream(filename));
		out = o;
		System.setOut(o);
		System.setErr(o);
	}

	public TRACE() {
	}

	private static String getName() {
		Thread t = Thread.currentThread();
		return ("[" + t.getName() + "]\t\t");
	}

	public static void DEBUG(String s) {
		if (level < DEBUG)
			return;
		out.println(getName() + s);
	}

	public static void LOG(String s) {
		if (level < LOG)
			return;
		out.println(getName() + s);
	}

	public static void LOG(Throwable e) {
		if (level < LOG)
			return;
		LOG(e.toString());
		printStackTrace(e);
	}

	public static void ALERT(String s) {
		String l = "";
		l += getName() + "**************************************\n";
		l += getName() + "* " + s + "\n";
		l += getName() + "**************************************";
		out.println(l);
	}

	public static void ALERT(Throwable e) {
		ALERT(e.toString());
		printStackTrace(e);
	}

	protected static void printStackTrace(Throwable e) {
		e.printStackTrace(out);
	}

}
