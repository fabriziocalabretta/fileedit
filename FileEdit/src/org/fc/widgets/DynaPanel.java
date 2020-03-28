package org.fc.widgets;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.fc.utils.TRACE;

/**
 * Pannello dinamico multi uso. Consente la rapida costruzione di maschere
 * interattive. L'utilizzo del pannello segue un flusso standard. In fase di
 * creazione
 
 * @author Fabrizio CALABRETTA
 */
public class DynaPanel extends JPanel implements DynaConstants, ActionListener {

	public final static int BORDER_NONE = 0;
	public final static int BORDER_COMPOUND = 1;
	public final static int BORDER_TITLED = 2;

	LinkedList fieldInfo = new LinkedList();
	LinkedList buttons = new LinkedList();
	String title = null;
	JFrame parent;
	String pressedButton = null;
	boolean notifyOnChange = false;

	int buttonLayout = FlowLayout.LEFT;

	int panelBorder = BORDER_COMPOUND;
	int labelPosition = LABEL_POSITION_UP;

	boolean editable = true;

	/**
	 * Crea HTDynaPanel
	 * 
	 * @param p
	 *            JFrame di appartenenza
	 * @param s
	 *            Titolo del pannello
	 */
	public DynaPanel(JFrame p, String s) {
		// super (p, modal);
		parent = p;
		title = s;
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
	}

	/**
	 * Prepara il pannello per la visualizzazione. Deve essere NECESSARIAMENTE
	 * chiamata quando si e' terminato di inserire gli elementi sul pannello
	 */
	public void pack() {
		initWidgets();
	}

