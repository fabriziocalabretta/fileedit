package org.fc.edit.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.text.DecimalFormat;

import javax.swing.JPanel;

import org.fc.edit.EditorModel;
import org.fc.edit.EditorModelListener;
import org.fc.io.IsamKeyPart;
import org.fc.io.KeyInfo;
import org.fc.io.Record;
import org.fc.utils.ByteConverter;

public class EditorView extends JPanel implements EditorModelListener {
	public final static int AREA_CHAR = 0;
	public final static int AREA_HEX1 = 1;
	public final static int AREA_HEX2 = 2;

	static DecimalFormat numFormat = new DecimalFormat("00000000 ");

	EditorModel model;
	FileEditorPane controller;

	Font normalFont;

	/*
	 * Color backgroundColor=Color.white; Color infoColor=Color.gray; Color
	 * dataColor=Color.black; Color hexColor=Color.blue; Color
	 * keyColor=Color.green;
	 */
	Color backgroundColor = Color.black;
	Color infoColor = Color.gray;
	Color dataColor = new Color(128, 128, 255);
	Color hexColor = Color.red;
	Color keyColor = Color.green;

	char[] paintBuffer;
	char[] paintBufferHex1;
	char[] paintBufferHex2;

	KeyInfo keyInfo;

	// Dimensioni in pixel
	int charWidth;
	int charHeight;
	int charDescent;
	int viewWidth;
	int viewHeight;

	// Dimensioni in caratteri
	int showWidth = 0;
	int showFrom = 0;
	int showTo = 0;
	int showHeight;
	int cursorX = 1;
	int cursorY = 1;
	int lastCursorX = 0;
	int lastCursorY = 0;

	int infoCharNumber;

	boolean showRecordNumber = false;
	boolean crossBeamCursor = false;
	int hexMode = 1;

	public EditorView(EditorModel m, FileEditorPane p) {
		super();
		model = m;
		controller = p;
		keyInfo = null;
		model.addListener(this);
		initWidgets();
		setShowRecordNumber(false);
		recalculateSize();

	}

	void initWidgets() {
		this.setOpaque(true);
		this.setEnabled(true);
		this.setDoubleBuffered(true);
		this.setBackground(backgroundColor);
		// this.setForeground(foreground);
		normalFont = new Font("monospaced", Font.PLAIN, 14);
		this.setFont(normalFont);
		FontMetrics fm = getFontMetrics(normalFont);
		if (fm != null) {
			charWidth = fm.charWidth('@');
			charHeight = fm.getHeight();
			charDescent = fm.getDescent();
		}
	}

	public void setKeyInfo(KeyInfo ki) {
		keyInfo = ki;
		// System.out.println("SETKEYINFO "+ki);
	}

