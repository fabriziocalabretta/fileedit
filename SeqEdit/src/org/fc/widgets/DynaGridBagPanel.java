package org.fc.widgets;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Pannello multiuso che implemnta un layout di tipo GridBag
 * 
  * @author Fabrizio Calabretta
 */

public class DynaGridBagPanel extends DynaPanel {
	GridBagLayout gridbag = null;
	GridBagConstraints c = null;
	static Insets inset1 = new Insets(10, 0, 0, 0);
	static Insets insetH = new Insets(5, 2, 0, 0);
	static Insets nullInsets = new Insets(0, 0, 0, 0);
	static Insets nullInsetsH = new Insets(5, 10, 0, 0);
	boolean filled = false;
	boolean horizontalLayout = false;

	public DynaGridBagPanel(JFrame p, String s) {
		this(p, s, false);
	}

	public DynaGridBagPanel(JFrame p, String s, boolean h) {
		super(p, s);
		horizontalLayout = h;
		gridbag = new GridBagLayout();
		c = new GridBagConstraints();
		c.gridy = 0;
		filled = false;
		this.setLayout(gridbag);
	}

	protected void layoutElement(JPanel pane, FieldInfo fi) {
		if (horizontalLayout) {
			layoutElementH(pane, fi);
		} else {
			layoutElementV(pane, fi);
		}
	}

	void layoutElementV(JPanel pane, FieldInfo fi) {
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.insets = inset1;
		c.gridx = 0;
		if (fi.hasLabel()) {
			c.weightx = 20;
			// c.fill = 0;
			gridbag.setConstraints(fi.getLabel(), c);
			pane.add(fi.getLabel());
			c.weightx = 80;
			c.gridy++;
			// c.gridwidth=GridBagConstraints.REMAINDER;
			// c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = nullInsets;
			gridbag.setConstraints(fi.getComponent(), c);
			pane.add(fi.getComponent());
		} else {
			gridbag.setConstraints(fi.getComponent(), c);
			pane.add(fi.getComponent());
		}
		c.gridy++;
	}

	void layoutElementH(JPanel pane, FieldInfo fi) {
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
		c.insets = insetH;
		c.gridx = 0;
		if (fi.hasLabel()) {
			c.weightx = 0.1;
			c.gridwidth = 1;
			gridbag.setConstraints(fi.getLabel(), c);
			pane.add(fi.getLabel());
			c.weightx = 0.9;
			c.gridx = 1;
			// c.gridwidth=GridBagConstraints.REMAINDER;
			// c.fill = GridBagConstraints.HORIZONTAL;
			c.insets = nullInsetsH;
			c.gridwidth = 2;
			gridbag.setConstraints(fi.getComponent(), c);
			pane.add(fi.getComponent());
		} else {
			c.gridwidth = 3;
			gridbag.setConstraints(fi.getComponent(), c);
			pane.add(fi.getComponent());
		}
		c.gridy++;
	}

	protected void layoutButtonPane(JPanel p) {
		c.fill = GridBagConstraints.BOTH;
		// c.anchor=GridBagConstraints.SOUTHWEST;
		c.weighty = 100;
		c.gridx = 0;
		c.insets = inset1;
		gridbag.setConstraints(p, c);
		this.add(p);
		filled = true;
	}

	public void pack() {
		super.pack();
		if (!filled) {
			JPanel p = new JPanel();
			c.fill = GridBagConstraints.BOTH;
			c.anchor = GridBagConstraints.SOUTH;
			c.weighty = 100;
			c.gridx = 0;
			gridbag.setConstraints(p, c);
			this.add(p);
			filled = true;
		}
	}

	/*
	 * public void pack() { super.pack(); JPanel p=new JPanel(); c.fill =
	 * GridBagConstraints.BOTH; c.anchor=GridBagConstraints.SOUTH; c.weighty =
	 * 1.0; c.gridx = 0; gridbag.setConstraints(p, c); this.add(p); }
	 */
}
