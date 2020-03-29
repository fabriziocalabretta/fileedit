module FileChooser {
	requires java.desktop;
	requires java.logging;
	requires javafx.controls;
	requires javafx.fxml;
	requires transitive javafx.graphics;
	requires javafx.base;
	requires javafx.swing;
	
	opens net.raumzeitfalle.fx to javafx.fxml;
	opens net.raumzeitfalle.fx.filechooser to javafx.fxml;
	exports net.raumzeitfalle.fx to javafx.graphics, javafx.fxml;
	
}