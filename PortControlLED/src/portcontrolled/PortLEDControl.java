package portcontrolled;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;

/**
 *
 * @author hgontarz
 */
public class PortLEDControl extends AnchorPane {
    @FXML
    private TitledPane idTitledPane;
    @FXML
    private RadioButton radioB1;
    @FXML
    private RadioButton radioB2;
    @FXML
    private RadioButton radioB3;
    @FXML
    private RadioButton radioB4;
    @FXML
    private RadioButton radioB5;
    @FXML
    private RadioButton radioB6;
    @FXML
    private RadioButton radioB7;
    @FXML
    private RadioButton radioB8;
    
    public PortLEDControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PortLEDControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
           fxmlLoader.load();
        } catch (IOException exception) {
           throw new RuntimeException(exception);
        }
    }
    public int getRadioButtons() {
        int rc = 0;
        
        if(radioB1.isSelected()) rc |= 0x01;
        if(radioB2.isSelected()) rc |= 0x02;
        if(radioB3.isSelected()) rc |= 0x04;
        if(radioB4.isSelected()) rc |= 0x08;
        if(radioB5.isSelected()) rc |= 0x10;
        if(radioB6.isSelected()) rc |= 0x20;
        if(radioB7.isSelected()) rc |= 0x40;
        if(radioB8.isSelected()) rc |= 0x80;
        return rc;
    }
    public void setRadioButtons(int value) {
        clearButtons();
        if((value & 0x01) == 0x01) radioB1.setSelected(true);
        if((value & 0x02) == 0x02) radioB2.setSelected(true);
        if((value & 0x04) == 0x04) radioB3.setSelected(true);
        if((value & 0x08) == 0x08) radioB4.setSelected(true);
        if((value & 0x10) == 0x10) radioB5.setSelected(true);
        if((value & 0x20) == 0x20) radioB6.setSelected(true);
        if((value & 0x40) == 0x40) radioB7.setSelected(true);
        if((value & 0x80) == 0x80) radioB8.setSelected(true);
    }
    public void clearButtons() {
        radioB1.setSelected(false);
        radioB2.setSelected(false);
        radioB3.setSelected(false);
        radioB4.setSelected(false);
        radioB5.setSelected(false);
        radioB6.setSelected(false);
        radioB7.setSelected(false);
        radioB8.setSelected(false);
    }
    public void setTitle(String title) {
        idTitledPane.setText(title);
    }
}
