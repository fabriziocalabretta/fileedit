package org.fc.seqedit;

import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DeleteMultipleDialog extends Dialog<DeleteMultipleCommand> {
	ResourceBundle messages;
	TextField tfFrom;
	TextField tfTo;

	public DeleteMultipleDialog(ResourceBundle rb)
	{
		super();
		messages=rb;
		initWidgets();
	}
	
	private ButtonType bttOk;
    private ButtonType bttCancel;
    
    void initWidgets()
	{
		this.setTitle(messages.getString("dialog.delete.multiple"));
		//dialog.setHeaderText(messages.getString("dialog.open.file"));
		
		tfFrom= new TextField();
		tfFrom.setPromptText("From record");
		tfTo= new TextField();
		tfTo.setPromptText("To record");
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20, 150, 10, 10));
		
		grid.add(new Label(messages.getString("dialog.delete.multiple.from")), 0,0);
		grid.add(tfFrom, 0,  1);
		grid.add(new Label(messages.getString("dialog.delete.multiple.to")), 1,0);
		grid.add(tfTo, 1,  1);
		
		Platform.runLater(() -> tfFrom.requestFocus());
		this.getDialogPane().setContent(grid);
		bttOk = new ButtonType(messages.getString("buttons.ok"), ButtonBar.ButtonData.OK_DONE);
	    bttCancel = new ButtonType(messages.getString("buttons.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(bttOk, bttCancel);
		
		setResultConverter(dialogButton -> {
		    if (dialogButton == bttOk) {
		    	DeleteMultipleCommand r= new DeleteMultipleCommand(
		    			Long.parseLong(tfFrom.getText().trim()),
		    			Long.parseLong(tfTo.getText().trim()));
		        return r;
		    }
		    return null;
		});
		
		Node bOk = getDialogPane().lookupButton(bttOk);
	    bOk.setDisable(true);
	    
		tfFrom.textProperty().addListener((observable, oldValue, newValue) -> {
			bOk.setDisable(newValue.trim().isEmpty() || tfTo.getText().trim().isEmpty());
		});
		tfTo.textProperty().addListener((observable, oldValue, newValue) -> {
			bOk.setDisable(newValue.trim().isEmpty() || tfFrom.getText().trim().isEmpty());
		});
		UnaryOperator<Change> filter = change -> {
		    String text = change.getText();
		    if (text.matches("[0-9]*")) {
		        return change;
		    }
		    return null;
		};
		tfFrom.setTextFormatter(new TextFormatter<>(filter));
		tfTo.setTextFormatter(new TextFormatter<>(filter));
	}
}
