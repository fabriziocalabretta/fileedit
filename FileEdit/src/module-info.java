module filedit {
	requires java.desktop;
	requires java.logging;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires javafx.base;
	requires javafx.swing;
	
	opens org.fc.edit to javafx.fxml;
	exports org.fc.edit;
	exports org.fc.io;
	exports org.fc.hdm;
}