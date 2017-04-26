package simasmfx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.*;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import portcontroldip.PortDIPControl;
import portcontrolled.PortLEDControl;

/**
 * FXML Controller class
 *
 * @author hgontarz
 */
public class SimASMFXController implements Initializable {
    boolean Trace = false;
    private static String cpuModel = "mucpu";
    ObservableList<AddrDataRow> data = FXCollections.observableArrayList();
    SimController sim;
    
    @FXML
    private TextField idDelay;
    @FXML
    private TextArea idCPUStatus;
    @FXML
    private PortLEDControl idPort1LED;
    @FXML
    private PortLEDControl idPort0LED;
    @FXML
    private PortDIPControl idPort3DIP;
    @FXML
    private PortDIPControl idPort2DIP;
    @FXML
    private TextArea idPort4LCD;
    @FXML
    private TableView<AddrDataRow> idTblVMemory;
    @FXML
    private TableColumn<String, String> idTblCAddr;
    @FXML
    private TableColumn<String, String> idTblCData;
    @FXML
    private TextArea idTxtSource;
    @FXML
    private Label idStatus;
    @FXML
    private CheckBox idTrace;
    @FXML
    private ToggleGroup mnuOptions;

    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initData();
        sim = new SimController(this);
        onMnuMUCPU(null);
        idPort0LED.setTitle("Port 0 - LEDs");
        idPort1LED.setTitle("Port 1 - LEDs");
        idPort2DIP.setTitle("Port 2 - DIP Switch");
        idPort3DIP.setTitle("Port 3 - DIP Switch");
    }

    @FXML
    private void onMnuNew(ActionEvent event) {
        setStatus("System reset for new program");
        resetSimCpuModel();
        setSource("");
        sim.setCurrentFileName("FileNotSaved.txt");
    }

    @FXML
    private void onMnuOpen(ActionEvent event) {
        String s = "";
        File myFile = chooseFile();
        if (myFile != null) {
            try {
                setStatus("Opening: " + myFile.getName() + "\n");
                resetSimCpuModel();
                setSource("");

                Scanner sc = new Scanner(myFile);
                while (sc.hasNextLine()) {
                    s += sc.nextLine() + "\n";
                }
                setSource(s);
                setStatus("File loaded: " + myFile.getName());
                sim.setCurrentFileName(myFile.getName());
            } catch (IOException ioe) {
                setStatus("ERROR - there was a problem opening " + myFile.getName());
            } catch (Exception other) {
                setStatus("ERROR - opening file");
            }
        } else {
            setStatus("Open command cancelled by user\n");
        }
    }

    @FXML
    private void onMnuSave(ActionEvent event) {
        PrintWriter out;
        File myFile = saveFile();
        if (myFile != null) {
            try {
                setStatus("Saving: " + myFile.getName() + "\n");
                out = new PrintWriter(new BufferedWriter(new FileWriter(myFile)));
                out.print(getSource());
                out.close();
                setStatus(myFile.getName() + " has been saved");
                sim.setCurrentFileName(myFile.getName());
            } catch (IOException ioe) {
                setStatus("ERROR - there was a problem saving " + myFile.getName());
            }
        } else {
            setStatus("Save command cancelled by user\n");
        }
    }

    @FXML
    private void onMnuPrintTrace(ActionEvent event) {
        setStatus("Print Trace for " + sim.getCurrentFileName());
        String s = getTrace();
        print(s);
    }

    @FXML
    private void onMnuPrintCode(ActionEvent event) {
        setStatus("Print Code for " + sim.getCurrentFileName());
        String s = getSource();
        print(s);
    }

    @FXML
    private void onMnuPrintList(ActionEvent event) {
        String s = "";
        setStatus("Print Listing for " + sim.getCurrentFileName());
        Assembler asm = new Assembler(getSource(), "SimASM");
        asm.assemble();
        if (asm.hasErrors()) {
            s += "**** Assembly Error ****\n" + asm.errorMessage() + "\n";
        }
        s += asm.getListing();
        print(s);
    }

    @FXML
    private void onMnuExit(ActionEvent event) {
        System.exit(0);
    }

    @FXML
    private void onMnuMUCPU(ActionEvent event) {
        sim.SetCPUModel(0);
        setCpuModel("mucpu");
        setStatus("System reset for " + getCpuModel());
        resetSimCpuModel();
    }

    @FXML
    private void onMnuAVRCPU(ActionEvent event) {
        sim.SetCPUModel(1);
        setCpuModel("muavr");
        setStatus("System reset for " + getCpuModel());
        resetSimCpuModel();
    }

    @FXML
    private void onMnuOverview(ActionEvent event) {
        String filename = "help/" + getCpuModel() + "Overview.txt";
        InputDlg("A Brief Overview", "", fetchTextFile(filename));
    }

    @FXML
    private void onMnuOpcodes(ActionEvent event) {
        String filename = "help/" + getCpuModel() + "Opcodes.txt";
        InputDlg("A List Of Opcodes", "Samples", fetchTextFile(filename));
    }

    @FXML
    private void onMnuAbout(ActionEvent event) {
        myDialog(SimASMFX.Caption, getSimInfo());
    }

    @FXML
    private void onBtnRun(ActionEvent event) {
        setStatus("Assembling the source code...");

        Assembler asm = new Assembler(getSource(), "SimASM");
        asm.assemble();

        if (asm.hasErrors()) {
            myDialog(asm.errorMessage(), "Assembly Error");
            setStatus("Assembly Error");
        } else {
            String[] tempmc = asm.getMC();
            if (tempmc != null) {
                resetSimCpuModel();
                for (int row = 0; row < tempmc.length && row < sim.getMaxRam() && tempmc[row] != null; row++) {
                    int temp = Integer.valueOf(tempmc[row], 16);
                    sim.setRam(row, temp);
                    data.get(row).setData(String.format("%02X", temp));
                }
                for(int i = 0; i < asm.registerTotal; i++) {
                    sim.setRegVal(i, asm.registers[i]);
                }
                setStatus(sim.getCurrentFileName() + " - Program Running");
                sim.setRunning(true);
            }
        }
    }

    @FXML
    private void onBtnStop(ActionEvent event) {
        setStatus(sim.getCurrentFileName() + " - Program Halted");
        sim.setRunning(false);
    }

    @FXML
    private void onBtnStep(ActionEvent event) {
        setStatus(sim.getCurrentFileName() + " - Program Stepped");
        sim.setRunning(false);
        sim.Execute();
        updateView();
    }

    @FXML
    private void onBtnReset(ActionEvent event) {
        setStatus(sim.getCurrentFileName() + " - CPU State Reset");
        sim.setRunning(false);
        sim.resetModel();
        sim.clearLCD();
        reset();
    }
    
    @FXML
    private void onDelayAction(ActionEvent event) {
        sim.setDelay(Integer.parseInt(getDelay()));
    }
    
    @FXML
    private void onBtnTrace(ActionEvent event) {
        Trace = idTrace.isSelected();
    }
