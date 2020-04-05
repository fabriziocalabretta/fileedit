package org.fc.edit.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ResourceBundle;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.fc.seqedit.FindDialog;
import org.fc.seqedit.PackageInfo;
import org.fc.widgets.DynaConstants;
import org.fc.widgets.DynaDialog;
import org.fc.widgets.DynaGridBagPanel;
import org.fc.widgets.DynaPanel;
import org.fc.widgets.SmartDialog;

public class FileEdit extends JFrame implements ActionListener {
	public final static String CMD_FILE_OPEN_FLAT = "file.open.flat";
	public final static String CMD_FILE_OPEN_VSAM = "file.open.vsam";
	public final static String CMD_FILE_SAVE = "file.save";
	public final static String CMD_FILE_SAVE_AS = "file.save.as";
	public final static String CMD_FILE_CLOSE = "file.close";
	public final static String CMD_FILE_EXIT = "file.exit";
	public final static String CMD_EDIT_CUT = "edit.cut";
	public final static String CMD_EDIT_COPY = "edit.copy";
	public final static String CMD_EDIT_PASTE = "edit.paste";

	public final static String CMD_EDIT_INSERT_ABOVE = "edit.insert.above";
	public final static String CMD_EDIT_INSERT_BELOW = "edit.insert.below";
	public final static String CMD_EDIT_DELETE = "edit.delete";
	public final static String CMD_EDIT_DELETE_MULTIPLE = "edit.delete.multiple";
	public final static String CMD_VIEW_ASCII = "view.ascii";
	public final static String CMD_VIEW_EBCDIC = "view.ebcdic";

	public final static String CMD_SEARCH_FIND = "search.find";
	public final static String CMD_SEARCH_PGUP = "search.find.page.up";
	public final static String CMD_SEARCH_PGDW = "search.find.page.down";
	public final static String CMD_SEARCH_FIND_NEXT = "search.find.next";
	public final static String CMD_SEARCH_FIND_PREV = "search.find.prev";
	public final static String CMD_SEARCH_REPLACE = "search.replace";
	public final static String CMD_SEARCH_TOP = "search.top";
	public final static String CMD_SEARCH_BOTTOM = "search.bottom";

	public final static String CMD_TOGGLE_HEXMODE = "toggle.hexmode";
	public final static String CMD_TOGGLE_RECNUM = "toggle.recnum";
	public final static String CMD_TOGGLE_CROSSBEAM = "toggle.cross.beam";

	public final static String CMD_HELP_CONTENTS = "help.contents";
	public final static String CMD_HELP_ABOUT = "help.about";

	ResourceBundle messages = null;
	FileEditorPane fe = null;

	FindDialog findDialog = null;
	FindDialog replaceDialog = null;
	JFileChooser chooser = null;

	JMenuBar menuBar;
	JMenu mFile;
	JMenuItem miOpenFlat;
	JMenuItem miSave;
	JMenuItem miSaveAs;
	JMenuItem miClose;
	JMenuItem miExit;

	JMenu mSearch;
	JMenuItem miPageDown;
	JMenuItem miPageUp;
	JMenuItem miSearchTop;
	JMenuItem miSearchBottom;
	JMenuItem miFind;
	JMenuItem miFindPrev;
	JMenuItem miFindNext;
	JMenuItem miReplace;

	JMenu mEdit;
	JMenuItem miCut;
	JMenuItem miCopy;
	JMenuItem miPaste;
	JMenuItem miInsertAbove;
	JMenuItem miInsertBelow;
	JMenuItem miDelete;
	JMenuItem miDeleteMultiple;
	JMenu mView;
	JRadioButtonMenuItem mrbViewAscii;
	JRadioButtonMenuItem mrbViewEbcdic;

	JMenu mHelp;
	JMenuItem miHelpContents;
	JMenuItem miHelpAbout;

