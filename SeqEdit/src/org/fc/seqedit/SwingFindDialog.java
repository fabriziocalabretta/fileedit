package org.fc.seqedit;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.LinkedList;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

import org.fc.widgets.DynaConstants;
import org.fc.widgets.DynaGridBagPanel;
import org.fc.widgets.SmartDialog;

public class SwingFindDialog extends JDialog implements ActionListener {
	// TODO da eliminare
	boolean ok;
	JFrame parent;
	ResourceBundle messages;
	DynaGridBagPanel dp;
	LinkedList previousFind;
	LinkedList previousReplace;
	JCheckBox cbHexMode;
	JCheckBox cbIgnoreCase;
	JCheckBox cbOnRange;
	JTextField tRangeFrom;
	JTextField tRangeTo;
	JLabel lRangeFrom;
	JLabel lRangeTo;
	JPanel rangePane;
	int rangeFrom = 0;
	int rangeTo = 0;
	boolean replaceMode;

	public SwingFindDialog(JFrame p, ResourceBundle m, boolean rep) {
		super(p, true);
		replaceMode = rep;
		parent = p;
		messages = m;
		previousFind = new LinkedList();
		previousReplace = new LinkedList();
		previousFind.add(new String());
		previousReplace.add(new String());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		ok = false;
		initWidgets();
		setTitle(messages.getString((replaceMode ? "caption.replace" : "caption.find")));
		setSize(400, 400);
		if (parent != null) {
			Dimension dlgSize = this.getSize();
			Dimension frmSize = parent.getSize();
			Point loc = parent.getLocation();
			this.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		}
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			closeDialog(false);
		}
	}

	void initWidgets() {
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		dp = new DynaGridBagPanel(parent, null);
		dp.setLabelPosition(DynaConstants.LABEL_POSITION_LEFT);
		dp.addField("FINDWHAT", messages.getString("labels.find.what"), DynaConstants.STRING, null, true);
		if (replaceMode) {
			dp.addField("REPLACE", messages.getString("labels.find.replace"), DynaConstants.STRING, null, true);
		}
		dp.addBoolean("HEXMODE", messages.getString("labels.find.hex"), false);
		dp.addBoolean("IGNORECASE", messages.getString("labels.find.ignore.case"), false);
		dp.addBoolean("ONRANGE", messages.getString("labels.find.on.range"), false);

		cbHexMode = (JCheckBox) dp.getComponent("HEXMODE");
		cbHexMode.setActionCommand("HEXMODE");
		cbHexMode.addActionListener(this);
		cbIgnoreCase = (JCheckBox) dp.getComponent("IGNORECASE");
		cbOnRange = (JCheckBox) dp.getComponent("ONRANGE");
		cbOnRange.setActionCommand("ONRANGE");
		cbOnRange.addActionListener(this);

		String[] v = { messages.getString("labels.find.direction.up"), messages.getString("labels.find.direction.down") };
		dp.addRadioButtons("DIRECTION", messages.getString("labels.find.direction"), v, 1);
		// dp.addButton("BTN_OK", messages.getString("buttons.find"));
		// dp.addButton("BTN_CANCEL", messages.getString("buttons.cancel"));
		rangePane = new JPanel(new GridLayout(2, 2));
		rangePane.setBorder(BorderFactory.createTitledBorder("Column range"));
		tRangeFrom = new JTextField();
		tRangeTo = new JTextField();
		lRangeFrom = new JLabel(messages.getString("labels.find.range.from"));
		lRangeTo = new JLabel(messages.getString("labels.find.range.to"));
		rangePane.add(lRangeFrom);
		rangePane.add(tRangeFrom);
		rangePane.add(lRangeTo);
		rangePane.add(tRangeTo);
		dp.addComponent("RANGEPANE", rangePane);

		dp.pack();
		JPanel bt = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton b;
		b = new JButton((replaceMode ? messages.getString("buttons.replace") : messages.getString("buttons.find")));
		b.setActionCommand("buttons.ok");
		b.addActionListener(this);
		bt.add(b);
		b = new JButton(messages.getString("buttons.cancel"));
		b.setActionCommand("buttons.cancel");
		b.addActionListener(this);
		bt.add(b);
		enableOnRangeWidgets();
		contentPane.add(dp, BorderLayout.CENTER);
		contentPane.add(bt, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent evt) {
		if (evt.getActionCommand().equals("buttons.ok")) {
			closeDialog(true);
			return;
		}
		if (evt.getActionCommand().equals("buttons.cancel")) {
			closeDialog(false);
			return;
		}
		if (evt.getActionCommand().equals("HEXMODE")) {
			cbIgnoreCase.setEnabled(!cbHexMode.isSelected());
			return;
		}
		if (evt.getActionCommand().equals("ONRANGE")) {
			enableOnRangeWidgets();
			return;
		}
	}

	void enableOnRangeWidgets() {
		boolean enable = cbOnRange.isSelected();
		tRangeFrom.setEnabled(enable);
		tRangeTo.setEnabled(enable);
		rangePane.setEnabled(enable);
		lRangeFrom.setEnabled(enable);
		lRangeTo.setEnabled(enable);
	}

	public boolean isOk() {
		return ok;
	}

	public byte[] getFindWhat() {
		return getBytes("FINDWHAT");
	}

	public byte[] getReplaceWith() {
		return getBytes("REPLACE");
	}

	public byte[] getBytes(String v) {
		String s = (String) dp.getValue(v);
		byte[] b = null;
		if (isHexMode()) {
			int len = s.length();
			if ((len % 2) != 0) {
				System.out.println("aggiungo 0");
				s = "0" + s;
				len++;
			}
			b = new byte[len / 2];
			int x = 0;
			for (int i = 0; i < len; i += 2) {
				String h = s.substring(i, i + 2);
				b[x++] = (byte) Integer.parseInt(h, 16);
			}
		} else {
			b = s.getBytes();
		}
		return b;
	}

	public boolean isHexMode() {
		Boolean b = (Boolean) dp.getValue("HEXMODE");
		return b.booleanValue();
	}

	public boolean isIgnoreCase() {
		Boolean b = (Boolean) dp.getValue("IGNORECASE");
		return b.booleanValue();
	}

	public boolean isOnRange() {
		Boolean b = (Boolean) dp.getValue("ONRANGE");
		return b.booleanValue();
	}

	public int getRangeFrom() {
		return rangeFrom;
	}

	public int getRangeTo() {
		return rangeTo;
	}

	public int getDirection() {
		return ((Integer) dp.getValue("DIRECTION")).intValue();
	}

	public void closeDialog(boolean b) {
		ok = b;
		if (ok) {
			try {
				if (!dp.verifyData()) {
					return;
				}
				if (isOnRange()) {
					String f = tRangeFrom.getText();
					String t = tRangeTo.getText();
					if (f.length() == 0 || t.length() == 0) {
						SmartDialog.errorBox(parent, messages.getString("msg.find.wrong.range"));
						return;
					}
					try {
						rangeFrom = Integer.parseInt(f);
						rangeTo = Integer.parseInt(t);
					} catch (NumberFormatException ex) {
						SmartDialog.errorBox(parent, messages.getString("msg.find.wrong.range"));
						return;
					}
					byte[] w = getFindWhat();
					rangeTo -= w.length + 1;
					if (rangeTo < rangeFrom) {
						SmartDialog.errorBox(parent, messages.getString("msg.find.unsuff.range"));
						return;
					}
				}

				if (replaceMode) {
					byte[] f = getFindWhat();
					byte[] r = getReplaceWith();
					if (f.length != r.length) {
						SmartDialog.errorBox(parent, messages.getString("msg.replace.different.length"));
						return;
					}
				}
			} catch (NumberFormatException e) {
				SmartDialog.errorBox(parent, messages.getString("msg.find.wrong.hex"));
				return;
			}
		}
		dispose();
	}

}