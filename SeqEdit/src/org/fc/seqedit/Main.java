/**
 * http://tutorials.jenkov.com/javafx/index.html
 */

package org.fc.seqedit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ToolBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
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

	MenuBar menuBar;
	Menu mFile;
	MenuItem miOpenFlat;
	MenuItem miSave;
	MenuItem miSaveAs;
	MenuItem miClose;
	MenuItem miExit;

	Menu mSearch;
	MenuItem miPageDown;
	MenuItem miPageUp;
	MenuItem miSearchTop;
	MenuItem miSearchBottom;
	MenuItem miFind;
	MenuItem miFindPrev;
	MenuItem miFindNext;
	MenuItem miReplace;

	Menu mEdit;
	MenuItem miCut;
	MenuItem miCopy;
	MenuItem miPaste;
	MenuItem miInsertAbove;
	MenuItem miInsertBelow;
	MenuItem miDelete;
	MenuItem miDeleteMultiple;
	Menu mView;
	RadioMenuItem mrbViewAscii;
	RadioMenuItem mrbViewEbcdic;

	Menu mHelp;
	MenuItem miHelpContents;
	MenuItem miHelpAbout;

	Button btnFileOpenFlat;
	Button btnFileSave;
	Button btnFileSaveAs;
	Button btnEditCut;
	Button btnEditCopy;
	Button btnEditPaste;
	Button btnSearchFind;
	Button btnSearchFindPrev;
	Button btnSearchFindNext;
	Button btnSearchReplace;
	Button btnSearchTop;
	Button btnSearchBottom;

	ToggleButton btnToggleHexmode;
	ToggleButton btnToggleRecnum;
	ToggleButton btnToggleCrossBeam;
	ResourceBundle messages = null;
	Logger logger = Logger.getLogger(this.getClass().getName());

	FileEditorPane fe = null;
	Stage primaryStage;

	@Override
	public void start(Stage stage) throws Exception {
		// StackPane root = new StackPane();
		// //FXMLLoader.load(getClass().getResource("scene.fxml"));
		// ObservableList list = root.getChildren();
		OpenFileCommand ofc = null;

		List<String> l = getParameters().getRaw();
		if (!l.isEmpty()) {
			ofc = handleCommandLine(l.toArray(new String[0]));
		}

		messages = ResourceBundle.getBundle("org.fc.seqedit.messages");

		primaryStage = stage;
		Pane root = initWidgets();
		Scene scene = new Scene(root, 800, 600);
//		String stylesheet = getClass().getResource("bootstrap3.css").toExternalForm();
//		scene.getStylesheets().add(stylesheet);
		setMenuState();
		primaryStage.setTitle("FileEdit " + PackageInfo.getVersion());
		primaryStage.setScene(scene);
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		    @Override
		    public void handle(WindowEvent t) {
		    	if (doClose()) {
		    		Platform.exit();
		    		System.exit(0);
		    	}
		    	else
		    	{
		    		t.consume();
		    	}
		    }
		});
		stage.show();

		if (ofc != null) {
			open(new File(ofc.filename), ofc.reclen, ofc.readonly, ofc.varlen, ofc.littleEndian);
		}
	}

	OpenFileCommand handleCommandLine(String[] argv) {
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
				Main.wrongParms("missing file name");
			}
			return new OpenFileCommand(file, len, ro, false, false);
		}
		return null;
	}

	public static void main(String[] argv) {
		launch(argv);
	}

	private Pane initWidgets() {
		ToolBar toolBar = new ToolBar();

		btnFileOpenFlat = addToolbarButton(toolBar, CMD_FILE_OPEN_FLAT);
		btnFileSave = addToolbarButton(toolBar, CMD_FILE_SAVE);
		btnFileSaveAs = addToolbarButton(toolBar, CMD_FILE_SAVE_AS);

		toolBar.getItems().add(new Separator());

		btnEditCut = addToolbarButton(toolBar, CMD_EDIT_CUT);
		btnEditCopy = addToolbarButton(toolBar, CMD_EDIT_COPY);
		btnEditPaste = addToolbarButton(toolBar, CMD_EDIT_PASTE);

		toolBar.getItems().add(new Separator());

		btnSearchFind = addToolbarButton(toolBar, CMD_SEARCH_FIND);
		btnSearchFindPrev = addToolbarButton(toolBar, CMD_SEARCH_FIND_PREV);
		btnSearchFindNext = addToolbarButton(toolBar, CMD_SEARCH_FIND_NEXT);
		btnSearchReplace = addToolbarButton(toolBar, CMD_SEARCH_REPLACE);
		btnSearchTop = addToolbarButton(toolBar, CMD_SEARCH_TOP);
		btnSearchBottom = addToolbarButton(toolBar, CMD_SEARCH_BOTTOM);

		toolBar.getItems().add(new Separator());

		btnToggleHexmode = addToolbarToggleButton(toolBar, CMD_TOGGLE_HEXMODE);
		btnToggleRecnum = addToolbarToggleButton(toolBar, CMD_TOGGLE_RECNUM);
		btnToggleCrossBeam = addToolbarToggleButton(toolBar, CMD_TOGGLE_CROSSBEAM);

		menuBar = new MenuBar();
		mFile = new Menu(messages.getString("menu.file"));
		miOpenFlat = addMenuItem(mFile, CMD_FILE_OPEN_FLAT);
		miSave = addMenuItem(mFile, CMD_FILE_SAVE);
		miSaveAs = addMenuItem(mFile, CMD_FILE_SAVE_AS);
		miClose = addMenuItem(mFile, CMD_FILE_CLOSE);
		mFile.getItems().add(new SeparatorMenuItem());
		miExit = addMenuItem(mFile, CMD_FILE_EXIT);
		menuBar.getMenus().add(mFile);

		mEdit = new Menu();
		mEdit.setText(messages.getString("menu.edit"));
		miCut = addMenuItem(mEdit, CMD_EDIT_CUT);
		miCopy = addMenuItem(mEdit, CMD_EDIT_COPY);
		miPaste = addMenuItem(mEdit, CMD_EDIT_PASTE);
		mEdit.getItems().add(new SeparatorMenuItem());
		miInsertAbove = addMenuItem(mEdit, CMD_EDIT_INSERT_ABOVE);
		miInsertBelow = addMenuItem(mEdit, CMD_EDIT_INSERT_BELOW);
		miDelete = addMenuItem(mEdit, CMD_EDIT_DELETE);
		miDeleteMultiple = addMenuItem(mEdit, CMD_EDIT_DELETE_MULTIPLE);
		mEdit.getItems().add(new SeparatorMenuItem());
		mView = new Menu();
		mView.setText(messages.getString("menu.view"));
		ToggleGroup group = new ToggleGroup();
		mrbViewAscii = addRadioButtonMenu(mView, group, CMD_VIEW_ASCII);
		mrbViewAscii.setSelected(true);
		mrbViewEbcdic = addRadioButtonMenu(mView, group, CMD_VIEW_EBCDIC);
		mEdit.getItems().add(mView);
		menuBar.getMenus().add(mEdit);

		mSearch = new Menu();
		mSearch.setText(messages.getString("menu.search"));
		miPageUp = addMenuItem(mSearch, CMD_SEARCH_PGUP, new KeyCodeCombination(KeyCode.F7));
		miPageDown = addMenuItem(mSearch, CMD_SEARCH_PGDW, new KeyCodeCombination(KeyCode.F8));
		mSearch.getItems().add(new SeparatorMenuItem());
		miFind = addMenuItem(mSearch, CMD_SEARCH_FIND, new KeyCodeCombination(KeyCode.F7, KeyCombination.CONTROL_DOWN));
		miFindPrev = addMenuItem(mSearch, CMD_SEARCH_FIND_PREV,
				new KeyCodeCombination(KeyCode.F3, KeyCombination.SHIFT_DOWN));
		miFindNext = addMenuItem(mSearch, CMD_SEARCH_FIND_NEXT, new KeyCodeCombination(KeyCode.F3));
		miReplace = addMenuItem(mSearch, CMD_SEARCH_REPLACE);
		mSearch.getItems().add(new SeparatorMenuItem());
		miSearchTop = addMenuItem(mSearch, CMD_SEARCH_TOP, new KeyCodeCombination(KeyCode.HOME));
		miSearchBottom = addMenuItem(mSearch, CMD_SEARCH_BOTTOM, new KeyCodeCombination(KeyCode.END));
		menuBar.getMenus().add(mSearch);

		mHelp = new Menu();
		mHelp.setText(messages.getString("menu.help"));
		miHelpContents = addMenuItem(mHelp, CMD_HELP_CONTENTS);
		miHelpAbout = addMenuItem(mHelp, CMD_HELP_ABOUT);
		menuBar.getMenus().add(mHelp);

		fe = new FileEditorPane(this, messages);

		BorderPane bp = new BorderPane();
		bp.setTop(new VBox(menuBar, toolBar));
		bp.setCenter(fe);
		return bp;
	}

	MenuItem addMenuItem(Menu m, String cmd, KeyCodeCombination ks) {
		MenuItem mi = addMenuItem(m, cmd);
		mi.setAccelerator(ks);
		return mi;
	}

	MenuItem addMenuItem(Menu m, String cmd) {
		MenuItem mi = new MenuItem(messages.getString("menu." + cmd));
		// String res = "graphics/" + cmd + ".png";
		// mi.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(res))));
		mi.setOnAction(value -> {
			actionPerformed(cmd);
		});
		m.getItems().add(mi);
		return mi;
	}

	RadioMenuItem addRadioButtonMenu(Menu m, ToggleGroup group, String cmd) {
		RadioMenuItem mi = new RadioMenuItem(messages.getString("menu." + cmd));
		// String res = "graphics/" + cmd + ".png";
		// mi.setGraphic(new ImageView(new Image(getClass().getResourceAsStream(res))));
		mi.setOnAction(value -> {
			actionPerformed(cmd);
		});
		group.getToggles().add(mi);
		m.getItems().add(mi);
		return mi;
	}

	private Button addToolbarButton(ToolBar t, String cmd) {
		return (Button) addTollbarAbstractButton(t, cmd, new Button());
	}

	private ToggleButton addToolbarToggleButton(ToolBar t, String cmd) {
		return (ToggleButton) addTollbarAbstractButton(t, cmd, new ToggleButton());
	}

	private ButtonBase addTollbarAbstractButton(ToolBar t, String cmd, ButtonBase b) {
		String res = "graphics/" + cmd + ".png";
		try {
			ImageView iv = new ImageView(new Image(getClass().getResourceAsStream(res)));
			iv.setFitWidth(24);
			iv.setFitHeight(24);
			b.setGraphic(iv);
		} catch (Exception e) {
			try {
				b.setText(messages.getString("toolbar." + cmd));
			} catch (Exception ie) {
				b.setText(cmd);
			}
		}
		b.setOnAction(value -> {
			actionPerformed(cmd);
		});
		t.getItems().add(b);
		return b;
	}

	void setMenuState() {
		boolean fileClose = true;
		boolean noSelection = true;
		boolean flat = false;

		if (fe != null) {
			fileClose = !fe.isOpened();
			noSelection = !fe.hasSelection();
			flat = fe.isFlat();
		}
		miOpenFlat.setDisable(false);
		miSave.setDisable(fileClose);
		miSaveAs.setDisable(true);
		miClose.setDisable(fileClose);
		miExit.setDisable(false);

		mSearch.setDisable(fileClose);
		// miSearchTop.setEnabled(file);
		// miSearchBottom.setEnabled(b);

		mEdit.setDisable(fileClose);
		miCut.setDisable(fileClose || noSelection);
		miCopy.setDisable(fileClose || noSelection);
		miPaste.setDisable(fileClose);
		miReplace.setDisable(fileClose);

		btnFileOpenFlat.setDisable(false);

		btnFileSave.setDisable(fileClose);
		btnFileSaveAs.setDisable(fileClose);

		btnEditCut.setDisable(fileClose || noSelection);
		btnEditCopy.setDisable(fileClose || noSelection);
		btnEditPaste.setDisable(fileClose);
		btnSearchFind.setDisable(fileClose);
		btnSearchFindPrev.setDisable(fileClose);
		btnSearchFindNext.setDisable(fileClose);
		btnSearchReplace.setDisable(fileClose);
		btnSearchTop.setDisable(fileClose);
		btnSearchBottom.setDisable(fileClose);
		miDeleteMultiple.setDisable(!flat);

		btnToggleHexmode.setDisable(fileClose);
		btnToggleRecnum.setDisable(fileClose);
		btnToggleCrossBeam.setDisable(fileClose);

	}

	public static void wrongParms(String m) {
		System.err.println("wrong parameters: " + m);
		Main.showUsage();
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

	public void doOpen(boolean flat) {
		if (!doClose()) {
			return;
		}
		FileDialog fc = new FileDialog(messages);
		Optional<OpenFileCommand> result = fc.showAndWait();
		if (result.isPresent()) {
			OpenFileCommand r = result.get();
			System.out.println("apro a " + r.reclen + " ro=" + r.readonly);
			open(new File(r.filename), r.reclen, r.readonly, r.varlen, r.littleEndian);
		}
		setMenuState();
	}

	public void doSave() {
		try {
			System.out.println("doSave()");
			fe.save();
			System.out.println("file saved");
			FXDialog.messageBox(this, messages.getString("msg.file.saved"));
		} catch (IOException e) {
			FXDialog.errorBox(e);
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
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
				alert.setTitle(messages.getString("dialog.file.changed"));
				alert.setContentText(messages.getString("msg.confirm.file.changed"));
				ButtonType yes = new ButtonType(messages.getString("buttons.yes"), ButtonData.YES);
				ButtonType no = new ButtonType(messages.getString("buttons.no"), ButtonData.NO);
				ButtonType canc = new ButtonType(messages.getString("buttons.cancel"), ButtonData.CANCEL_CLOSE);
				alert.getButtonTypes().setAll(yes, no, canc);

				Optional<ButtonType> result = alert.showAndWait();
				switch (result.get().getButtonData()) {
				case YES:
					fe.save();
					break;

				case NO:
					fe.revertChanges();
					break;

				case CANCEL_CLOSE:
				default:
					return false;
				}
			}
			fe.close();
		} catch (IOException e) {
			FXDialog.errorBox(e);
		}
		setMenuState();
		return true;
	}

	public void open(File f, int reclen, boolean ro, boolean vl, boolean vlle) {

		try {
			fe.open(f, reclen, ro, vl, vlle);
			fe.requestFocus();
			setMenuState();
		} catch (Exception e) {
			FXDialog.errorBox(e);
		}
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
		FindReplaceDialog fc = new FindReplaceDialog(messages, false);
		Optional<FindReplaceCommand> result = fc.showAndWait();
		if (result.isPresent()) {
			FindReplaceCommand r = result.get();
			fe.find(r.getWhat(), r.isIgnoreCase(), r.isForward(), r.isOnRange(), r.getRangeFrom(), r.getRangeTo());
		}
	}

	public void doReplace() {
		FindReplaceDialog fc = new FindReplaceDialog(messages, true);
		Optional<FindReplaceCommand> result = fc.showAndWait();
		if (result.isPresent()) {
			FindReplaceCommand r = result.get();
			fe.replace(r.getWhat(), r.getWith(), r.isIgnoreCase(), r.isForward(), r.isOnRange(), r.getRangeFrom(), r.getRangeTo());
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
			FXDialog.messageBox(messages.getString("msg.cannot.insert.here"));
		}
	}

	public void doDeleteRecord() {
		fe.deleteRecord();
	}

	public void doDeleteMultiple() {
		DeleteMultipleDialog fc = new DeleteMultipleDialog(messages);
		Optional<DeleteMultipleCommand> result = fc.showAndWait();
		if (result.isPresent()) {
			DeleteMultipleCommand r = result.get();
			if (r.getTo() < r.getFrom()) {
				FXDialog.errorBox(messages.getString("msg.wrong.range"));
			} else {
				fe.deleteMultiple(r.getFrom(), r.getTo());
			}
		}
	}

	public void doHelp() {
		FXDialog.errorBox("You ingenuous!!!");
	}

	public void doAbout() {
		Alert alert = new Alert(AlertType.INFORMATION);
		alert.setTitle(messages.getString("caption.about"));
		alert.setHeaderText(org.fc.seqedit.PackageInfo.getName());
		alert.setContentText("Version " + org.fc.seqedit.PackageInfo.getVersion());
		alert.showAndWait();
	}

	public long acceptNumberedKey(long rrn) {
		return 0;
	}

	public void actionPerformed(String cmd) {
		// String cmd = evt.getActionCommand();
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
}