	JButton btnFileOpenFlat;
	JButton btnFileSave;
	JButton btnFileSaveAs;
	JButton btnEditCut;
	JButton btnEditCopy;
	JButton btnEditPaste;
	JButton btnSearchFind;
	JButton btnSearchFindPrev;
	JButton btnSearchFindNext;
	JButton btnSearchReplace;
	JButton btnSearchTop;
	JButton btnSearchBottom;

	JToggleButton btnToggleHexmode;
	JToggleButton btnToggleRecnum;
	JToggleButton btnToggleCrossBeam;

	JFrame parent = this;


	public FileEdit() {
		super();
		this.setSize(800, 600);
		this.setTitle("FileEdit " + PackageInfo.getVersion());
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension frameSize = this.getSize();
		if (frameSize.height > screenSize.height) {
			frameSize.height = screenSize.height;
		}
		if (frameSize.width > screenSize.width) {
			frameSize.width = screenSize.width;
		}
		int posx = (screenSize.width - frameSize.width) / 2;
		int posy = (screenSize.height - frameSize.height) / 2;
		this.setLocation(posx, posy);

		messages = ResourceBundle.getBundle("org.fc.edit.messages");
		setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		//setLookAndFeel("de.javasoft.plaf.synthetica.SyntheticaBlackMoonLookAndFeel");
		//setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		initWidgets();
		setMenuState();
		this.setVisible(true);
	}

