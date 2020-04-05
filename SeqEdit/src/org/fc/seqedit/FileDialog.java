package org.fc.seqedit;

import java.io.File;
import java.io.FileFilter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileDialog extends Dialog<OpenFileCommand> {
	ResourceBundle messages;
	TreeView<String> tvDirectoryTree;
	ListView<String> lvFiles;
	TextField tfFilename;
	TextField tfReclen;
	TextField tfFilter;
	CheckBox cbReadOnly;
	CheckBox cbVariableLength;
	CheckBox cbLittleEndian;
	String fileFilter=null;
	
	
	public FileDialog(ResourceBundle rb)
	{
		super();
		messages=rb;
		initWidgets();
	}
	
	private ButtonType bttOk;
    private ButtonType bttCancel;
    //private Button bOk;

	void initWidgets()
	{
		this.setTitle(messages.getString("dialog.open.file"));
		//dialog.setHeaderText(messages.getString("dialog.open.file"));
		
		tfFilename = new TextField();
		tfFilename.setPromptText("Filename");
		tfReclen= new TextField();
		tfReclen.setPromptText("Record length");
		tfFilter= new TextField();
		tfFilter.setPromptText("Filter");
		
		cbReadOnly=new CheckBox(messages.getString("dialog.open.readonly"));
		cbVariableLength=new CheckBox(messages.getString("dialog.open.varlen"));
		cbLittleEndian=new CheckBox(messages.getString("dialog.open.le"));
		
		initDirectoryTree();
		initFileList();
		
		VBox info=new VBox(
				new Label(messages.getString("dialog.filter")),
				tfFilter,
				new Label(messages.getString("dialog.open.reclen")),
				tfReclen,
				cbReadOnly, 
				cbVariableLength, 
				cbLittleEndian);
		
		HBox browser=new HBox(
				tvDirectoryTree,
				lvFiles,
				info); 
		
		HBox hbFn=new HBox(
				new Label(messages.getString("dialog.open.filename")),
				tfFilename);
		HBox.setHgrow(tfFilename, Priority.ALWAYS);
		
		BorderPane content=new BorderPane();
		content.setTop(hbFn);
		content.setCenter(browser);
		content.setRight(info);
		
		Platform.runLater(() -> tfFilename.requestFocus());
		this.getDialogPane().setContent(content);
		bttOk = new ButtonType(messages.getString("buttons.ok"), ButtonBar.ButtonData.OK_DONE);
	    bttCancel = new ButtonType(messages.getString("buttons.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(bttOk, bttCancel);
		
		setResultConverter(dialogButton -> {
		    if (dialogButton == bttOk) {
		    	OpenFileCommand r= new OpenFileCommand(
		    		tfFilename.getText().trim(),
		    		Integer.parseInt(tfReclen.getText().trim()),
		    		cbReadOnly.isSelected(),
		    		cbLittleEndian.isSelected(),
		    		cbVariableLength.isSelected());
		        return r;
		    }
		    return null;
		});
		
		Node bOk = getDialogPane().lookupButton(bttOk);
	    bOk.setDisable(true);
	    
		tfFilename.textProperty().addListener((observable, oldValue, newValue) -> {
			bOk.setDisable(newValue.trim().isEmpty() || tfReclen.getText().trim().isEmpty());
		});
		tfReclen.textProperty().addListener((observable, oldValue, newValue) -> {
			bOk.setDisable(newValue.trim().isEmpty() || tfFilename.getText().trim().isEmpty());
		});
		tfFilter.textProperty().addListener((observable, oldValue, newValue) -> {
			fileFilter=(newValue==null?null:newValue.trim());
			FileTreeItem fti=(FileTreeItem)tvDirectoryTree.getSelectionModel().getSelectedItem();
			if (fti!=null) {
				populateFileList(fti.getFile());
			}
		});
		UnaryOperator<Change> filter = change -> {
		    String text = change.getText();

		    if (text.matches("[0-9]*")) {
		        return change;
		    }

		    return null;
		};
		tfReclen.setTextFormatter(new TextFormatter<>(filter));
	}
	
	private void initDirectoryTree() {
		tvDirectoryTree = new TreeView<String>();
		String hostName = "/";
		try {
			hostName = InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException x) {

		}
		assert hostName != null : "Unable to get local host name.";
		TreeItem<String> rootNode = new TreeItem<>(hostName,
				new ImageView(new Image(getClass().getResourceAsStream("graphics/computer.png"))));
		Iterable<Path> rootDirectories = FileSystems.getDefault().getRootDirectories();
		FileFilter f = new FileFilter() {
			@Override
			public boolean accept(File f) {
		        return f.isDirectory();
		    }
		};
		for (Path name : rootDirectories) {
			FileTreeItem treeNode = new FileTreeItem(new File(name.toString()), f);
			rootNode.getChildren().add(treeNode);
		}
		rootNode.setExpanded(true);
		//this.tvFileBrowser.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tvDirectoryTree.setRoot(rootNode);
		tvDirectoryTree.getSelectionModel().selectedItemProperty().addListener((obs, oldsel, newsel) ->{
			if (newsel!=null) {
				FileTreeItem fti=(FileTreeItem)newsel;
				populateFileList(fti.getFile());
			}
		});
	}
	
	
	private void populateFileList(File f)
	{
		File[] files=f.listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) return false;
				if (fileFilter==null || fileFilter.length()==0) return true;
				return f.getName().contains(fileFilter);
			}
		});
		lvFiles.getItems().clear();
		if (files!=null) {
			for (int i=0;i<files.length;i++)
			{
				lvFiles.getItems().add(files[i].getName());
			}
		}
	}
	
	private void initFileList()
	{
		lvFiles=new ListView<String>();
		lvFiles.getSelectionModel().selectedItemProperty().addListener((obs, oldsel, newsel) ->{
			if (newsel!=null) {
				tfFilename.setText(newsel);
			}
		});
	}
}
