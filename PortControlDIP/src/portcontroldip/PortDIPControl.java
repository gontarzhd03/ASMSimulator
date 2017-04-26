package portcontroldip;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
/**
 * FXML Controller class
 *
 * @author hgontarz
 */
public class PortDIPControl extends AnchorPane {
    @FXML
    private TitledPane idTitledPane;
    @FXML
    private CheckBox checkB8;
    @FXML
    private CheckBox checkB7;
    @FXML
    private CheckBox checkB6;
    @FXML
    private CheckBox checkB5;
    @FXML
    private CheckBox checkB4;
    @FXML
    private CheckBox checkB3;
    @FXML
    private CheckBox checkB2;
    @FXML
    private CheckBox checkB1;

    public PortDIPControl() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("PortDIPControl.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);
        try {
           fxmlLoader.load();
        } catch (IOException exception) {
           throw new RuntimeException(exception);
        }
    }
    public int getCheckButtons() {
        int rc = 0;
        
        if(checkB1.isSelected()) rc |= 0x01;
        if(checkB2.isSelected()) rc |= 0x02;
        if(checkB3.isSelected()) rc |= 0x04;
        if(checkB4.isSelected()) rc |= 0x08;
        if(checkB5.isSelected()) rc |= 0x10;
        if(checkB6.isSelected()) rc |= 0x20;
        if(checkB7.isSelected()) rc |= 0x40;
        if(checkB8.isSelected()) rc |= 0x80;
        return rc;
    }
    public void setCheckButtons(int value) {
        clearButtons();
        if((value & 0x01) == 0x01) checkB1.setSelected(true);
        if((value & 0x02) == 0x02) checkB2.setSelected(true);
        if((value & 0x04) == 0x04) checkB3.setSelected(true);
        if((value & 0x08) == 0x08) checkB4.setSelected(true);
        if((value & 0x10) == 0x10) checkB5.setSelected(true);
        if((value & 0x20) == 0x20) checkB6.setSelected(true);
        if((value & 0x40) == 0x40) checkB7.setSelected(true);
        if((value & 0x80) == 0x80) checkB8.setSelected(true);
    }
    public void setCheckButton(boolean val, int item) {
        switch(item) {
            case 1: checkB1.setSelected(val); break;
            case 2: checkB2.setSelected(val); break;
            case 3: checkB3.setSelected(val); break;
            case 4: checkB4.setSelected(val); break;
            case 5: checkB5.setSelected(val); break;
            case 6: checkB6.setSelected(val); break;
            case 7: checkB7.setSelected(val); break;
            case 8: checkB8.setSelected(val); break;
        }
    }
    public void clearButtons() {
        checkB1.setSelected(false);
        checkB2.setSelected(false);
        checkB3.setSelected(false);
        checkB4.setSelected(false);
        checkB5.setSelected(false);
        checkB6.setSelected(false);
        checkB7.setSelected(false);
        checkB8.setSelected(false);
    }
    public void setTitle(String title) {
        idTitledPane.setText(title);
    }
}
