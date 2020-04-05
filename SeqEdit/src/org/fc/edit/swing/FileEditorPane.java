package org.fc.edit.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;

import org.fc.hdm.ByteArray;
import org.fc.io.DataFile;
import org.fc.io.FlatFile;
import org.fc.io.FlatRecord;
import org.fc.io.Record;
import org.fc.io.RrdsKey;
import org.fc.io.VariableFlatFile;
import org.fc.seqedit.EditorModel;
import org.fc.utils.Ascii2EbcdicConverter;
import org.fc.utils.ByteConverter;
import org.fc.utils.Ebcdic2AsciiConverter;
import org.fc.utils.SimpleConverter;
import org.fc.widgets.SmartDialog;

/**
 * bean che incapsula tutta la baracca dell'editor e funge da controller
 */
public class FileEditorPane extends JPanel implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {
	public final static int MODE_REPLACE = 0;
	public final static int MODE_REPLACE_ALL = 1;
	public final static int MODE_SKIP = 2;
	public final static int MODE_CANCEL = 3;

	EditorModel model;
	EditorView view;
	DataFile df = null;
	JPanel statusBar;
	JLabel lMsg;
	JLabel lRo;
	JLabel lOffset;
	JLabel lInsert;
	JProgressBar progressBar;
	ResourceBundle messages;
	// Record lastRecordRead=null;
	JFrame parent;
	ByteConverter outputConverter;
	ByteConverter inputConverter;
	ByteConverter converterAscii2Ebcdic;
	ByteConverter converterEbcdic2Ascii;
	ByteConverter converterAsis;

	int currentRecordStatus;
	public final static int EXISTING = 0;
	public final static int MODIFIED = 1;
	public final static int DELETED = 2;
	public final static int NEW = 3;

	public final static int CONVERSION_NONE = 0;
	public final static int CONVERSION_EBCDIC = 1;
	// public final static int CONVERSION_EBC2ASC = 2;

	int conversionMode = FileEditorPane.CONVERSION_NONE;
	boolean insertMode;

	Rectangle selection = null;
	boolean selecting = false;

	ByteArray findWhat = null;
	ByteArray replaceWith = null;
	// int findFromIndex=-1;
	// int findFromColumn=0;
	boolean findIgnoreCase;
	boolean findOnRange;
	int findRangeFrom;
	int findRangeTo;
	Rectangle findArea = null;

	public FileEditorPane(JFrame p, ResourceBundle m) {
		messages = m;
		parent = p;
		insertMode = false;
		initWidgets();
		addKeyListener(this);
		converterAscii2Ebcdic = new Ascii2EbcdicConverter();
		converterEbcdic2Ascii = new Ebcdic2AsciiConverter();
		converterAsis = new SimpleConverter();
		outputConverter = converterAsis;
		inputConverter = converterAsis;
		refreshStatusBar();
	}

	/** Per la gestione del focus */
	// public boolean isFocusTraversable() { return(true); }
	/** Per la gestione del focus */
	// public boolean isManagingFocus() { return(true); }
	void this_focusLost(FocusEvent e) {
		this.requestFocus();
	}

	void initWidgets() {
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addMouseWheelListener(this);
		// this.setBorder(BorderFactory.createEtchedBorder());
		model = new EditorModel(40, 20);
		view = new EditorView(model, this);
		JPanel pview = new JPanel(new BorderLayout());
		pview.add(view, BorderLayout.CENTER);
		pview.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		// statusBar.setBorder(BorderFactory.createEtchedBorder());
		lOffset = new JLabel("off 0");
		// lOffset.setFont(new Font("monospaced", Font.PLAIN, 12));
		lOffset.setBorder(BorderFactory.createLoweredBevelBorder());
		lRo = new JLabel();
		lRo.setBorder(BorderFactory.createLoweredBevelBorder());
		lInsert = new JLabel();
		lInsert.setBorder(BorderFactory.createLoweredBevelBorder());

		lMsg = new JLabel();

		statusBar.add(lMsg);
		statusBar.add(lRo);
		statusBar.add(lOffset);
		statusBar.add(lInsert);

		this.setLayout(new BorderLayout());

		this.add(pview, BorderLayout.CENTER);
		this.add(statusBar, BorderLayout.SOUTH);
	}