//#########################################################################
//# Startup Methods
//#########################################################################
    // Initialize the memory address/data table
    public void initData() {
        idTblVMemory.getItems().clear();
        data.clear();
        for (int i = 0; i < 256; i++) {
            data.add(new AddrDataRow(String.format("%02X", i), "00"));
        }
        updateTable();
    }
    // Reset all of the panels to the startup state
    public void reset() {
        clear();
        setLED(0, 0);
        setLED(1, 0);
        setSwitch(2, 0);
        setSwitch(3, 0);
        setLCD("");
    }
       // reset the cpu
    private void resetSimCpuModel() {
        reset();
        initData();
        updateTable();
        sim.resetModel();
        sim.initMemory();
    }
//#########################################################################
//# Print function
//#########################################################################
    private void print(Node node) {
       PrinterJob job = PrinterJob.createPrinterJob();
       if (job != null) {
         if(job.showPrintDialog(null)) {
            boolean printed = job.printPage(node);
            if (printed) {
              job.endJob();
            } 
            else {
            }
         }
       } else {
           System.out.println("Could not create a printer job.");
       }
    }
 
    // Print the given string of text on a printer.
    private void print(String myText) {
        String s = getSimInfo() + "\n\n";

        s += "Program: " + sim.getCurrentFileName() + "\n";
        s += "Date/Time: " + Calendar.getInstance().getTime() + "\n";
        s += "Programmer: " + "\n\n";
        s += myText;

        // Throw the string into a text area for easy printing
        TextArea ta = new TextArea(s);
        ta.setBorder(Border.EMPTY);
        ta.setBackground(Background.EMPTY);
        ta.setFont(Font.font("Courier New", FontWeight.NORMAL, 8));
        try {
            print(ta);
            setStatus("Printing " + sim.getCurrentFileName());
        } catch (Exception pe) {
            System.out.println("Printer exception");
            setStatus("Exception - trying to print " + sim.getCurrentFileName());
        }
    }
        // If the model has changed then update the view to match.
    public void updateView() {
        if (!Trace) {
            clear();
        }
        idCPUStatus.appendText(sim.getCPUStatus());
        if (sim.isPortChange(0)) {
            setLED(0, sim.getPortChange(0));
        }
        if (sim.isPortChange(1)) {
            setLED(1, sim.getPortChange(1));
        }
        if (sim.isPortChange(4)) {
            sim.updateLCD(sim.getPortChange(4));
            setLCD(sim.getLCDString());
        }
        if (sim.isPortChange(9)) {
            for (int row = 0; row < sim.getMaxRam(); row++) {
                data.get(row).setData(String.format("%02X", sim.getRam(row)));
            }
            updateTable();
        }
    }