	void initWidgets() {
		JToolBar toolBar = new JToolBar();

		btnFileOpenFlat = addToolbarButton(toolBar, CMD_FILE_OPEN_FLAT);
		btnFileSave = addToolbarButton(toolBar, CMD_FILE_SAVE);
		btnFileSaveAs = addToolbarButton(toolBar, CMD_FILE_SAVE_AS);

		toolBar.addSeparator();

		btnEditCut = addToolbarButton(toolBar, CMD_EDIT_CUT);
		btnEditCopy = addToolbarButton(toolBar, CMD_EDIT_COPY);
		btnEditPaste = addToolbarButton(toolBar, CMD_EDIT_PASTE);

		toolBar.addSeparator();

		btnSearchFind = addToolbarButton(toolBar, CMD_SEARCH_FIND);
		btnSearchFindPrev = addToolbarButton(toolBar, CMD_SEARCH_FIND_PREV);
		btnSearchFindNext = addToolbarButton(toolBar, CMD_SEARCH_FIND_NEXT);
		btnSearchReplace = addToolbarButton(toolBar, CMD_SEARCH_REPLACE);
		btnSearchTop = addToolbarButton(toolBar, CMD_SEARCH_TOP);
		btnSearchBottom = addToolbarButton(toolBar, CMD_SEARCH_BOTTOM);

		toolBar.addSeparator();

		btnToggleHexmode = addToolbarToggle(toolBar, CMD_TOGGLE_HEXMODE);
		btnToggleRecnum = addToolbarToggle(toolBar, CMD_TOGGLE_RECNUM);
		btnToggleCrossBeam = addToolbarToggle(toolBar, CMD_TOGGLE_CROSSBEAM);

		menuBar = new JMenuBar();
		mFile = new JMenu();
		mFile.setText(messages.getString("menu.file"));
		miOpenFlat = addMenuItem(mFile, CMD_FILE_OPEN_FLAT);
		miSave = addMenuItem(mFile, CMD_FILE_SAVE);
		miSaveAs = addMenuItem(mFile, CMD_FILE_SAVE_AS);
		miClose = addMenuItem(mFile, CMD_FILE_CLOSE);
		mFile.addSeparator();
		miExit = addMenuItem(mFile, CMD_FILE_EXIT);
		menuBar.add(mFile);

		mEdit = new JMenu();
		mEdit.setText(messages.getString("menu.edit"));
		miCut = addMenuItem(mEdit, CMD_EDIT_CUT);
		miCopy = addMenuItem(mEdit, CMD_EDIT_COPY);
		miPaste = addMenuItem(mEdit, CMD_EDIT_PASTE);
		mEdit.addSeparator();
		miInsertAbove = addMenuItem(mEdit, CMD_EDIT_INSERT_ABOVE);
		miInsertBelow = addMenuItem(mEdit, CMD_EDIT_INSERT_BELOW);
		miDelete = addMenuItem(mEdit, CMD_EDIT_DELETE);
		miDeleteMultiple = addMenuItem(mEdit, CMD_EDIT_DELETE_MULTIPLE);
		mEdit.addSeparator();
		mView = new JMenu();
		mView.setText(messages.getString("menu.view"));
		ButtonGroup group = new ButtonGroup();
		mrbViewAscii = addRadioButtonMenu(mView, group, CMD_VIEW_ASCII);
		mrbViewAscii.setSelected(true);
		mrbViewEbcdic = addRadioButtonMenu(mView, group, CMD_VIEW_EBCDIC);
		mEdit.add(mView);
		menuBar.add(mEdit);

		mSearch = new JMenu();
		mSearch.setText(messages.getString("menu.search"));
		miPageUp = addMenuItem(mSearch, CMD_SEARCH_PGUP, KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0));
		miPageDown = addMenuItem(mSearch, CMD_SEARCH_PGDW, KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0));
		mSearch.addSeparator();
		miFind = addMenuItem(mSearch, CMD_SEARCH_FIND, KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.CTRL_MASK));
		miFindPrev = addMenuItem(mSearch, CMD_SEARCH_FIND_PREV, KeyStroke.getKeyStroke(KeyEvent.VK_F3, InputEvent.SHIFT_MASK));
		miFindNext = addMenuItem(mSearch, CMD_SEARCH_FIND_NEXT, KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0));
		miReplace = addMenuItem(mSearch, CMD_SEARCH_REPLACE);
		mSearch.addSeparator();
		miSearchTop = addMenuItem(mSearch, CMD_SEARCH_TOP, KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK));
		miSearchBottom = addMenuItem(mSearch, CMD_SEARCH_BOTTOM, KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK));
		menuBar.add(mSearch);

		mHelp = new JMenu();
		mHelp.setText(messages.getString("menu.help"));
		miHelpContents = addMenuItem(mHelp, CMD_HELP_CONTENTS);
		miHelpAbout = addMenuItem(mHelp, CMD_HELP_ABOUT);
		menuBar.add(mHelp);

		this.setJMenuBar(menuBar);
		JPanel contentPane = (JPanel) this.getContentPane();
		contentPane.setLayout(new BorderLayout());
		fe = new FileEditorPane(this, messages);
		contentPane.add(toolBar, BorderLayout.NORTH);
		contentPane.add(fe, BorderLayout.CENTER);
	}

	public void doOpen(boolean flat) {
		if (!doClose()) {
			return;
		}
		if (flat) {
			DynaGridBagPanel dp;
			if (chooser == null) {
				chooser = new JFileChooser();
				dp = new DynaGridBagPanel(this, "");
				dp.addField("RECLEN", messages.getString("labels.record.length"), DynaDialog.INTEGER, null, true);
				dp.addField("READONLY", messages.getString("labels.read.only"), DynaDialog.BOOLEAN, null);
				dp.addField("VARLEN", messages.getString("labels.variable.length"), DynaDialog.BOOLEAN, null);
				dp.addField("VARLENLE", messages.getString("labels.variable.length.le"), DynaDialog.BOOLEAN, null);
				dp.setPanelBorder(DynaPanel.BORDER_NONE);
				dp.pack();
				chooser.setAccessory(dp);
			} else {
				dp = (DynaGridBagPanel) chooser.getAccessory();
			}
			for (;;) {
				int returnVal = chooser.showOpenDialog(parent);
				if (returnVal != JFileChooser.APPROVE_OPTION) {
					return;
				}
				if (dp.verifyData()) {
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						doClose();
						Integer reclen = (Integer) dp.getValue("RECLEN");
						Boolean ro = (Boolean) dp.getValue("READONLY");
						Boolean varlen=(Boolean) dp.getValue("VARLEN");
						Boolean varlenLE=(Boolean) dp.getValue("VARLENLE");
						if (varlen.booleanValue()) {
							ro=new Boolean(true);
							reclen=new Integer(32*1024);
						}
						System.out.println("apro a " + reclen + " ro=" + ro);
						open(chooser.getSelectedFile(), reclen.intValue(), ro.booleanValue(), varlen.booleanValue(), varlenLE.booleanValue());
					}
					break;
				}
			}
		} else {
		}
		setMenuState();
	}

	public void doSave() {
		try {
			System.out.println("doSave()");
			fe.save();
			System.out.println("file saved");
			SmartDialog.messageBox(this, messages.getString("msg.file.saved"));
		} catch (IOException e) {
			showException(e);
		}
		setMenuState();
	}

	public void doSaveAs() {
	}

	public void doExit() {
		if (doClose()) {
			System.exit(0);
		}
	}

	public boolean doClose() {
		try {
			if (fe.isModified()) {
				int rc = JOptionPane.showConfirmDialog(null, messages.getString("msg.confirm.file.changed"), messages
						.getString("dialog.file.changed"), JOptionPane.YES_NO_CANCEL_OPTION);
				switch (rc) {
				case JOptionPane.YES_OPTION:
					fe.save();
					break;
				case JOptionPane.NO_OPTION:
					fe.revertChanges();
					break;
				default:
					return false;
				}
			}
			fe.close();
		} catch (IOException e) {
			showException(e);
		}
		setMenuState();
		return true;
	}

	public void open(File f, int reclen, boolean ro, boolean vl, boolean vlle) {
		new OpenFileThread(f, reclen, ro, vl, vlle).start();
		/*
		 * try { fe.open(f,reclen, ro); fe.requestFocus(); } catch (Exception e) {
		 * showException(e); }
		 */
	}


	public void doToggleHexmode() {
		fe.setHexMode(btnToggleHexmode.isSelected());
	}

	public void doToggleRecnum() {
		fe.setShowRecordNumber(btnToggleRecnum.isSelected());
	}

	public void doToggleCrossBeam() {
		fe.setShowCrossBeam(btnToggleCrossBeam.isSelected());
	}

	public void doChangeConversionMode(int m) {
		fe.setConversionMode(m);
	}

	public void doPageUp() {
		fe.pageUp();
	}

	public void doPageDown() {
		fe.pageDown();
	}

	public void doLocateTop() {
		fe.locateTop();
	}

	public void doLocateBottom() {
		fe.locateBottom();
	}

	public void doCopy() {
		fe.copySelection();
	}

	public void doPaste() {
		fe.pasteClipboardContents();
	}

	public void doCut() {
		fe.cutSelection();
	}

	public void doFind() {
		if (findDialog == null) {
			findDialog = new FindDialog(this, messages, false);
		}
		findDialog.setVisible(true);

		if (findDialog.isOk()) {
			System.out.println("INIZIO IL FIND");
			byte[] s = findDialog.getFindWhat();
			fe.find(s, findDialog.isIgnoreCase(), (findDialog.getDirection() > 0), findDialog.isOnRange(), findDialog
					.getRangeFrom(), findDialog.getRangeTo());
		}
	}

	public void doReplace() {
		if (replaceDialog == null) {
			replaceDialog = new FindDialog(this, messages, true);
		}
		replaceDialog.setVisible(true);

		if (replaceDialog.isOk()) {
			System.out.println("INIZIO REPLACE");
			byte[] s = replaceDialog.getFindWhat();
			byte[] r = replaceDialog.getReplaceWith();

			fe.replace(s, r, replaceDialog.isIgnoreCase(), (replaceDialog.getDirection() > 0), replaceDialog.isOnRange(),
					replaceDialog.getRangeFrom(), replaceDialog.getRangeTo());
		}
	}

	public void doFindPrevious() {
		fe.findPrevious();
	}

	public void doFindNext() {
		fe.findNext();
	}

	public void doInsertRecord(boolean above) {
		boolean rc = fe.insertRecord(above);
		if (!rc) {
			message(messages.getString("msg.cannot.insert.here"));
		}
	}

	public void doDeleteRecord() {
		fe.deleteRecord();
	}

	public void doDeleteMultiple() {
		DynaDialog d = new DynaDialog(this, true, "Delete multiple");
		d.addField("FROM", messages.getString("labels.delete.multiple.from"), DynaConstants.INTEGER, null, true);
		d.addField("TO", messages.getString("labels.delete.multiple.to"), DynaConstants.INTEGER, null, true);
		d.pack();
		d.setVisible(true);
		if (d.isOk()) {
			Integer f = (Integer) d.getValue("FROM");
			Integer t = (Integer) d.getValue("TO");
			if (t.longValue() < f.longValue()) {
				SmartDialog.messageBox(this, messages.getString("msg.wrong.range"));
			} else {
				fe.deleteMultiple(f.intValue(), t.longValue());
			}
		}
	}

	public void doHelp() {
		SmartDialog.errorBox(this, "You ingenuous!!!");
	}

	public void doAbout() {
		DynaDialog dd = new DynaDialog(this, true, messages.getString("caption.about"));
		dd.addLabel("File Edit");
		dd.addLabel("Version " + org.fc.seqedit.PackageInfo.getVersion());
		dd.setVisible(true);
	}

	public long acceptNumberedKey(long rrn) {
		return 0;
	}

	public void actionPerformed(ActionEvent evt) {
		String cmd = evt.getActionCommand();
		if (cmd.equals(CMD_FILE_OPEN_FLAT)) {
			doOpen(true);
		} else if (cmd.equals(CMD_FILE_OPEN_VSAM)) {
			doOpen(false);
		} else if (cmd.equals(CMD_FILE_EXIT)) {
			doExit();
		} else if (cmd.equals(CMD_FILE_SAVE)) {
			doSave();
		} else if (cmd.equals(CMD_FILE_SAVE_AS)) {
			doSaveAs();
		} else if (cmd.equals(CMD_FILE_CLOSE)) {
			doClose();
		} else if (cmd.equals(CMD_EDIT_COPY)) {
			doCopy();
		} else if (cmd.equals(CMD_EDIT_PASTE)) {
			doPaste();
		} else if (cmd.equals(CMD_EDIT_CUT)) {
			doCut();
		} else if (cmd.equals(CMD_TOGGLE_HEXMODE)) {
			doToggleHexmode();
		} else if (cmd.equals(CMD_TOGGLE_RECNUM)) {
			doToggleRecnum();
		} else if (cmd.equals(CMD_TOGGLE_CROSSBEAM)) {
			doToggleCrossBeam();
		} else if (cmd.equals(CMD_SEARCH_PGUP)) {
			doPageUp();
		} else if (cmd.equals(CMD_SEARCH_PGDW)) {
			doPageDown();
		} else if (cmd.equals(CMD_SEARCH_TOP)) {
			doLocateTop();
		} else if (cmd.equals(CMD_SEARCH_BOTTOM)) {
			doLocateBottom();
		} else if (cmd.equals(CMD_SEARCH_FIND)) {
			doFind();
		} else if (cmd.equals(CMD_SEARCH_REPLACE)) {
			doReplace();
		} else if (cmd.equals(CMD_SEARCH_FIND_PREV)) {
			doFindPrevious();
		} else if (cmd.equals(CMD_SEARCH_FIND_NEXT)) {
			doFindNext();
		} else if (cmd.equals(CMD_EDIT_INSERT_ABOVE)) {
			doInsertRecord(true);
		} else if (cmd.equals(CMD_EDIT_INSERT_BELOW)) {
			doInsertRecord(false);
		} else if (cmd.equals(CMD_EDIT_DELETE)) {
			doDeleteRecord();
		} else if (cmd.equals(CMD_EDIT_DELETE_MULTIPLE)) {
			doDeleteMultiple();
		} else if (cmd.equals(CMD_VIEW_ASCII)) {
			doChangeConversionMode(FileEditorPane.CONVERSION_NONE);
		} else if (cmd.equals(CMD_VIEW_EBCDIC)) {
			doChangeConversionMode(FileEditorPane.CONVERSION_EBCDIC);
		} else if (cmd.equals(CMD_HELP_CONTENTS)) {
			doHelp();
		} else if (cmd.equals(CMD_HELP_ABOUT)) {
			doAbout();
		}
		fe.requestFocus();
	}

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			doExit();
		}
	}

	public static void main(String[] argv) {
		FileEdit j = new FileEdit();
		if (argv.length > 0) {
			int len = 80;
			String file = null;
			String catalog = null;
			boolean ro = false;
			OPTS: for (int i = 0; i < argv.length; i++) {
				if (argv[i].startsWith("-")) {
					if (argv[i].equals("-help")) {
						showUsage();
					}
					if (argv[i].equals("-version")) {
						showVersion();
					}
					if (argv[i].equals("-length")) {
						len = Integer.parseInt(argv[++i]);
						continue OPTS;
					}
					if (argv[i].equals("-ro")) {
						ro = true;
						continue OPTS;
					}
					
					continue OPTS;
				}
				if (file == null) {
					file = argv[i];
					continue OPTS;
				}
				if (catalog == null) {
					catalog = argv[i];
					continue OPTS;
				}
			}
			if (file == null) {
				FileEdit.wrongParms("missing file name");
			}
		
			j.open(new File(file), len, ro, false, false);
		}
	}

	public static void wrongParms(String m) {
		System.err.println("wrong parameters: " + m);
		FileEdit.showUsage();
	}

	public static void showUsage() {
		System.err.println("usage:\n");
		System.err.println("  FileEdit [options] <cluster|file> [catalog]\n");
		System.err.println("options:");
		System.err.println("  -flat         open a flat file");
		System.err.println("  -length <l>   specifies length <l> for flat file (ignore for vsam)");
		System.err.println("  -ro           open file read only");
		System.err.println("  -version      show version");
		System.err.println("  -help         show this help");
		System.exit(0);
	}

	public static void showVersion() {
		System.err.println("Jedi version " + PackageInfo.getVersion());
		System.exit(0);
	}

	public void message(String msg) {
		SmartDialog.errorBox(this, msg);
	}

	JToggleButton addToolbarToggle(JToolBar t, String cmd) {
		JToggleButton b = new JToggleButton();
		b.addActionListener(this);
		b.setActionCommand(cmd);
		try {
			String res = "graphics/" + cmd + ".16.gif";
			b.setIcon(new ImageIcon(getClass().getResource(res)));
		} catch (NullPointerException e) {
			b.setText(messages.getString("toolbar." + cmd));
		}

		t.add(b);
		return b;
	}

	JButton addToolbarButton(JToolBar t, String cmd) {
		JButton b = new JButton();

		try {
			String res = "graphics/" + cmd + ".16.gif";
			b.setIcon(new ImageIcon(getClass().getResource(res)));
		} catch (NullPointerException e) {
			b.setText(messages.getString("toolbar." + cmd));
		}
		b.addActionListener(this);
		b.setActionCommand(cmd);
		t.add(b);
		return b;
	}

	JMenuItem addMenuItem(JMenu m, String cmd, KeyStroke ks) {
		JMenuItem mi = addMenuItem(m, cmd);
		mi.setAccelerator(ks);
		return mi;
	}

	JMenuItem addMenuItem(JMenu m, String cmd) {
		JMenuItem mi = new JMenuItem();
		mi.setText(messages.getString("menu." + cmd));
		try {
			String res = "graphics/" + cmd + ".16.gif";
			mi.setIcon(new ImageIcon(getClass().getResource(res)));
		} catch (NullPointerException e) {
		}
		// mi.setHorizontalAlignment(SwingConstants.LEFT);
		// mi.setHorizontalTextPosition(SwingConstants.LEADING);
		mi.setActionCommand(cmd);
		mi.addActionListener(this);
		m.add(mi);
		return mi;
	}

	JRadioButtonMenuItem addRadioButtonMenu(JMenu m, ButtonGroup grp, String cmd) {
		JRadioButtonMenuItem mi = new JRadioButtonMenuItem();
		mi.setText(messages.getString("menu." + cmd));
		try {
			String res = "graphics/" + cmd + ".16.gif";
			mi.setIcon(new ImageIcon(getClass().getResource(res)));
		} catch (NullPointerException e) {
		}
		// mi.setHorizontalAlignment(SwingConstants.LEFT);
		// mi.setHorizontalTextPosition(SwingConstants.LEADING);
		mi.setActionCommand(cmd);
		mi.addActionListener(this);
		m.add(mi);
		grp.add(mi);
		return mi;
	}

	void showException(Exception e) {
		SmartDialog.errorBox(this, e);
	}

	void setMenuState() {
		boolean b = false;
		boolean sel = false;
		boolean flat = true;

		if (fe != null) {
			b = fe.isOpened();
			sel = fe.hasSelection();
			flat = fe.isFlat();
		}
		miOpenFlat.setEnabled(true);
		miSave.setEnabled(b);
		miSaveAs.setEnabled(false);
		miClose.setEnabled(b);
		miExit.setEnabled(true);

		mSearch.setEnabled(b);
		// miSearchTop.setEnabled(b);
		// miSearchBottom.setEnabled(b);

		mEdit.setEnabled(b);
		miCut.setEnabled(b && sel);
		miCopy.setEnabled(b && sel);
		miPaste.setEnabled(b);
		// miFind.setEnabled(b);
		// miFindPrev.setEnabled(b);
		// miFindNext.setEnabled(b);
		miReplace.setEnabled(b);
		// miInsertAbove.setEnabled(b);
		// miInsertBelow.setEnabled(b);
		// miDelete.setEnabled(b);

		btnFileOpenFlat.setEnabled(true);

		btnFileSave.setEnabled(b);
		btnFileSaveAs.setEnabled(b);

		btnEditCut.setEnabled(b && sel);
		btnEditCopy.setEnabled(b && sel);
		btnEditPaste.setEnabled(b);
		btnSearchFind.setEnabled(b);
		btnSearchFindPrev.setEnabled(b);
		btnSearchFindNext.setEnabled(b);
		btnSearchReplace.setEnabled(b);
		btnSearchTop.setEnabled(b);
		btnSearchBottom.setEnabled(b);
		miDeleteMultiple.setEnabled(flat);

		btnToggleHexmode.setEnabled(b);
		btnToggleRecnum.setEnabled(b);
		btnToggleCrossBeam.setEnabled(b);

	}

	void setLookAndFeel(String l) {
		try {
			UIManager.setLookAndFeel(l);
			SwingUtilities.updateComponentTreeUI(this);
		} catch (Exception ex) {
		}
	}

	class OpenFileThread extends Thread {
		File file;
		int reclen;
		boolean ro;
		boolean varlen;
		boolean varlenLE;

		public OpenFileThread(File f, int r, boolean b, boolean vl, boolean vlle) {
			file = f;
			reclen = r;
			ro = b;
			varlen=vl;
			varlenLE=vlle;
		}

		public void run() {
			try {
				fe.open(file, reclen, ro, varlen, varlenLE);
				fe.requestFocus();
				setMenuState();
			} catch (Exception e) {
				showException(e);
			}
		}
	}
}