	private void initWidgets() {
		// this.setLayout(new BorderLayout());
		// JPanel dataPane=new JPanel();
		initDynaFields(this, fieldInfo);
		Border b = null;
		switch (panelBorder) {
		case BORDER_NONE:
			break;

		case BORDER_TITLED:
			b = BorderFactory.createTitledBorder((title != null ? title : "<untitled>"));
			break;

		case BORDER_COMPOUND:
		default:
			b = BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2), BorderFactory.createEtchedBorder());
			break;
		}
		this.setBorder(b);
		if (buttons.size() > 0) {
			JPanel pButtons = new JPanel();
			pButtons.setLayout(new FlowLayout(buttonLayout));
			initButtons(pButtons);
			layoutButtonPane(pButtons);
		}
	}

	/**
	 * Inizializza i pulsanti
	 */
	protected void initButtons(JPanel pane) {
		ListIterator li = buttons.listIterator();
		while (li.hasNext()) {
			pane.add((JComponent) li.next());
		}

	}

	/**
	 * Inizializza i campi dinamici
	 */
	protected void initDynaFields(JPanel pane, LinkedList fieldList) {
		if (fieldList.size() == 0) {
			// pane.add(new JLabel("No fields initialized"));
			TRACE.LOG("NO FIELDS INIZIALIZED");
		}
		ListIterator li = fieldList.listIterator();
		while (li.hasNext()) {
			FieldInfo fi = (FieldInfo) li.next();
			layoutElement(pane, fi);
		}
	}

	protected void layoutElement(JPanel pane, FieldInfo fi) {
		JPanel box = new JPanel();
		box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		if (fi.hasLabel()) {
			switch (labelPosition) {
			case LABEL_POSITION_LEFT:
				box.setLayout(new FlowLayout(FlowLayout.LEFT));
				break;
			case LABEL_POSITION_UP:
			default:
				box.setLayout(new GridLayout(2, 1));
				break;
			}
			box.add(fi.getLabel());
			box.add(fi.getComponent());
		} else {
			box.setLayout(new FlowLayout(FlowLayout.LEFT));
			box.add(fi.getComponent());
		}
		// fi.getComponent().setEnabled(isEditable());
		pane.add(box);
	}

	protected void layoutButtonPane(JPanel pane) {
		this.add(pane);
	}

	/**
	 * Imposta il tipo di bordo. Possibili bordi:
	 * <ul>
	 * <li>BORDER_NODE
	 * <li>BORDER_COMPOUND
	 * <li>BORDER_NONE
	 * </ul>
	 * 
	 * @param b
	 *            tipo di bordo
	 */
	public void setPanelBorder(int b) {
		panelBorder = b;
	}

	/** Restituisce il tipo di bordo utilizzato */
	public int getPanelBorder() {
		return panelBorder;
	}

	/**
	 * Imposta la posizione della labels. Possibili posizioni:
	 * <ul>
	 * <li>HTDynaConstants.LABEL_POSITION_UP
	 * <li>HTDynaConstants.LABEL_POSITION_LEFT
	 * </ul>
	 * 
	 * @param b
	 *            tipo di bordo
	 */
	public void setLabelPosition(int i) {
		labelPosition = i;
	}

	/** Restituisce il posizionamento delle label utilizzato */
	public int getLabelPosition() {
		return labelPosition;
	}

	public void setButtonLayout(int b) {
		buttonLayout = b;
	}

	public int getButtonLayout() {
		return buttonLayout;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean b) {
		editable = b;
	}

	/**
	 * Aggiunge un pulsante alla dialog con listener uguale a se stesso
	 * 
	 * @param n
	 *            action command
	 * @param l
	 *            label
	 */
	public JButton addButton(String n, String l) {
		return (addButton(n, l, this));
	}

	/**
	 * Aggiunge un pulsante alla dialog
	 * 
	 * @param n
	 *            action command
	 * @param l
	 *            label
	 * @param al
	 *            action listener
	 */
	public JButton addButton(String n, String l, ActionListener al) {
		JButton b = new JButton(l);
		b.setActionCommand(n);
		b.addActionListener(al);
		buttons.addLast(b);
		return b;
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
		FieldInfo fi = new FieldInfo(n, l, t, d);
		fi.setNotNull(notNull);
		fieldInfo.add(fi);
	}

	/**
	 * Aggiunge un campo non obligattorio alla dialog. Puo' essere una stringa,
	 * numero o booleano.
	 * 
	 * @param n
	 *            nome
	 * @param l
	 *            label
	 * @param t
	 *            tipo (STRING/INTEGER/BOOLEAN)
	 * @param d
	 *            valore di default
	 */
	public void addField(String n, String l, int t, Object d) {
		addField(n, l, t, d, false);
	}

	/**
	 * Aggiunge un componente Swing! al pannello.
	 * 
	 * @param n
	 *            nome
	 * @param c
	 *            componente
	 */
	public void addComponent(String n, JComponent c) {
		FieldInfo fi = new FieldInfo(n, c);
		fieldInfo.add(fi);
	}

	/**
	 * Aggiunge una label.
	 * 
	 * @param socket
	 *            testo della label
	 */
	public void addLabel(String l) {
		addField("LBL" + l.hashCode(), l, MESSAGE, null, false);
	}

	/**
	 * Aggiunge un booleano (radio button) alla dialog.
	 * 
	 * @param n
	 *            nome
	 * @param l
	 *            label
	 * @param d
	 *            valore di default
	 */
	public void addBoolean(String n, String l, boolean d) {
		addField(n, l, BOOLEAN, new Boolean(d), false);
	}

	/**
	 * Aggiunge uno slider.
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param min
	 *            valore minimo
	 * @param max
	 *            valore massimo
	 * @param val
	 *            valore iniziale
	 */
	public void addSlider(String n, String l, int min, int max, int val) {
		FieldInfo fi = new FieldInfo(n, l, SLIDER, new Integer(val));
		fi.setMin(min);
		fi.setMax(max);
		fi.setNotNull(false);
		fieldInfo.add(fi);
	}

	/**
	 * Aggiunge un combo.
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore di oggetti che inizzializza il combo
	 * @param def
	 *            indice dell'elemento di default
	 */
	public void addCombo(String n, String l, Object[] items, int def) {
		FieldInfo fi = new FieldInfo(n, l, COMBO, null);
		fi.setItems(items, def);
		fi.setNotNull(false);
		fieldInfo.add(fi);
	}

	/**
	 * Aggiunge un set di radio buttons.
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore di oggetti che inizzializza il combo
	 * @param def
	 *            indice dell'elemento di default
	 */
	public void addRadioButtons(String n, String l, Object[] items, int def) {
		FieldInfo fi = new FieldInfo(n, l, RADIO, null);
		fi.setItems(items, def);
		fi.setNotNull(false);
		fieldInfo.add(fi);
	}

	/**
	 * Aggiunge un combo.
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore di oggetti che inizzializza il combo
	 * @param def
	 *            indice dell'elemento di default
	 */
	public void addEditableCombo(String n, String l, Object[] items, int def) {
		FieldInfo fi = new FieldInfo(n, l, EDITCOMBO, null);
		fi.setItems(items, def);
		fi.setNotNull(false);
		fieldInfo.add(fi);
	}

	/**
	 * aggiunge listbox
	 * 
	 * @param n
	 *            nome del campo
	 * @param l
	 *            label
	 * @param items
	 *            vettore di oggetti che inizzializza la listbox
	 * @param def
	 *            indice dell'elemento di default
	 */
	public void addList(String n, String l, Object[] items, int def) {
		FieldInfo fi = new FieldInfo(n, l, LIST, null);
		fi.setItems(items, def);
		fi.setNotNull(false);
		fieldInfo.add(fi);
	}

	protected FieldInfo getFieldInfo(String name) {
		FieldInfo obj = null;
		ListIterator li = fieldInfo.listIterator();
		while (li.hasNext()) {
			FieldInfo fi = (FieldInfo) li.next();
			if (fi.getName().equals(name)) {
				obj = fi;
				break;
			}
		}
		return (obj);
	}

	/**
	 * Restituisce il componente specificato
	 * 
	 * @param name
	 *            nome del campo
	 */
	public JComponent getComponent(String name) {
		return (getFieldInfo(name).getComponent());
	}

	/**
	 * Restituisce il valore del campo specificato. Il tipo di oggetto
	 * restiruito varia in funzione della tipologia di campo:
	 * 
	 * @param name
	 *            nome del campo
	 */
	public Object getValue(String name) {
		return (getFieldInfo(name).getValue());
	}

	/**
	 * Restituisce il valore del campo specificato. Il tipo di oggetto
	 * restiruito varia in funzione della tipologia di campo:
	 * 
	 * @param name
	 *            nome del campo
	 * @param v
	 *            valore
	 */
	public void setValue(String name, Object v) {
		getFieldInfo(name).setValue(v);
	}

	/**
	 * Restituisce il nome del campo premuto
	 */
	public String getPressedButton() {
		return pressedButton;
	}

	/**
	 * Effettua le operazioni di verifica relative ai campi.
	 */
	public boolean verifyData() {
		ListIterator li = fieldInfo.listIterator();
		while (li.hasNext()) {
			FieldInfo fi = (FieldInfo) li.next();
			if (!fi.isValid()) {
				JOptionPane.showMessageDialog(parent, DynaMessages.getMessages().getString("error.field.empty") + " '"
						+ fi.getLabel().getText() + "'", DynaMessages.getMessages().getString("caption.error"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
		}
		return true;
	}

	// public boolean isOk() { return(ok); }

	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		pressedButton = cmd;
		/*
		 * if (cmd.equals(B_OK)) { closeDialog(true); return; } if
		 * (cmd.equals(B_CANCEL)) { closeDialog(false); return; }
		 */
		pressedButton = cmd;
		// closeDialog(true);
	}

	class FieldInfo {
		String name = null;
		JLabel label = null;
		int type;
		JComponent widget = null;
		boolean notNull = false;
		ButtonGroup buttonGroup = null;

		public FieldInfo(String n, JComponent c) {
			type = COMPONENT;
			name = n;
			widget = c;
			TRACE.LOG("Aggiungo " + n + " " + c);
			label = null;
		}

		public FieldInfo(String n, String l, int t, Object o) {
			name = new String(n);
			label = new JLabel(l);
			type = t;
			switch (type) {
			case MESSAGE:
				widget = label;
				break;

			case BOOLEAN:
				widget = new JCheckBox(l);
				if (o != null) {
					((JCheckBox) widget).setSelected(((Boolean) o).booleanValue());
				}
				break;

			case INTEGER:
			case STRING:

				widget = new JTextField();
				if (o != null) {
					((JTextField) widget).setText(o.toString());
				}
				((JTextField) widget).setColumns(15);
				break;

			case PASSWORD:
				widget = new JPasswordField();
				if (o != null) {
					((JPasswordField) widget).setText(o.toString());
				}
				((JPasswordField) widget).setColumns(15);
				type = STRING;
				break;

			case SLIDER:
				JSlider w = new JSlider(JSlider.HORIZONTAL);// ,0,100,Integer);
				w.setMajorTickSpacing(5);
				w.setMinorTickSpacing(1);
				w.setPaintTicks(true);
				w.setPaintLabels(true);
				w.setValue(((Integer) o).intValue());
				widget = w;
				break;

			case COMBO:
				widget = new JComboBox();
				break;

			case EDITCOMBO:
				widget = new JComboBox();
				((JComboBox) widget).setEditable(true);
				break;

			case RADIO:
				widget = new JPanel();
				((JPanel) widget).setBorder(BorderFactory.createTitledBorder(l));
				((JPanel) widget).setLayout(new BoxLayout(widget, BoxLayout.Y_AXIS));
				break;

			case LIST:
				widget = new JList();
				widget.setBorder(BorderFactory.createLoweredBevelBorder());
				break;

			default:
				widget = new JLabel("Unknow type detected");
				break;
			}
			widget.setEnabled(isEditable());
		}

		public void setNotNull(boolean b) {
			notNull = b;
		}

		public boolean isNotNull() {
			return (notNull);
		}

		public boolean hasLabel() {
			if (type == MESSAGE || type == BOOLEAN || type == COMPONENT || type == RADIO)
				return (false);
			return (true);
		}

		public JLabel getLabel() {
			return (label);
		}

		public JComponent getComponent() {
			return (widget);
		}

		public String getName() {
			return (name);
		}

		public void setMin(int m) {
			if (type == SLIDER) {
				((JSlider) widget).setMinimum(m);
			}

		}

		public void setMax(int m) {
			if (type == SLIDER) {
				((JSlider) widget).setMaximum(m);
			}

		}

		public void setItems(Object[] items, int def) {
			switch (type) {
			case COMBO:
			case EDITCOMBO:
				JComboBox cb = (JComboBox) widget;
				for (int i = 0; i < items.length; i++) {
					cb.addItem(items[i]);
				}
				cb.setSelectedIndex(def);
				break;

			case RADIO:
				buttonGroup = new ButtonGroup();
				for (int i = 0; i < items.length; i++) {
					JRadioButton rb = new JRadioButton(items[i].toString());
					rb.setSelected(i == def);
					buttonGroup.add(rb);
					rb.setEnabled(isEditable());
					((JPanel) widget).add(rb);
				}
				break;

			default:
				JList li = (JList) widget;
				li.setListData(items);
				li.setSelectedIndex(def);
				break;
			}
		}

		public boolean isValid() {
			if (type == STRING || type == INTEGER) {
				String s = ((JTextField) widget).getText();
				if (isNotNull()) {
					if (s.length() == 0)
						return (false);
				}
				if (type == INTEGER) {
					try {
						int i = Integer.parseInt(s);
					} catch (NumberFormatException e) {
						return (false);
					}
				}
			}
			return (true);
		}

		public Object getValue() {
			Object o = null;
			switch (type) {
			case BOOLEAN:
				o = new Boolean(((JCheckBox) widget).isSelected());
				break;

			case INTEGER:
				o = Integer.valueOf(((JTextField) widget).getText());
				break;

			case STRING:
				o = new String(((JTextField) widget).getText());
				break;

			case SLIDER:
				o = new Integer(((JSlider) widget).getValue());
				break;

			case COMBO:
				o = new Integer(((JComboBox) widget).getSelectedIndex());
				break;

			case EDITCOMBO:
				o = ((JComboBox) widget).getSelectedItem();
				break;

			case RADIO:
				Enumeration e = buttonGroup.getElements();
				for (int i = 0; e.hasMoreElements(); i++) {
					JRadioButton b = (JRadioButton) e.nextElement();
					if (b.isSelected()) {
						o = new Integer(i);
						break;
					}
				}
				break;

			case LIST:
				o = new Integer(((JList) widget).getSelectedIndex());
				break;

			case COMPONENT:
				o = getComponent().toString();
				break;

			default:
				TRACE.LOG("Unknow type detected");
				break;
			}
			return (o);
		}

		public void setValue(Object o) {
			switch (type) {
			case BOOLEAN:
				((JCheckBox) widget).setSelected(((Boolean) o).booleanValue());
				break;

			case INTEGER:
				((JTextField) widget).setText(((Integer) o).toString());
				break;

			case STRING:
				((JTextField) widget).setText(((String) o));
				break;

			case SLIDER:
				((JSlider) widget).setValue(((Integer) o).intValue());
				break;

			case COMBO:
			case EDITCOMBO:
				if (o instanceof Integer) {
					((JComboBox) widget).setSelectedIndex(((Integer) o).intValue());
				} else {
					((JComboBox) widget).setSelectedItem(o);
				}
				break;

			case RADIO:
				Enumeration e = buttonGroup.getElements();
				for (int i = 0; e.hasMoreElements(); i++) {
					JRadioButton b = (JRadioButton) e.nextElement();
					if (i == ((Integer) o).intValue()) {
						b.setSelected(true);
						break;
					}
				}
				break;

			case LIST:
				((JList) widget).setSelectedIndex(((Integer) o).intValue());
				break;

			case COMPONENT:
				// o=getComponent().toString();
				break;

			default:
				TRACE.LOG("Unknow type detected");
				break;
			}
		}

	}
}