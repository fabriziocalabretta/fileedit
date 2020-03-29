package org.fc.edit;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.text.DecimalFormat;
import java.util.logging.Logger;

import org.fc.edit.FileEditorPane;
import org.fc.io.KeyInfo;
import org.fc.utils.ByteConverter;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class EditorView extends Pane implements EditorModelListener {
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
	
	Logger logger = Logger.getLogger(this.getClass().getName());
	
	public EditorView(EditorModel m, FileEditorPane p)
	{
		super();
		model = m;
		controller = p;
		keyInfo = null;
		model.addListener(this);
		initWidgets();
		setShowRecordNumber(false);
		recalculateSize();
	}
	
	private void initWidgets()
	{
		this.getChildren().add(new Label("EDITOR"));
	}
	
	public void setKeyInfo(KeyInfo ki) {
		keyInfo = ki;
	}

	ByteConverter converter;
	
	void recalculateSize() {
		logger.severe("IMPLEMET recalculateSize");
	}
	
	@Override
	public void dataChanged() {
		logger.severe("IMPLEMET datachnaged");
	}

	@Override
	public void sizeChanged() {
		logger.severe("IMPLEMET datachnaged");
		
	}
	void refreshCursor()
	{
		logger.severe("IMPLEMET refreshcursor");
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
	
	///
	/// Cursor Movement
	///

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
	
	public void repaint()
	{
		logger.severe("implementare repaint");
	}
}
