package simasmfx;

//SimController.java by John Phillips on 8/18/2010 revised 10/31/2010
//version 1.01 -- initial release with slight bug fix 10/21/2010
//version 2.00 -- integrated assembler 10/31/2010
public class SimController implements Runnable {
    private SimCPUModel model;
    private final SimLCDModel lcdModel;
    private final SimASMFXController view;
    private static boolean running = false;
    private final Thread runner;
    private int delay = 500;
    private String currentFileName;

    public SimController(SimASMFXController view) {
        SetCPUModel(0);
        lcdModel = new SimLCDModel4x40();
        this.view = view;
        this.currentFileName = "FileNotSaved.txt";

        runner = new Thread(this);
        runner.start();
    }

    // This is the main loop of the program simulation. It fetches a line
    // of code from the view and then sends it to the model to be executed.
    // Then it updates the view with the results.
    @Override
    public void run() {
        while (true) {
            if (running) {
                model.setPort(2, view.getSwitch(0));
                model.setPort(3, view.getSwitch(1));
                try {
                    running = model.execute();
                } catch (Exception exc) {
                    running = false;
                    view.setStatus("Error with this instruction. System stopped.");
                }
                view.updateView();
            }

            try {
//                int temp = Integer.parseInt(view.getDelay());

                if (!(delay >= 0 && delay < 9999)) {
                    delay = 500;
                    view.setDelay("500");
                    view.setStatus("Set Delay between 0 and 9999 and then press Run");
                }
                runner.sleep(delay, 1); // add 1 ns for stability when delay is 0 ms
            } catch (InterruptedException e) {
            } catch (Exception e2) {
                running = false;
                view.setStatus("Enter Delay between 0 and 9999 and then press Run");
            }
        }
    }

//#########################################################################
//#########################################################################
    public void setRunning(boolean val) {
        running = val;
    }
    public String getCurrentFileName() {
        return currentFileName;
    }
    public void setCurrentFileName(String val) {
        currentFileName = val;
    }
    public void resetModel() {
        model.reset();
    }
    public void initMemory() {
        model.initMemory();
    }
    public void clearLCD() {
        lcdModel.clearLCD();
    }
    public void Execute() {
        model.execute();
    }
    public int getMaxRam() {
        return model.getMaxRam();
    }
    public int getRam(int row) {
        return model.getRam(row);
    }
    public String getCPUStatus() {
        return model.columnNames() + "\n" + model.toString() + "\n";
    }
    public void setRam(int address, int value) {
        model.setRam(address, value);
    }
    public void setRegVal(int idx, int value) {
        model.setRegVal(idx, value);
    }
    public void SetCPUModel(int val) {
        switch(val) {
            case 0: model = new SimCPUModelMUCPU(); break;
            case 1: model = new SimCPUModelMUAVR(); break;
        }
    }
    public boolean isPortChange(int port) {
        boolean rc = false;
        
        switch(port) {
            case 0: rc = model.isPort0Changed(); break;
            case 1: rc = model.isPort1Changed(); break;
            case 4: rc = model.isPort4Changed(); break;
            case 9: rc = model.isRamChanged(); break;
        }
        return rc;
    }
    public int getPortChange(int port) {
        int rc = 0;
        
        switch(port) {
            case 0: rc = model.getPort(port); break;
            case 1: rc = model.getPort(port); break;
            case 4: rc = model.getPort(port); break;
        }
        return rc;
    }
    public void updateLCD(int x) {
        lcdModel.updateLCD(x);
    }
    public String getLCDString() {
        return lcdModel.toString();
    }
    public void setDelay(int val) {
        delay = val;
    }
} // end class SimController
