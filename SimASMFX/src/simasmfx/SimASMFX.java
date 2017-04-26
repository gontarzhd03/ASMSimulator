package simasmfx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author hgontarz
 */
//#########################################################################
//#########################################################################
public class SimASMFX extends Application {
    public static final String Caption = "Simple Computer Simulator version 3.00";
    
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("SimASMFX.fxml"));
        String themeUrl = this.getClass().getResource("SimASMFX.css").toExternalForm();
        Scene scene = new Scene(root);
        scene.getStylesheets().clear();
        scene.getStylesheets().add(themeUrl);
        stage.setTitle(Caption);
        stage.setScene(scene);
        stage.show();
    }
    
    /**
     * @param args the command line arguments
     */
//#########################################################################
//#########################################################################
    public static void main(String[] args) {
        launch(args);
    }
    
}