	public void open(File file, int reclen, boolean readOnly, boolean vl, boolean vlle) throws IOException {
		if (vl) {
			df = new VariableFlatFile(file, reclen, vlle);
		} else {
			df = new FlatFile(file, reclen);
		}
		ProgressMonitor progressMonitor = new ProgressMonitor(parent, messages.getString("progress.open") + " " + file + " ...",
				"", 0, 100);
		progressMonitor.setProgress(0);
		progressMonitor.setMillisToDecideToPopup(2000);
		df.setProgressMonitor(progressMonitor);
		open(readOnly);
		progressMonitor.close();
	}

	void open(boolean readOnly) throws IOException {
		df.open(readOnly);
		model.clear();
		model.setMaxRecordLength(df.getMaxRecordLength());
		if (df.hasIndex()) {
			view.setKeyInfo(df.getKeyInfo());
		} else {
			view.setKeyInfo(null);
		}
		currentRecordStatus = EXISTING;
		view.recalculateSize();
		refresh();
		refreshStatusBar();
	}

	public boolean isModified() {
		boolean rc = false;
		if (df != null) {
			releaseCurrentRecord();
			rc = df.isModified();
		}
		return rc;
	}

	public void save() throws IOException {
		releaseCurrentRecord();
		df.save();
	}

	public void revertChanges() throws IOException {
		df.revertChanges();
	}

