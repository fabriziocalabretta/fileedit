package org.fc.widgets;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Dialog dinamica. Dialog multi uso che implementa HTDynaPanel
 * 
  * @author Fabrizio Calabretta
 */
public class DynaDialog extends JDialog implements DynaConstants, ActionListener {

	LinkedList buttons = new LinkedList();

	String title = "unnamed";
	JPanel contentPane = null;

	String pressedButton = null;
	boolean ok = false;
	JFrame parent;

	DynaPanel dynaPane = null;

	public DynaDialog() {
	}

	public DynaDialog(JFrame p, boolean modal, String s) {
		this(p, new DynaPanel(p, null), modal, s);
	}

	public DynaDialog(ContainedPane p, boolean modal, String s) {
		this(p.getContainerFrame(), modal, s);
	}

	public DynaDialog(JFrame p, DynaPanel pane, boolean modal, String s) {
		super(p, modal);
		dynaPane = pane;
		parent = p;
		title = s;
	}

	public void setVisible(boolean b) {
		if (b) {
			initWidgets();
			this.setTitle(title);
			pack();
			DynaDialog.centerDialog(parent, this);
		}
		super.setVisible(b);
	}

	public void initWidgets() {
		contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());

		dynaPane.pack();

		JPanel pButtons = new JPanel();
		pButtons.setLayout(new FlowLayout(FlowLayout.RIGHT));

		JButton bCancel = new JButton(DynaMessages.getMessages().getString("buttons.cancel"));
		bCancel.setActionCommand(B_CANCEL);
		bCancel.addActionListener(this);
		JButton bOk = new JButton(DynaMessages.getMessages().getString("buttons.ok"));
		bOk.setActionCommand(B_OK);
		bOk.addActionListener(this);
		initButtons(pButtons);
		pButtons.add(bCancel);
		pButtons.add(bOk);
		contentPane.add(dynaPane, BorderLayout.CENTER);
		contentPane.add(pButtons, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(bOk);
	}

	protected void initButtons(JPanel pane) {
		ListIterator li = buttons.listIterator();
		while (li.hasNext()) {
			pane.add((JComponent) li.next());
		}

	}

	/**
	 * Aggiunge un pulsante alla dialog
	 * 
	 * @param n
	 *            nome
	 * @param l
	 *            label
	 */
	public void addButton(String n, String l) {
		JButton b = new JButton(l);
		b.setActionCommand(n);
		b.addActionListener(this);
		buttons.addLast(b);
	}

	/**
	 * Aggiunge un campo alla dialog. Puo' essere una stringa, numero o
	 * booleano.
	 * 
	 * @param n
	 *            nome
	 * @param l
	 *            label
	 * @param t
	 *            tipo (STRING/INTEGER/BOOLEAN)
	 * @param d
	 *            valore di default
	 * @param notNull
	 *            obbligatorio o meno
	 */
	public void addField(String n, String l, int t, Object d, boolean notNull) {
		dynaPane.addField(n, l, t, d, notNull);
	}

	/**
	 * Aggiunge una label.
	 * 
	 * @param socket
	 *            testo della label
	 */
	public void addLabel(String l) {
		dynaPane.addLabel(l);
	}

	/**
	 * aggiunge slider
	 */
	public void addSlider(String n, String l, int min, int max, int val) {
		dynaPane.addSlider(n, l, min, max, val);
	}

	/**
	 * Aggiunge un combo.
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore delle stringhe
	 * @param def
	 *            indice stringa di default
	 */
	public void addCombo(String n, String l, Object[] items, int def) {
		dynaPane.addCombo(n, l, items, def);
	}

	/**
	 * Aggiunge un combo.
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore delle stringhe
	 * @param def
	 *            indice stringa di default
	 */
	public void addEditableCombo(String n, String l, Object[] items, int def) {
		dynaPane.addEditableCombo(n, l, items, def);
	}

	/**
	 * aggiunge listbox
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore delle stringhe
	 * @param def
	 *            indice stringa di default
	 */
	public void addList(String n, String l, Object[] items, int def) {
		dynaPane.addList(n, l, items, def);
	}

	public void addRadioButtons(String n, String l, Object[] i, int d) {
		dynaPane.addRadioButtons(n, l, i, d);
	}

	public JComponent getComponent(String name) {
		return (dynaPane.getComponent(name));
	}

	public Object getValue(String name) {
		return (dynaPane.getValue(name));
	}

	public String getPressedButton() {
		return pressedButton;
	}

	public void addField(String n, String l, int t, Object d) {
		dynaPane.addField(n, l, t, d);
	}

	public void closeDialog(boolean b) {
		if (b) {
			if (!dynaPane.verifyData())
				return;
		}
		ok = b;
		dispose();
	}

	public boolean isOk() {
		return (ok);
	}

	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		pressedButton = cmd;
		if (cmd.equals(B_OK)) {
			closeDialog(true);
			return;
		}
		if (cmd.equals(B_CANCEL)) {
			closeDialog(false);
			return;
		}
		pressedButton = cmd;
		// closeDialog(true);

	}

	public void setLabelPosition(int p) {
		dynaPane.setLabelPosition(p);
	}

	public static void centerDialog(JFrame f, JDialog d) {
		DynaDialog.centerWindow(f, d);
	}

	public static void centerWindow(JFrame f, Window d) {
		if (f != null) {
			Dimension dlgSize = d.getSize();
			Dimension frmSize = f.getSize();
			Point loc = f.getLocation();
			d.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
		} else {
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();

			int w = d.getSize().width;
			int h = d.getSize().height;
			int x = (dim.width - w) / 2;
			int y = (dim.height - h) / 2;

			d.setLocation(x, y);
		}
	}
}