//#########################################################################
//# File selection
//#########################################################################
    public File chooseFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            return file;
        }
        return null;
    }
    public File saveFile() {
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            return file;
        }
        return null;
    }
    // Fetches a text file from the program's directory.
    // Used for reading in help files.
    private String fetchTextFile(String fileName) {
        String s = "";
        String temp = "";
        try {
//            temp = Paths.get("").toAbsolutePath().toString() + "/";
//            temp += fileName;
            InputStream instream = getClass().getResourceAsStream(fileName);
            try (Scanner scanner = new Scanner(instream)) {
                scanner.useDelimiter(System.getProperty("line.separator"));
                while (scanner.hasNext()) {
                    s += scanner.next() + "\n";
                }
            }
        } catch (Exception fnfe) {
            setStatus("Exception - opening " + fileName);
        }
        return s;
    }
//#########################################################################
//# Dialog Boxes
//#########################################################################
    public void ErrorDlg(String hdr, String txt) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error Dialog");
        alert.setHeaderText(hdr);
        alert.setContentText(txt);
        alert.showAndWait();
    }
    public void myDialog(String hdr, String txt) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information Dialog");
        alert.setHeaderText(hdr);
        alert.setContentText(txt);
        alert.showAndWait();
    }
    public String InputDlg(String hdr, String cnt, String txt) {
        String rc;
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(hdr);
        alert.setHeaderText(hdr);
        alert.setContentText(cnt);
        
        TextArea textArea = new TextArea(txt);
        textArea.setEditable(true);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        textArea.setPrefSize(500, 200);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);
        GridPane expContent = new GridPane();
        
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);
        expContent.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        alert.getDialogPane().setExpandableContent(expContent);
        alert.getDialogPane().setExpanded(true);
        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK) {
//            rc = textArea.getText();
            rc = "";
        }
        else {
            rc = "";
        }
        return rc;
    }
//#########################################################################
//# CPU Status TextArea
//#########################################################################
    public void append(String s) {
        idCPUStatus.appendText(s);
//        traceTA.setCaretPosition(traceTA.getText().length());
    }

    public void clear() {
        idCPUStatus.clear();
    }
    public String getTrace() {
        return idCPUStatus.getText();
    }
//#########################################################################
//# All Port Controls
//#########################################################################
    public int getSwitch(int type) {
        int rc = 0;
        
        switch(type) {
            case 0: rc = idPort0LED.getRadioButtons(); break;
            case 1: rc = idPort1LED.getRadioButtons(); break;
            case 2: rc = idPort2DIP.getCheckButtons(); break;
            case 3: rc = idPort3DIP.getCheckButtons(); break;
        }
        return rc;
    }
    public void setSwitch(int type, int val) {
        switch(type) {
            case 0: idPort0LED.setRadioButtons(val); break;
            case 1: idPort1LED.setRadioButtons(val); break;
            case 2: idPort2DIP.setCheckButtons(val); break;
            case 3: idPort3DIP.setCheckButtons(val); break;
        }
    }
    public void setLED(int type, int val) {
        switch(type) {
            case 0: idPort0LED.setRadioButtons(val); break;
            case 1: idPort1LED.setRadioButtons(val); break;
            case 2: idPort2DIP.setCheckButtons(val); break;
            case 3: idPort3DIP.setCheckButtons(val); break;
        }
    }
//#########################################################################
//# 4x40 LCD textarea
//#########################################################################
    public void setLCD(String message) {
        idPort4LCD.setText(message);
    }

    public void append(int c) {
        idPort4LCD.appendText(Character.toString((char) c));
    }
//#########################################################################
//# Status Bar Label
//#########################################################################
    public void setStatus(String s) {
        idStatus.setText(s);
    }

//#########################################################################
//# Memory Table 
//#########################################################################
    public void setValueAt(String addr, String data, int row, int col) {
        boolean dataChanged = false;
        ObservableList<AddrDataRow> ol = FXCollections.observableArrayList();
        
        try {
            ol.add(new AddrDataRow(addr, data));
            if(idTblVMemory.getItems().size() > 0) idTblVMemory.scrollTo(0);
            idTblVMemory.setItems(ol);
            idTblCAddr.setCellValueFactory(new PropertyValueFactory<>("Addr"));
            idTblCData.setCellValueFactory(new PropertyValueFactory<>("Data"));
            dataChanged = true;
        }
        catch(Exception e) {
        }
    }
    public void updateTable() {
        idTblVMemory.setItems(data);
        idTblCAddr.setCellValueFactory(new PropertyValueFactory<>("Addr"));
        idTblCData.setCellValueFactory(new PropertyValueFactory<>("Data"));
    }
        
//#########################################################################
//# Assembly Source [sourceTA -> idTxtSource]
//#########################################################################
    public String getSource() {
        return idTxtSource.getText();
    }
    public void setSource(String s) {
        idTxtSource.setText(s);
    }
//#########################################################################
//#########################################################################
    public String getDelay() {
        return idDelay.getText();
    }
    public void setDelay(String val) {
        idDelay.setText(val);
    }
    public static String getCpuModel() {
        return cpuModel;
    }
    public static void setCpuModel(String s) {
        cpuModel = s;
    }
    public static String getSimInfo() {
        return " CPU model = " + getCpuModel();
    }

}
