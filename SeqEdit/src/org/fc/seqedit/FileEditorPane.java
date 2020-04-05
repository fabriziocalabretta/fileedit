package org.fc.seqedit;

import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseWheelEvent;
//import java.awt.event.KeyEvent;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import org.fc.hdm.ByteArray;
import org.fc.io.DataFile;
import org.fc.io.FlatFile;
import org.fc.io.FlatRecord;
import org.fc.io.Record;
import org.fc.io.RrdsKey;
import org.fc.io.VariableFlatFile;
import org.fc.utils.Ascii2EbcdicConverter;
import org.fc.utils.ByteConverter;
import org.fc.utils.Ebcdic2AsciiConverter;
import org.fc.utils.SimpleConverter;

import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class FileEditorPane extends BorderPane {
	public final static int MODE_REPLACE = 0;
	public final static int MODE_REPLACE_ALL = 1;
	public final static int MODE_SKIP = 2;
	public final static int MODE_CANCEL = 3;

	EditorModel model;
	EditorView view;
	DataFile df = null;
	HBox statusBar;
	Label lMsg;
	Label lRo;
	Label lOffset;
	Label lInsert;
	ProgressBar progressBar;
	ResourceBundle messages;
	// Record lastRecordRead=null;
	Application parent;
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

	int conversionMode = FileEditorPane.CONVERSION_NONE;
	boolean insertMode;

	Rectangle selection = null;
	boolean selecting = false;

	ByteArray findWhat = null;
	ByteArray replaceWith = null;
	boolean findIgnoreCase;
	boolean findOnRange;
	int findRangeFrom;
	int findRangeTo;
	Rectangle findArea = null;

	Logger logger = Logger.getLogger(this.getClass().getName());

	public FileEditorPane(Application p, ResourceBundle m) {
		messages = m;
		parent = p;
		insertMode = false;
		initWidgets();
		converterAscii2Ebcdic = new Ascii2EbcdicConverter();
		converterEbcdic2Ascii = new Ebcdic2AsciiConverter();
		converterAsis = new SimpleConverter();
		outputConverter = converterAsis;
		inputConverter = converterAsis;
		refreshStatusBar();
	}

	private void initWidgets() {
		// this.setBorder(BorderFactory.createEtchedBorder());
		model = new EditorModel(40, 20);
		view = new EditorView(model, this);
		SwingNode swingNode = new SwingNode();
		swingNode.setDisable(true);		// allows to keep focus always on the JavaFX side
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				swingNode.setContent(view);
			}
		});

		BorderPane pview = new BorderPane();
		pview.setCenter(swingNode);
		// pview.setBorder(BorderFactory.createLoweredBevelBorder());
		statusBar = new HBox();
		// statusBar.setBorder(BorderFactory.createEtchedBorder());
		lOffset = new Label("off 0");
		// lOffset.setFont(new Font("monospaced", Font.PLAIN, 12));
		// lOffset.setBorder(BorderFactory.createLoweredBevelBorder());
		lRo = new Label();
		// lRo.setBorder(BorderFactory.createLoweredBevelBorder());
		lInsert = new Label();
		// lInsert.setBorder(BorderFactory.createLoweredBevelBorder());

		lMsg = new Label();

		statusBar.getChildren().add(lMsg);
		statusBar.getChildren().add(lRo);
		statusBar.getChildren().add(lOffset);
		statusBar.getChildren().add(lInsert);

		this.setCenter(pview);
		this.setBottom(statusBar);

		setEventHandlers();

	}


	public void open(File file, int reclen, boolean readOnly, boolean vl, boolean vlle) throws IOException {
		if (vl) {
			df = new VariableFlatFile(file, reclen, vlle);
		} else {
			df = new FlatFile(file, reclen);
		}
		// TODO progress monitor
//		ProgressMonitor progressMonitor = new ProgressMonitor(parent, messages.getString("progress.open") + " " + file + " ...", "", 0, 100);
//		progressMonitor.setProgress(0);
//		progressMonitor.setMillisToDecideToPopup(2000);
//		df.setProgressMonitor(progressMonitor);
		open(readOnly);
		// progressMonitor.close();
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
				beep();
				// refresh();
			} else {
				System.out.println("********** EOF *************");
				beep();
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
				beep();
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
			beep();
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
				FXDialog.errorBox(parent, e);
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
				FXDialog.errorBox(parent, e);
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
			 * System.out.println("i="+i); System.out.println("max="+model.size());
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
			FXDialog.errorBox(parent, e);
			e.printStackTrace();
		}
		System.out.println("NON TROVATO");
		beep();
		if (FXDialog.confirmBox(parent, messages.getString("msg.find.not.found"))) {
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
		 * outputConverter=converterAscii2Ebcdic; inputConverter=converterAscii2Ebcdic;
		 * break;
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

	public void repaint() {
		if (view != null) view.repaint();
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
		((Main) parent).setMenuState();
		repaint();
	}

	public void copySelection() {
		if (hasSelection()) {
			Record r = (Record) model.get((int) selection.getY());
			String s = r.getString((int) selection.getX(), (int) selection.getWidth());
			try {
				Clipboard clipboard = Clipboard.getSystemClipboard();
				ClipboardContent content = new ClipboardContent();
				clipboard.setContent(content);
				// Display.resetSelectedText();
				// resetSelection();
			} catch (AccessControlException e) {
				FXDialog.errorBox(parent, e);
			}
		}
	}

	public void pasteClipboardContents() {
		try {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			if (!clipboard.hasString())
				return;
			String s = (String) clipboard.getContent(DataFormat.PLAIN_TEXT);
			if (s != null) {
				byte[] v = s.getBytes();
				for (int i = 0; i < v.length; i++) {
					putByte(v[i]);
				}
				// Record r=(Record)model.get(view.getCurrentIndex());
				// r.setString(s,view.getCursorX());
			}
		} catch (Exception e) {
			FXDialog.errorBox(parent, e);
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

	public int getReplaceMode() {

		Alert dialog = new Alert(AlertType.CONFIRMATION);
		dialog.setTitle(messages.getString("dialog.replace.mode.title"));
		dialog.setHeaderText(messages.getString("dialog.replace.mode.header"));

		ButtonType replace = new ButtonType(messages.getString("buttons.replace"));
		ButtonType skip = new ButtonType(messages.getString("buttons.skip"));
		ButtonType replaceAll = new ButtonType(messages.getString("buttons.replace.all"));
		ButtonType cancel = new ButtonType(messages.getString("buttons.cancel"), ButtonData.CANCEL_CLOSE);

		dialog.getButtonTypes().setAll(replace, skip, replaceAll, cancel);
		Optional<ButtonType> result = dialog.showAndWait();

		if (result.get() == replace) {
			return MODE_REPLACE;
		} else if (result.get() == skip) {
			return MODE_SKIP;
		} else if (result.get() == replaceAll) {
			return MODE_REPLACE_ALL;
		} else {
			return MODE_CANCEL;
		}
	}

	private void beep() {
		Toolkit.getDefaultToolkit().beep();
	}

	private void setEventHandlers() {
		this.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				logger.info("pressed" +event);
				switch (event.getCode()) {
				case LEFT:
					cursorLeft();
					break;
				case RIGHT:
					cursorRight();
					break;
				case DOWN:
					cursorDown();
					break;
				case UP:
					cursorUp();
					break;
				case PAGE_UP:
					pageUp();
					break;
				case PAGE_DOWN:
					pageDown();
					break;
				case HOME:
					cursorBeginOfLine();
					break;
				case END:
					cursorEndOfLine();
					break;
				case ENTER:
					cursorNextLine();
					break;
				case BACK_SPACE:
					backSpace();
					break;
				case DELETE:
					deleteChar();
					break;
				case INSERT:
					toggleInsertMode();
					break;
				default:
					return;
				}
				event.consume();
			}
		});
		this.setOnKeyTyped(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent evt) {
				logger.info("typed "+ evt);
				if (df == null || df.isReadOnly()) {
					return;
				}
				byte b;
				
				switch (evt.getCode()) {
				case ENTER:
				case BACK_SPACE:
					logger.info("typed ENTER or BSPC");
					return;
				default:
				}

				char c = evt.getCharacter().charAt(0);
				if (c == '\n' || c == '\r')
				{
					return;
				}
				// System.out.println("-->"+(int)c);
				int ea = view.getEditArea();
				if (ea == EditorView.AREA_CHAR) {
					b = (byte) c;
					b = inputConverter.convert(b);
				} else {
					if ((c < '0' || c > '9') && (c < 'a' || c > 'f') && (c < 'A' || c > 'f')) {
						beep();
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
		});
		
		this.setOnMousePressed(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent evt) {
				//int m = evt.getModifiers();
				logger.info("mouse click "+evt);
				logger.info("click count "+evt.getClickCount());
				logger.info("still "+evt.isStillSincePress());
				if (evt.getClickCount() == 1 && evt.isStillSincePress()) {
					selection = null;
					((Main) parent).setMenuState();
					selecting = false;
					
					int x = ((int)evt.getX()) / view.getCharWidth();
					int y = ((int)evt.getY()) / view.getCharHeight();
					if (view.getShowRecordNumber()) {
						x -= 9;
					}
					cursorAt(x, y + 1);
				}
			}
		});
		
		this.setOnMouseReleased(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent evt) {
				logger.info(getClass().getName()+"+mouse release "+evt);
				if (selection == null)
					return;
				int w = ((int)(evt.getX()) / view.getCharWidth()) - (int) selection.getX() + 1;
				int h = ((int)(evt.getY()) / view.getCharHeight()) - (int) selection.getY() + 1;
				if (view.getShowRecordNumber()) {
					w -= 9;
				}
				selection.setSize(w, 1);
				if (selection.getWidth() > 0 && selection.getHeight() > 0) {
					repaint();
				} else {
					selection = null;
				}
				((Main) parent).setMenuState();
				selecting = false;
			}
		});
		
		this.setOnMouseDragged(new EventHandler<MouseEvent>() {
			// TODO: fai funzionare la selezione
			@Override
			public void handle(MouseEvent evt) {
				logger.info(getClass().getName()+"+mouse drag "+evt);
				int nx = ((int)evt.getX()) / view.getCharWidth();
				int ny = ((int)evt.getY()) / view.getCharHeight();
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
				((Main) parent).setMenuState();
			}
		});
		
		this.setOnScroll(new EventHandler<ScrollEvent>() {
			// TODO: fai funzionare la selezione
			@Override
			public void handle(ScrollEvent evt) {
				logger.info(getClass().getName()+"+scroll "+evt);
				if (df != null && df.isOpened()) {
					int l=0;
					switch(evt.getTextDeltaYUnits()) {
			        case LINES:
			        	l=(int)evt.getTextDeltaY(); 
			            break;
			        case PAGES:
			            // scroll about event.getTextDeltaY() pages
			        	//logger.info("pages");
			            break;
			        case NONE:
			            // scroll about event.getDeltaY() pixels
			        	//logger.info("non");
			            break;
					}
					if (l!=0)
					{
						logger.info("scroll "+l);
						scroll(Math.abs(l), l>0);
					}
			    }
			}
		});
	}
}