	ByteConverter converter;

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if (model == null) {
			return;
		}
		converter = controller.getOutputConverter();
		setFont(normalFont);
		for (int i = 0, i2 = 0; i < model.size(); i++) {
			Record r = (Record) model.get(i);
			paintRecord(g, r, i2);
			i2 += hexMode;
		}
		drawCursor(g, cursorX, cursorY, backgroundColor);
		if (controller.getSelection() != null) {
			// System.out.println("Selezione "+selection);
			g.setXORMode(Color.magenta);
			g.fillRect((int) (infoCharNumber + controller.getSelection().getX()) * charWidth, (int) controller.getSelection()
					.getY()
					* (isHexMode() ? 3 : 1) * charHeight, (int) controller.getSelection().getWidth() * charWidth, (int) controller
					.getSelection().getHeight()
					* charHeight);
		}
	}

	public final static int ZERO_X = 2;

	public void paintRecord(Graphics g, Record r, int y) {
		int yh = y * charHeight;

		if (showRecordNumber) {
			String num = numFormat.format(r.getRecordNumber());

			g.setColor(backgroundColor);
			g.fillRect(ZERO_X, yh, charWidth * (r.length() + num.length()), charHeight);

			g.setColor(infoColor);
			g.drawString(num, ZERO_X, (y + 1) * charHeight - charDescent);
		}
		byte[] b = r.getBytes();
		for (int i = 0; i < r.length(); i++) {
			char c = (char) converter.convert(b[i]);
			// if (b[i]>31 && b[i]<129) {
			// paintBuffer[i]=(char)b[i];
			// } else {
			if (c == 128) {
				c = '€';
			} else {
				if (c < 32 || c > 128) {
					c = 183;
				}
			}
			paintBuffer[i] = c;
		}
		if (isHexMode()) {
			for (int i = 0; i < r.length(); i++) {
				byte h = b[i];
				paintBufferHex1[i] = (char) Character.forDigit((h & 0xF0) >> 4, 16);
				paintBufferHex2[i] = (char) Character.forDigit(h & 0x0F, 16);
			}
		}
		if (keyInfo != null) {
			int pos = showFrom;
			int kpn = 0;
			while (pos < showWidth) {
				int start, len;
				if (kpn < keyInfo.getPartNumber()) {
					IsamKeyPart kp = keyInfo.getKeyPart(kpn);
					if (kp.getOffset() > pos) {
						start = pos;
						len = kp.getOffset() - pos;
						g.setColor(dataColor);
					} else {
						start = kp.getOffset();
						len = kp.getLength();
						g.setColor(keyColor);
						kpn++;
					}
				} else {
					start = pos;
					len = r.length() - pos;
					g.setColor(dataColor);
				}
				// System.out.println("mando "+start+", "+len);
				g.drawChars(paintBuffer, start, len, ZERO_X + charWidth * (infoCharNumber + start), (y + 1) * charHeight
						- charDescent);
				pos += len;
				// System.out.println("pos="+pos);
			}
		} else {
			g.setColor(dataColor);
			// g.drawChars(paintBuffer,0,r.length(),charWidth*infoCharNumber,(y+1)*charHeight-charDescent);
			// System.out.println("stampo da "+showFrom+" PER "+showWidth);
			g.drawChars(paintBuffer, showFrom, showWidth, ZERO_X + charWidth * infoCharNumber, (y + 1) * charHeight - charDescent);
		}
		if (isHexMode()) {
			g.setColor(hexColor);
			g.drawChars(paintBufferHex1, showFrom, showWidth, ZERO_X + charWidth * infoCharNumber, (y + 2) * charHeight
					- charDescent);
			g.drawChars(paintBufferHex2, showFrom, showWidth, ZERO_X + charWidth * infoCharNumber, (y + 3) * charHeight
					- charDescent);
		}
		if ((showFrom + showWidth) >= r.length()) {
			g.setColor(infoColor);
			int lx = charWidth * (infoCharNumber + showWidth + 1);
			int ly1 = (y) * charHeight - charDescent;
			int ly2 = (y + hexMode) * charHeight - charDescent;
			g.drawLine(lx, ly1, lx, ly2);
		}
	}

	public void refreshCursor() {
		// selection=null;
		controller.resetSelection();
		repaint();
	}

	public void drawCursor(Graphics g, int x, int y, Color bg) {
		g.setColor(Color.white);
		g.setXORMode(bg);
		x -= showFrom;
		x--;
		int xw = (x + infoCharNumber) * charWidth;
		g.fillRect(ZERO_X + xw, (y - 1) * charHeight, (controller.getInsertMode() ? 2 : charWidth), charHeight);
		if (crossBeamCursor) {
			// Rectangle r=getBounds();
			int yh = y * charHeight;
			g.drawLine(ZERO_X, yh, viewWidth, yh);
			g.drawLine(ZERO_X + xw, 0, xw, viewHeight);
		}
		g.setPaintMode();
	}

	public void dataChanged() {
		repaint(0, 0, viewWidth, viewHeight);
	}

	public void sizeChanged() {
		// System.out.println("Size changed ------");
		// System.out.println("lastidx="+lastIndex);
		recalculateSize();
		int l = (lastIndex > 0 ? lastIndex : getCurrentIndex());
		if (l > model.getMaxRecords()) {
			// System.out.println("riposiziono a "+(model.getMaxRecords()-1));
			cursorOnIndex(model.getMaxRecords() - 1);
		} else {
			cursorOnIndex(l);
		}
		lastIndex = -1;
		repaint();
	}

	Rectangle lastBounds = null;

	public void setBounds(int x, int y, int w, int h) {
		super.setBounds(x, y, w, h);
		// System.out.println("SETBOUNDS");
		Rectangle r = getBounds();
		if (lastBounds == null || !lastBounds.equals(r)) {
			lastBounds = r;
			recalculateSize();
		}
		controller.viewCapacityChanged(showHeight);
	}

	public Dimension getPreferredSize() {
		return (getSize());
	}

	public Dimension getSize() {
		return new Dimension(model.getMaxRecordLength() * charWidth, model.size() * charHeight);
	}

	Dimension getCharCapacity(Rectangle r) {
		int h = ((int) r.getHeight()) / (charHeight * hexMode);
		int w = ((int) r.getWidth() / charWidth);
		return new Dimension(w, h);
	}

	void recalculateSize() {
		infoCharNumber = (showRecordNumber ? 9 : 0);
		Rectangle r = getBounds();
		viewWidth = (int) r.getWidth();
		viewHeight = (int) r.getHeight();
		Dimension d = getCharCapacity(r);
		// System.out.println("Dimension="+d);
		showWidth = (int) d.getWidth() - infoCharNumber;
		if (showWidth > model.getMaxRecordLength()) {
			showFrom = 0;
			showWidth = model.getMaxRecordLength();
		}
		showTo = getShowTo();
		showHeight = (int) d.getHeight();
		// System.out.println("l'editor puo' mostrare max "+showWidth);
		// System.out.println("da "+showFrom+" a "+showTo);
		paintBuffer = new char[model.getMaxRecordLength()];
		paintBufferHex1 = new char[model.getMaxRecordLength()];
		paintBufferHex2 = new char[model.getMaxRecordLength()];
	}

	int getShowTo() {
		int t = showFrom + showWidth;
		if (t > model.getMaxRecordLength()) {
			t = model.getMaxRecordLength();
		}
		return t;
	}

	public boolean isOnLastRecord() {
		return (getCurrentIndex() == (model.size() - 1));
	}

	public boolean isOnLastLine() {
		if (isOnLastRecord()) {
			if (isHexMode()) {
				return (getEditArea() == AREA_HEX2);
			} else {
				return true;
			}
		}
		return false;
	}

	public void cursorOnIndex(int n) {
		cursorY = (n * hexMode) + 1;
		refreshCursor();
	}

	public void cursorOnColumn(int x) {
		if (x > model.getMaxRecordLength()) {
			return;
		}
		cursorX = x;
		if (cursorX <= showFrom) {
			showFrom = 0;
			showTo = getShowTo();
		}
		if (cursorX > showTo) {
			showFrom++;
			showTo = showFrom + showWidth;
		}
		refreshCursor();
	}

	public void cursorOn(int x, int y) {
		cursorY = y;
		cursorOnColumn(x);
	}

	public boolean cursorNextLine() {
		if (isOnLastRecord()) {
			return false;
		}
		if (isHexMode()) {
			cursorY += 3 - getEditArea();
		} else {
			cursorY++;
		}
		cursorX = 1;
		if (cursorX <= showFrom) {
			showFrom = 0;
			showTo = getShowTo();
		}
		refreshCursor();
		return true;
	}

	public boolean cursorDown() {
		if (isOnLastLine()) {
			return false;
		}
		cursorY++;
		refreshCursor();
		return true;
	}

	public boolean cursorUp() {
		if (cursorY == 1) {
			return false;
		}
		cursorY--;
		refreshCursor();
		return true;
	}

	public boolean cursorLeft() {
		if (cursorX == 1) {
			return true;
		}
		cursorX--;
		if (cursorX <= showFrom) {
			showFrom--;
			showTo = getShowTo();
		}
		refreshCursor();
		return true;
	}

	public boolean cursorRight() {
		if (cursorX == model.getMaxRecordLength()) {
			return true;
		}
		cursorX++;
		if (cursorX > showTo) {
			showFrom++;
			showTo = showFrom + showWidth;
		}

		refreshCursor();
		return true;
	}

	boolean checkIfCursorIsOutView(boolean left) {
		if (left) {
			if (cursorX <= showFrom) {
				showFrom--;
				showTo = getShowTo();
				// System.out.println("sforato a sx "+cursorX);
				return true;
			}
		} else {
			if (cursorX > showTo) {
				showFrom++;
				showTo = showFrom + showWidth;
				// System.out.println("sforato a dx "+cursorX);
				return true;
			}
		}
		return false;
	}

	public void cursorHome() {
		cursorY = 1;
		cursorX = 1;
		refreshCursor();
	}

	public void cursorBottom() {
		cursorX = 1;
		cursorY = model.getMaxRecords() * hexMode;
		refreshCursor();
	}

	public void cursorBeginOfLine() {
		cursorX = 1;
		showFrom = 0;
		showTo = getShowTo();
		refreshCursor();
	}

	public void cursorEndOfLine() {
		cursorX = model.getMaxRecordLength();
		showFrom = model.getMaxRecordLength() - showWidth;
		refreshCursor();
	}

	public int getCursorX() {
		return cursorX;
	}

	public int getCursorY() {
		return cursorY;
	}

	public int getCurrentIndex() {
		return ((cursorY - 1) / hexMode);
	}

	public int getCharWidth() {
		return charWidth;
	}

	public int getCharHeight() {
		return charHeight;
	}

	public int getEditArea() {
		if (isHexMode()) {
			int m = (cursorY - 1) - (getCurrentIndex() * hexMode);
			return m;
		}
		return AREA_CHAR;
	}

	public boolean isHexMode() {
		return (hexMode == 3);
	}

	int lastIndex;

	public void setHexMode(boolean b) {
		lastIndex = getCurrentIndex();
		hexMode = (b ? 3 : 1);
		sizeChanged();
		controller.viewCapacityChanged(showHeight);
	}

	public void setShowRecordNumber(boolean b) {
		showRecordNumber = b;
		sizeChanged();
	}

	public boolean getShowRecordNumber() {
		return showRecordNumber;
	}

	public void setShowCrossBeam(boolean b) {
		crossBeamCursor = b;
		repaint();
	}

}
