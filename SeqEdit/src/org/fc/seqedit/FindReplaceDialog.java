package org.fc.seqedit;

import java.util.ResourceBundle;
import java.util.function.UnaryOperator;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class FindReplaceDialog extends Dialog<FindReplaceCommand> {
	ResourceBundle messages;
	TextField tfFindWhat;
	TextField tfReplaceWith;
	CheckBox cbHexSearch;
	CheckBox cbIgnoreCase;
	CheckBox cbOnColumnRange;
	RadioButton rbUp;
	RadioButton rbDown;
	TextField tfFromRange;
	TextField tfToRange;
	Label lbFromRange;
	Label lbToRange;
	boolean replaceMode=false;
		
	public FindReplaceDialog(ResourceBundle rb, boolean replace)
	{
		super();
		replaceMode=replace;
		messages=rb;
		initWidgets();
	}
	
	private ButtonType bttOk;
    private ButtonType bttCancel;
    
    void initWidgets()
	{
		this.setTitle(messages.getString(replaceMode?"dialog.replace":"dialog.find"));
		//dialog.setHeaderText(messages.getString("dialog.open.file"));
		
		tfFindWhat = new TextField();
		tfFindWhat.setPromptText("Text to find");
		tfReplaceWith= new TextField();
		tfReplaceWith.setPromptText("Replace with");
		
		cbHexSearch=new CheckBox(messages.getString("dialog.find.hex"));
		cbIgnoreCase=new CheckBox(messages.getString("dialog.find.ignore.case"));
		
		ToggleGroup group = new ToggleGroup();

		rbUp= new RadioButton(messages.getString("dialog.find.direction.up"));
		rbUp.setToggleGroup(group);
		rbDown= new RadioButton(messages.getString("dialog.find.direction.down"));
		rbDown.setToggleGroup(group);
		rbDown.setSelected(true);
		VBox vbDir=new VBox(
			new Label(messages.getString("dialog.find.direction")),
			rbUp,
			rbDown);

		cbOnColumnRange=new CheckBox(messages.getString("dialog.find.on.range"));
		lbFromRange=new Label(messages.getString("dialog.find.range.from"));
		lbFromRange.setDisable(true);
		tfFromRange = new TextField();
		tfFromRange.setPromptText("From column");
		tfFromRange.setDisable(true);
		lbToRange=new Label(messages.getString("dialog.find.range.to"));
		lbToRange.setDisable(true);
		tfToRange= new TextField();
		tfToRange.setPromptText("To column");
		tfToRange.setDisable(true);
		UnaryOperator<Change> filter = change -> {
		    String text = change.getText();
		    if (text.matches("[0-9]*")) {
		        return change;
		    }
		    return null;
		};
		tfFromRange.setTextFormatter(new TextFormatter<>(filter));
		tfToRange.setTextFormatter(new TextFormatter<>(filter));
		cbOnColumnRange.selectedProperty().addListener((observable, oldValue, newValue) -> {
			boolean b=!newValue;
			lbFromRange.setDisable(b);
			lbToRange.setDisable(b);
			tfFromRange.setDisable(b);
			tfToRange.setDisable(b);
		});
				
		VBox vbRange=new VBox(
				cbOnColumnRange,
				lbFromRange,
				tfFromRange,
				lbToRange,
				tfToRange);
		//vbRange.setDisable(true);
		
		VBox info=new VBox();
		info.getChildren().add(new Label(messages.getString("dialog.find.what")));
		info.getChildren().add(tfFindWhat);
		if (replaceMode) {
			info.getChildren().add(new Label(messages.getString("dialog.find.replace")));
			info.getChildren().add(tfReplaceWith);
		}
		info.getChildren().add(cbHexSearch);
		info.getChildren().add(cbIgnoreCase);
		info.getChildren().add(vbDir);
		info.getChildren().add(vbRange);
		
		
		BorderPane content=new BorderPane();
		//content.setTop(hbFn);
		content.setCenter(info);
		//content.setRight(info);
		
		Platform.runLater(() -> tfFindWhat.requestFocus());
		this.getDialogPane().setContent(content);
		bttOk = new ButtonType(messages.getString(replaceMode?"buttons.replace":"buttons.find"), ButtonBar.ButtonData.OK_DONE);
	    bttCancel = new ButtonType(messages.getString("buttons.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(bttOk, bttCancel);
		
		setResultConverter(dialogButton -> {
		    if (dialogButton == bttOk) {
		    	byte[] what=getBytes(tfFindWhat.getText(), cbHexSearch.isSelected());
		    	byte[] with=null;
		    	if (replaceMode)
		    	{
		    		with=getBytes(tfReplaceWith.getText(), cbHexSearch.isSelected());
		    	}
		    	int from=0;
		    	int to=0;
		    	if (cbOnColumnRange.isSelected())
		    	{
		    		from=Integer.parseInt(tfFromRange.getText().trim());
		    		from=Integer.parseInt(tfToRange.getText().trim());
		    	}
		    	FindReplaceCommand r= new FindReplaceCommand(
		    		what,
		    		with,
		    		cbIgnoreCase.isSelected(),
		    		rbDown.isSelected(),
		    		cbOnColumnRange.isSelected(),
		    		from, to);
		    		
		        return r;
		    }
		    return null;
		});
		
		Node bOk = getDialogPane().lookupButton(bttOk);
	    bOk.setDisable(true);
	    
	    if (replaceMode) {
	    	tfFindWhat.textProperty().addListener((observable, oldValue, newValue) -> {
	    		bOk.setDisable(newValue.trim().isEmpty() || tfReplaceWith.getText().trim().isEmpty());
	    	});
	    	tfReplaceWith.textProperty().addListener((observable, oldValue, newValue) -> {
	    		bOk.setDisable(newValue.trim().isEmpty() || tfFindWhat.getText().trim().isEmpty());
	    	});
	    } else {
	    	tfFindWhat.textProperty().addListener((observable, oldValue, newValue) -> {
	    		bOk.setDisable(newValue.trim().isEmpty());
	    	});
	    }
	}
    
    private byte[] getBytes(String s, boolean hex) {
		byte[] b = null;
		if (hex) {
			int len = s.length();
			if ((len % 2) != 0) {
				System.out.println("aggiungo 0");
				s = "0" + s;
				len++;
			}
			b = new byte[len / 2];
			int x = 0;
			for (int i = 0; i < len; i += 2) {
				String h = s.substring(i, i + 2);
				b[x++] = (byte) Integer.parseInt(h, 16);
			}
		} else {
			b = s.getBytes();
		}
		return b;
	}
    
}