	public void close() {
		if (df != null && df.isOpened()) {
			try {
				df.close();
				df = null;
				model.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		refreshStatusBar();
	}

	public void refresh() {
		if (df == null) {
			return;
		}
		// System.out.println("refresh()");
		try {
			releaseCurrentRecord();
			if (model.size() > 0) {
				Record f = (Record) model.getFirst();
				// lastRecordRead=null;
				df.seek(f.getKey(), DataFile.GTEQ);
			}
			model.clear();
			scrollDown();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchElementException nse) {
		}
	}

	public void seek(Record b, boolean backward) {
		// System.out.println("SEEK backward="+backward +" per
		// "+model.getMaxRecords() );
		try {
			df.seek(b.getKey(), DataFile.GTEQ);
			model.clear();
			try {
				loadModel(model.getMaxRecords(), backward);
			} catch (EOFException e) {
				System.out.println("EOF gestito: " + e);
			}
			view.cursorHome();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void scroll(int n, boolean backward) {
		// System.out.println("SCROLL "+n+ "backward="+backward);
		try {
			if (model.size() > 0) {
				Record b = (Record) (backward ? model.getFirst() : model.getLast());
				// System.out.println("seek "+b);
				df.seek(b.getKey(), (backward ? DataFile.GTEQ : DataFile.GREATER));
				// System.out.println("trovato");
			}
			loadModel(n, backward);
		} catch (EOFException eof) {
			if (backward) {
				System.out.println("********** BOF *************");
				getToolkit().beep();
				// refresh();
			} else {
				System.out.println("********** EOF *************");
				getToolkit().beep();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}

	public void loadModel(int n, boolean backward) throws IOException {
		for (int i = 0; i < n; i++) {
			Record r;
			if (backward) {
				r = df.readPrevious();
				model.addFirst(r);
			} else {
				r = df.readNext();
				model.addLast(r);
			}
		}
	}

	public boolean insertRecord(boolean above) {
		// RecordKey rk=acceptRecordKey();
		releaseCurrentRecord();
		Record r = createEmptyRecord(above);
		if (r == null)
			return false;

		model.insertRecord(r, view.getCurrentIndex() + (above ? 1 : 2));
		if (df instanceof FlatFile) {
			model.renum();
		}
		if (!above) {
			cursorDown();
		}
		currentRecordStatus = NEW;
		return true;
	}

	public boolean deleteRecord() {
		Record r = getCurrentRecord();
		try {
			df.delete(r.getKey());
		} catch (IOException e) {
			e.printStackTrace();
		}
		refresh();
		return true;
	}

	public boolean deleteMultiple(long f, long t) {
		try {
			for (long i = f; i <= t; i++) {
				df.delete(new RrdsKey(f));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		refresh();
		return true;
	}

	public void putByte(byte b) {
		if (insertMode) {
			byte[] v = getCurrentRecord().getBytes();
			if (v[v.length - 1] == (byte) 0) {
				int i = view.getCursorX();
				int l = v.length - i - 1;
				System.arraycopy(v, i, v, i + 1, l);
			} else {
				getToolkit().beep();
				return;
			}
		}
		model.setByte(view.getCursorX() - 1, view.getCurrentIndex(), b);
		if (currentRecordStatus == EXISTING) {
			currentRecordStatus = MODIFIED;
		}
		cursorRight();
	}

	public void backSpace() {
		if (view.getCursorX() > 1) {
			cursorLeft();
			deleteChar();
		} else {
			getToolkit().beep();
		}
	}

	public void deleteChar() {
		byte[] v = getCurrentRecord().getBytes();
		int i = view.getCursorX() - 1;
		System.arraycopy(v, i + 1, v, i, v.length - i - 1);
		model.setByte(v.length - 1, view.getCurrentIndex(), (byte) 0);

		if (currentRecordStatus == EXISTING) {
			currentRecordStatus = MODIFIED;
		}
		// model.dataChanged();
	}

	public void cursorUp() {
		if (leavingRecord(true)) {
			if (releaseCurrentRecord()) {
				return;
			}
		}
		if (!view.cursorUp()) {
			scroll(1, true);
		}
	}

	public void cursorDown() {
		if (leavingRecord(false)) {
			if (releaseCurrentRecord()) {
				return;
			}
		}
		if (!view.cursorDown()) {
			scroll(1, false);
		}

	}

	public void cursorNextLine() {
		if (releaseCurrentRecord()) {
			return;
		}
		if (!view.cursorNextLine()) {
			scroll(1, false);
		}
		refreshStatusBar();
	}

	public void cursorLeft() {
		view.cursorLeft();
		refreshStatusBar();
	}

	public void cursorRight() {
		view.cursorRight();
		refreshStatusBar();
	}

	public void cursorBeginOfLine() {
		view.cursorBeginOfLine();
		refreshStatusBar();
	}

	public void cursorEndOfLine() {
		view.cursorEndOfLine();
		refreshStatusBar();
	}

	public void pageUp() {
		releaseCurrentRecord();
		scrollUp();
	}

	public void pageDown() {
		releaseCurrentRecord();
		scrollDown();
	}

	public void cursorAt(int x, int y) {
		if (leavingRecord(true)) {
			if (releaseCurrentRecord()) {
				return;
			}
		}
		view.cursorOn(x, y);
		refreshStatusBar();
	}

	public void cursorAtIndex(int x, int y) {
		view.cursorOnIndex(y);
		view.cursorOnColumn(x);
		refreshStatusBar();
	}

	public void scrollUp() {
		scroll(model.getMaxRecords(), true);
	}

	public void scrollDown() {
		scroll(model.getMaxRecords(), false);
	}

	public void locateTop() {
		releaseCurrentRecord();
		try {
			df.locate(DataFile.FIRST);
			model.clear();
			loadModel(model.getMaxRecords(), false);
			view.cursorHome();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void locateBottom() {
		releaseCurrentRecord();
		try {
			df.locate(DataFile.LAST);
			model.clear();
			loadModel(model.getMaxRecords(), true);
			view.cursorBottom();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean leavingRecord(boolean movingUp) {
		if (view.isHexMode()) {
			// System.out.println("--> "+view.getEditArea());
			if (movingUp) {
				return (view.getEditArea() == EditorView.AREA_CHAR);
			} else {
				return (view.getEditArea() == EditorView.AREA_HEX2);
			}
		}
		return true;
	}

	public void viewCapacityChanged(int r) {
		if (df != null && df.isOpened()) {
			if (r != model.getMaxRecords()) {
				model.setMaxRecords(r);
				refresh();
			}
		}
	}

	public Record getCurrentRecord() {
		return (Record) model.get(view.getCurrentIndex());
	}

	public boolean releaseCurrentRecord() {
		Record r = null;
		boolean cursorRepositioned = false;
		switch (currentRecordStatus) {
		case MODIFIED:
			r = getCurrentRecord();
			// System.out.println("aggiorno "+r.toString());
			try {
				df.rewrite(r);
			} catch (IOException e) {
				SmartDialog.errorBox(parent, e);
				e.printStackTrace();
			}
			break;

		case NEW:
			r = getCurrentRecord();
			// System.out.println("inserisco "+r.toString());
			try {
				df.write(r);
				// refresh();
			} catch (IOException e) {
				SmartDialog.errorBox(parent, e);
				e.printStackTrace();
			}
			break;

		case EXISTING:
			break;

		default:
			System.out.println("stato record corrent non gestito " + currentRecordStatus);
			break;
		}
		currentRecordStatus = EXISTING;
		return cursorRepositioned;
	}

	public Record createEmptyRecord(boolean above) {
		if (df == null) {
			return null;
		}
		if (df instanceof FlatFile) {
			// long n=((FlatRecord)getCurrentRecord()).getRrn();
			// n+=(above?-1:1);
			// if (n<1) { return null; }
			return new FlatRecord(model.getMaxRecordLength(), new RrdsKey(0));
		}

		return null;
	}

	void refreshStatusBar() {
		if (df != null && df.isOpened()) {
			lMsg.setText(df.toString());
			lRo.setText(df.isReadOnly() ? "RO" : "RW");
			lOffset.setText(Integer.toString(view.getCursorX() - 1));
		} else {
			lMsg.setText("Ready.");
			lOffset.setText("-");
			lRo.setText("-");
		}
		lInsert.setText(insertMode ? "INS" : "OVR");
	}

	void prepareFind(byte[] s, boolean ignoreCase, boolean onRange, int f, int t) {
		findIgnoreCase = ignoreCase;
		findOnRange = onRange;
		if (findOnRange) {
			findRangeFrom = f;
			findRangeTo = t;
		} else {
			findRangeFrom = 0;
			findRangeTo = model.getMaxRecordLength();
		}
		findWhat = new ByteArray(s);
		findArea = null;
		if (ignoreCase) {
			findWhat = findWhat.toUpperCase();
		}
	}

	public void replace(byte[] s, byte[] r, boolean ignoreCase, boolean forward, boolean range, int f, int t) {
		prepareFind(s, ignoreCase, range, f, t);
		boolean found = true;
		int mode = MODE_REPLACE;
		REPLACE: while (found) {
			found = findOccurrence(forward);
			if (found) {
				System.out.println("replace");
				if (mode != MODE_REPLACE_ALL) {
					mode = getReplaceMode();
				}
				switch (mode) {
				case MODE_REPLACE:
				case MODE_REPLACE_ALL:
					for (int i = 0; i < r.length; i++) {
						putByte(r[i]);
					}
					break;

				case MODE_CANCEL:
					break REPLACE;

				default:
				case MODE_SKIP:

				}
			}
		}

	}

	public void find(byte[] s, boolean ignoreCase, boolean forward, boolean range, int f, int t) {
		prepareFind(s, ignoreCase, range, f, t);
		findOccurrence(forward);
	}

	public void findPrevious() {
		findOccurrence(false);
	}

	public void findNext() {
		findOccurrence(true);
	}

	public void doReplace(boolean forward) {
		boolean all = false;
		boolean replace = (replaceWith != null);
	}

	public boolean findOccurrence(boolean forward) {
		// System.out.println("cerco in "+(forward?"avanti":"indietro"));
		if (findWhat == null) {
			return false;
		}

		if (leavingRecord(false)) {
			if (releaseCurrentRecord()) {
				return false;
			}
		}
		int direction;
		if (forward) {
			direction = 1;
		} else {
			direction = -1;
		}
		int findFromIndex = view.getCurrentIndex();
		int findFromColumn = view.getCursorX() - 1;
		if (!forward) {
			findFromColumn--;
		}

		if (findArea != null && findArea.getX() == findFromColumn && findArea.getY() == findFromIndex) {
			// System.out.println("findarea uguale");
			findFromColumn++;
		}
		ByteArray ba;

		// System.out.println("cerco "+(forward?"next":"prev")+" da
		// riga="+findFromIndex+ " col="+findFromColumn);
		// System.out.println("range "+findOnRange+" da "+findRangeFrom +" a "
		// +findRangeTo);

		/** ricerco all'interno della parte visibile */
		for (int i = findFromIndex; (i >= 0 && i < model.size()); i += direction) {
			/*
			 * System.out.println("i="+i);
			 * System.out.println("max="+model.size());
			 * System.out.println("dir="+direction);
			 */
			ba = (ByteArray) model.get(i);
			if (findIgnoreCase) {
				ba = (ByteArray) ba.toUpperCase();
			}
			int pos;
			if (forward) {
				pos = ba.indexOf(findWhat, findFromColumn);
			} else {
				pos = ba.lastIndexOf(findWhat, findFromColumn);
			}
			if (posOnFindRange(pos)) {
				// System.out.println("trovato ");
				findArea = new Rectangle(pos, i, findWhat.length(), 1);
				cursorAtIndex(pos + 1, i);
				setSelection(findArea);
				findFromColumn = pos + direction;
				// System.out.println("truva' "+findFromColumn);
				return true;
			}
			findFromColumn = (forward ? 0 : model.size() - 1);
		}

		/**
		 * non e' nella parte visibile cerco nel file
		 */
		System.out.println("cerco nel file da riga=" + findFromIndex + " col=" + findFromColumn);
		Record start = (Record) (forward ? model.getLast() : model.getFirst());
		try {
			df.seek(start.getKey(), (forward ? DataFile.GREATER : DataFile.GTEQ));
			boolean loop = true;
			while (loop) {
				try {
					Record r = (forward ? df.readNext() : df.readPrevious());
					// System.out.println("letto "+r);
					if (findIgnoreCase) {
						ba = r.toUpperCase();
					} else {
						ba = (ByteArray) r;
					}
					int pos = ba.indexOf(findWhat, 0);
					if (posOnFindRange(pos)) {
						// System.out.println("trovato ");
						seek(r, false);
						findArea = new Rectangle(pos, 0, findWhat.length(), 1);
						view.cursorOnColumn(pos + 1);
						setSelection(findArea);
						findFromIndex = 1;
						findFromColumn = pos + 1;
						refreshStatusBar();
						return true;
					}
				} catch (EOFException eof) {
					loop = false;
				}
			}
		} catch (Exception e) {
			SmartDialog.errorBox(parent, e);
			e.printStackTrace();
		}
		System.out.println("NON TROVATO");
		getToolkit().beep();
		if (SmartDialog.confirmBox(parent, messages.getString("msg.find.not.found"))) {
			System.out.println("relocate");
			locateTop();
			return findOccurrence(forward);
		}
		return false;
	}

	public boolean posOnFindRange(int pos) {
		if (pos >= 0) {
			if (findOnRange) {
				if (pos >= findRangeFrom && pos <= findRangeTo) {
					return true;
				}
			} else {
				return true;
			}
		}
		return false;
	}

	public void setHexMode(boolean b) {
		view.setHexMode(b);
	}

	public void setShowRecordNumber(boolean b) {
		view.setShowRecordNumber(b);
	}

	public void setShowCrossBeam(boolean b) {
		view.setShowCrossBeam(b);
	}

	public void setConversionMode(int m) {
		conversionMode = m;
		switch (conversionMode) {
		/*
		 * case FileEditorPane.CONVERSION_ASC2EBC:
		 * outputConverter=converterAscii2Ebcdic;
		 * inputConverter=converterAscii2Ebcdic; break;
		 */
		case FileEditorPane.CONVERSION_EBCDIC:
			outputConverter = converterEbcdic2Ascii;
			inputConverter = converterAscii2Ebcdic;
			break;
		case FileEditorPane.CONVERSION_NONE:
		default:
			outputConverter = converterAsis;
			inputConverter = converterAsis;
		}
		repaint();
	}

	public int getConversionMode() {
		return conversionMode;
	}

	public ByteConverter getOutputConverter() {
		return outputConverter;
	}

	public boolean isOpened() {
		return (df != null && df.isOpened());
	}

	public boolean isFlat() {
		return (df instanceof FlatFile);
	}

	public void toggleInsertMode() {
		insertMode = !insertMode;
		view.refreshCursor();
	}

	public boolean getInsertMode() {
		return insertMode;
	}

	public Rectangle getSelection() {
		return selection;
	}

	public boolean hasSelection() {
		return (selection != null);
	}

	public void resetSelection() {
		setSelection(null);
	}

	public void setSelection(Rectangle r) {
		selection = r;
		((FileEdit) parent).setMenuState();
		repaint();
	}

	public void copySelection() {
		if (hasSelection()) {
			Record r = (Record) model.get((int) selection.getY());
			String s = r.getString((int) selection.getX(), (int) selection.getWidth());
			try {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection data = new StringSelection(s);
				clipboard.setContents(data, data);
				// Display.resetSelectedText();
				// resetSelection();
			} catch (AccessControlException e) {
				SmartDialog.errorBox(parent, e);
			}
		}
	}

	public void pasteClipboardContents() {
		try {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			Transferable clipData = clipboard.getContents(clipboard);
			String s = null;
			s = (String) (clipData.getTransferData(DataFlavor.stringFlavor));
			if (s != null) {
				byte[] v = s.getBytes();
				for (int i = 0; i < v.length; i++) {
					putByte(v[i]);
				}
				// Record r=(Record)model.get(view.getCurrentIndex());
				// r.setString(s,view.getCursorX());
			}
		} catch (Exception e) {
			SmartDialog.errorBox(parent, e);
		}

	}

	public void cutSelection() {
		if (hasSelection()) {
			Record r = (Record) model.get((int) selection.getY());
			int b = (int) selection.getX();
			int w = (int) selection.getWidth();
			cursorAtIndex(b + 1, (int) selection.getY());
			for (int i = 0; i < w; i++) {
				if (insertMode) {
					deleteChar();
				} else {
					putByte((byte) 0);
				}
			}
		}
	}

	/**
	 * interfaccia KeyListener
	 */
	public void keyPressed(KeyEvent evt) {
		// System.out.println("KP "+evt);
		switch (evt.getKeyCode()) {
		case KeyEvent.VK_LEFT:
			cursorLeft();
			break;
		case KeyEvent.VK_RIGHT:
			cursorRight();
			break;

		case KeyEvent.VK_DOWN:
			cursorDown();
			break;
		case KeyEvent.VK_UP:
			cursorUp();
			break;
		case KeyEvent.VK_PAGE_UP:
			pageUp();
			break;
		case KeyEvent.VK_PAGE_DOWN:
			pageDown();
			break;
		case KeyEvent.VK_HOME:
			cursorBeginOfLine();
			break;
		case KeyEvent.VK_END:
			cursorEndOfLine();
			break;

		case KeyEvent.VK_ENTER:
			cursorNextLine();
			break;

		case KeyEvent.VK_BACK_SPACE:
			backSpace();
			break;
		case KeyEvent.VK_DELETE:
			deleteChar();
			break;
		case KeyEvent.VK_INSERT:
			toggleInsertMode();
			break;
		default:
			break;
		}
	}

	public void keyTyped(KeyEvent evt) {
		// System.out.println("KT "+evt);
		if (df == null || df.isReadOnly()) {
			return;
		}
		byte b;
		char c = evt.getKeyChar();
		// System.out.println("-->"+(int)c);
		switch (c) {
		case KeyEvent.VK_ENTER:
		case KeyEvent.VK_BACK_SPACE:
			return;
		default:
		}
		int ea = view.getEditArea();
		if (ea == EditorView.AREA_CHAR) {
			b = (byte) c;
			b = inputConverter.convert(b);
		} else {
			if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'f')) {
				getToolkit().beep();
				return;
			}
			int d = Character.digit(c, 16);
			int o = (int) model.getByte(view.getCursorX() - 1, view.getCurrentIndex());
			if (ea == EditorView.AREA_HEX1) {
				b = (byte) ((d << 4) | (o & 0x0F));
			} else {
				b = (byte) (d | (o & 0xF0));
			}
		}
		putByte(b);
	}

	public void keyReleased(KeyEvent evt) {
	}

	/**
	 * interfaccia MouseListener
	 */
	public void mouseClicked(MouseEvent evt) {
		int m = evt.getModifiers();
		if (evt.getClickCount() == 1) {
			selection = null;
			((FileEdit) parent).setMenuState();
			selecting = false;
			int x = (evt.getX()) / view.getCharWidth();
			int y = (evt.getY()) / view.getCharHeight();
			if (view.getShowRecordNumber()) {
				x -= 9;
			}
			cursorAt(x, y + 1);
		}
	}

	public void mouseReleased(MouseEvent evt) {
		if (selection == null)
			return;
		int w = ((evt.getX()) / view.getCharWidth()) - (int) selection.getX() + 1;
		int h = ((evt.getY()) / view.getCharHeight()) - (int) selection.getY() + 1;
		if (view.getShowRecordNumber()) {
			w -= 9;
		}
		selection.setSize(w, 1);
		if (selection.getWidth() > 0 && selection.getHeight() > 0) {
			repaint();
		} else {
			selection = null;
		}
		((FileEdit) parent).setMenuState();
		selecting = false;
	}

	public void mouseEntered(MouseEvent evt) {
	}

	public void mouseExited(MouseEvent evt) {
	}

	public void mousePressed(MouseEvent evt) {
	}

	/**
	 * interfaccia MouseMotionListener
	 */
	public void mouseDragged(MouseEvent evt) {
		/*
		 * if(selectOnRightButton) { if (!
		 * SwingUtilities.isRightMouseButton(evt)) { //TRACE.LOG("non e' button
		 * 3"); //this_mouseClicked(evt); return; } }
		 */
		int nx = (evt.getX()) / view.getCharWidth();
		int ny = (evt.getY()) / view.getCharHeight();
		if (view.getShowRecordNumber()) {
			nx -= 9;
		}
		if (view.isHexMode()) {
			ny = ny / 3;
		}
		if (selection == null || selecting == false) {
			// inizio la selezione
			selection = new Rectangle();
			selection.setSize(1, 1);
			selection.setLocation(nx, ny);
			selecting = true;
		} else {
			// if(selection==null) return;
			int w = 1;
			int h = 1;
			if (nx != selection.getX()) {
				if (nx > selection.getX()) {
					w = (int) nx - (int) selection.getX() + 1;
				} else {
					w = (int) selection.getWidth() + (int) (selection.getX() - nx - 1);
					selection.setLocation((int) nx + 1, (int) selection.getY());
				}
			}
			if (ny != selection.getY()) {
				if (ny > selection.getY()) {
					h = ny - (int) selection.getY() + 1;
				} else {
					h = (int) selection.getHeight() + (int) (selection.getY() - ny - 1);
					selection.setLocation((int) selection.getX(), (int) ny + 1);
				}

			}
			// selection.setSize (w,h);
			selection.setSize(w, 1);
			if (selection.getWidth() > 0 && selection.getHeight() > 0) {
				repaint();
			}
		}
		((FileEdit) parent).setMenuState();
	}

	public void mouseMoved(MouseEvent evt) {
	}

	/**
	 * interfaccia MouseWheelListener
	 */
	public void mouseWheelMoved(MouseWheelEvent evt) {
		System.out.println(evt.toString());
		if (df != null && df.isOpened()) {
			if (evt.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
				scroll(evt.getScrollAmount(), (evt.getWheelRotation() < 0));
			}
		}

	}

	ReplaceDialog rd = null;

	public int getReplaceMode() {
		if (rd == null) {
			rd = new ReplaceDialog();
		}
		rd.setVisible(true);
		return rd.getMode();
	}

	class ReplaceDialog extends JDialog implements ActionListener {
		int mode;

		public int getMode() {
			return mode;
		}

		public ReplaceDialog() {
			super((Frame) null, true);
			JPanel p = (JPanel) this.getContentPane();
			p.setLayout(new GridLayout(1, 4));
			JButton b;
			b = new JButton(messages.getString("buttons.replace"));
			b.addActionListener(this);
			b.setActionCommand("replace");
			p.add(b);
			b = new JButton(messages.getString("buttons.skip"));
			b.addActionListener(this);
			b.setActionCommand("skip");
			p.add(b);
			b = new JButton(messages.getString("buttons.replace.all"));
			b.addActionListener(this);
			b.setActionCommand("all");
			p.add(b);
			b = new JButton(messages.getString("buttons.cancel"));
			b.addActionListener(this);
			b.setActionCommand("cancel");
			p.add(b);
			pack();
		}

		public final static int MODE_REPLACE = 0;
		public final static int MODE_REPLACE_ALL = 1;
		public final static int MODE_SKIP = 2;
		public final static int MODE_CANCEL = 3;

		public void actionPerformed(ActionEvent e) {
			if (e.getActionCommand().equals("replace")) {
				this.mode = MODE_REPLACE;
			}
			if (e.getActionCommand().equals("all")) {
				this.mode = MODE_REPLACE_ALL;
			}
			if (e.getActionCommand().equals("cancel")) {
				this.mode = MODE_CANCEL;
			}
			if (e.getActionCommand().equals("skip")) {
				this.mode = MODE_SKIP;
			}
			this.dispose();
		}
	}
}
