package org.fc.seqedit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;

public class FXDialog {

	public static void errorBox(Exception e)
	{
		errorBox(null, e);
	}
	public static void errorBox(Application a, Exception e)
	{
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText(e.getMessage());
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);
		alert.setContentText(sw.toString());
		alert.showAndWait();
	}
	
	public static void errorBox(String s)
	{
		messageBox(AlertType.ERROR, s);
	}
	public static void messageBox(String s)
	{
		messageBox(AlertType.INFORMATION, s);
	}
	public static void messageBox(Application a, String s)
	{
		messageBox(s);
	}
	public static void messageBox(AlertType t, String s)
	{
		Alert alert = new Alert(t);
		alert.setTitle("Information");
		alert.setHeaderText(s);
		alert.setContentText(s);
		alert.showAndWait();
	}
	
	public static boolean confirmBox(Application a, String msg)
	{
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Question");
		//alert.setHeaderText(e.getMessage());
		alert.setContentText(msg);
		alert.showAndWait();
		Optional<ButtonType> result = alert.showAndWait();
		return (result.get() == ButtonType.OK);
	}
	
}
