package org.fc.widgets;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class SmartDialog {
	public final static void messageBox(ContainedPane p, String msg) {
		messageBox(p.getContainerFrame(), msg);
	}

	public final static void messageBox(JFrame p, String msg) {
		JOptionPane.showMessageDialog(p, msg, DynaMessages.getMessages().getString("caption.info"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	public final static boolean confirmBox(JFrame p, String msg) {
		int rc = JOptionPane.showConfirmDialog(p, msg, DynaMessages.getMessages().getString("caption.confirm"),
				JOptionPane.YES_NO_OPTION);
		return (rc == JOptionPane.YES_OPTION);
	}

	public final static int confirmOrCancelBox(JFrame p, String msg) {
		int rc = JOptionPane.showConfirmDialog(p, msg, DynaMessages.getMessages().getString("caption.confirm"),
				JOptionPane.YES_NO_CANCEL_OPTION);
		return rc;
	}

	public final static Object inputBox(JFrame p, String msg) {
		Object rc = JOptionPane.showInputDialog(p, msg);
		return (rc);
	}

	public final static void errorBox(ContainedPane p, String msg) {
		errorBox(p.getContainerFrame(), msg);
	}

	public final static void errorBox(JFrame p, String msg) {
		JOptionPane.showMessageDialog(p, msg, DynaMessages.getMessages().getString("caption.error"), JOptionPane.ERROR_MESSAGE);
	}

	public final static void errorBox(ContainedPane p, Exception e) {
		errorBox(p.getContainerFrame(), e);
	}

	public final static void errorBox(JFrame p, Exception e) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		String s = sw.toString().replace('\t', ' ');
		// s=s.replace('\r','\n');
		//
		String msg = new String("Unhandled exception caught:");
		msg += s;
		/*
		 * StringTokenizer st=new StringTokenizer(s,"\n"); while
		 * (st.hasMoreTokens()) { msg+="\n"; String x=st.nextToken(); msg+=x; }
		 */
		SmartDialog.errorBox(p, msg);
	}

}
