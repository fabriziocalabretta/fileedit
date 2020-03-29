package org.fc.edit;

import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class FileDialog extends Dialog<OpenFileCommand> {
	ResourceBundle messages;
	TextField tfFilename;
	TextField tfReclen;
	CheckBox cbReadOnly;
	CheckBox cbVariableLength;
	CheckBox cbLittleEndian;
	
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
		
		cbReadOnly=new CheckBox(messages.getString("dialog.open.readonly"));
		cbVariableLength=new CheckBox(messages.getString("dialog.open.varlen"));
		cbLittleEndian=new CheckBox(messages.getString("dialog.open.le"));
		 
		VBox content=new VBox(
				new HBox(
						new Label(messages.getString("dialog.open.filename")),
						tfFilename
						),
				new HBox(
						new Label(messages.getString("dialog.open.reclen")),
						tfReclen
						),
				cbReadOnly, 
				cbVariableLength, 
				cbLittleEndian);
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
		
		
		UnaryOperator<Change> filter = change -> {
		    String text = change.getText();

		    if (text.matches("[0-9]*")) {
		        return change;
		    }

		    return null;
		};
		tfReclen.setTextFormatter(new TextFormatter<>(filter));
	}
}
