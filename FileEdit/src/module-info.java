module filedit {
	requires java.desktop;
	requires java.logging;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.base;
	
	opens org.fc.edit to javafx.fxml;
	exports org.fc.edit to javafx.graphics, javafx.fxml;
	
}