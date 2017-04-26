package simasmfx;
// Assembler.java by John Phillips on 10/20/2008 last revised 10/31/2010

// The purpose of this program is to assemble MUCPU source code into machine code.
// This program can be run from the command line or called from another program.
// Revised to support MUCPU2008v6 and MSC v1 10/14/2009
// Revised to support John's Computer Simulator SimCPU on 10/31/2010
import java.util.Scanner;
import java.io.*;
import java.util.regex.*;

public class Assembler {
    // limit program to 256 lines of code

    final int maxRecords = 256;

    // this inner class creates a data structure to track a label's name and address
    class LabelRecord {
        String label;
        int address;

        public LabelRecord(String label, int address) {
            this.label = label;
            this.address = address;
        }
    }

    // holds each unique label and its address
    LabelRecord[] labelRecord = new LabelRecord[maxRecords];

    // holds the original source code with blank lines and comment lines stripped
    String[] source = new String[maxRecords];

    // holds source with remaining comments stripped
    String[] sourceStripped = new String[maxRecords];

    // holds the final machine code
    String[] mc = new String[maxRecords];

    // holds the combined source and mc listing
    String[] listing = new String[maxRecords];

    int[] registers = new int[maxRecords];
    int registerTotal = 0;
    
    String fileName = "";
    int labelTotalLines = 0;
    int sourceTotalLines = 0;
    int sourceStrippedTotalLines = 0;
    int mcTotalLines = 0;
    int listingTotalLines = 0;
    boolean errors = false;
    String errorMessage = "";

    // constructor to get things started when source is in a file
    public Assembler(String fileName) {
        this.fileName = fileName;
        getDataFromFile(fileName);
    }

    // constructor to get things started when source is in a string
    public Assembler(String s, String type) {
        if (type.equalsIgnoreCase("SimASM")) {
            getData(s);
        }
    }

    // read the source code from a string into the source array
    private void getData(String s) {
        errors = false;
        sourceTotalLines = 0;
        s = s.toUpperCase();
        Scanner sc = new Scanner(s);
        while (sc.hasNextLine()) {
            source[sourceTotalLines++] = sc.nextLine();
        }
    }

    // read the source code from a file into the source array
    private void getDataFromFile(String fileName) {
        errors = false;
        sourceTotalLines = 0;
        try {
            Scanner sc = new Scanner(new File(fileName));
            while (sc.hasNextLine()) {
                source[sourceTotalLines++] = sc.nextLine().toUpperCase();
            }
        } catch (Exception e) {;
        }
    }

    // remove comments, blank lines, leading and trailing spaces from source
    private void stripComments() {
        stripFullComments();
        stripPartialComments();
    }

    // Strip the full comments and blank lines from the source
    // This overwrites the original source and is later used
    // when displaying a listing and error messages
    private void stripFullComments() {
        String[] tempSource = new String[maxRecords];
        int j = 0;

        for (int i = 0; i < sourceTotalLines; i++) {
            if (source[i].matches("#.*") || source[i].matches("\\s*")) {;
            } else {
                tempSource[j++] = source[i];
            }
        }
        source = tempSource;
        sourceTotalLines = j;
    }

    // strip the partial comments from the source array
    // sourceStripped will be used as the source to be parsed
    private void stripPartialComments() {
        for (int i = 0; i < sourceTotalLines; i++) {
            String[] result = source[i].split("#", 2);
            String temp = result[0].trim();
            if (!temp.equals("")) {
                sourceStripped[sourceStrippedTotalLines++] = temp;
            }
        }
    }

    // parse each stripped line	into a label, command, and operands
    private void parseLine() {
        int address = 0;

        Pattern p = Pattern.compile("((\\w+):)?\\s*(\\w+)\\s*(.*)",
                Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher("");
        for (int i = 0; i < sourceStrippedTotalLines; i++) {
            m = m.reset(sourceStripped[i]);
            if (m.matches()) {
                String label = m.group(2);
                String command = m.group(3);
                String operand = m.group(4);
                if (label != null) {
                    labelRecord[labelTotalLines++] = new LabelRecord(label, address);
                }
                if (command != null) {
                    address += parseCommand(command, operand, i);
                }
            }
        }
    }

    // called from parseLine; fills in the machine code array (mc) with the opcode
    // sets address labels up for future decoding; fills in DB bytes
    // ! is used to mark label for later replacement
    private int parseCommand(String command, String operand, int currentLine) {
        int rc = 0;
        int register = 0;
        // p1 used to match LOD, STO, ADD, and other commands with [address] in brackets
        Pattern p1 = Pattern.compile(".*?\\[(\\w+)].*?", Pattern.CASE_INSENSITIVE);
        Matcher m1 = p1.matcher(operand);

        // p2 used to match JMP, JZ, JC, and other jump commands
        Pattern p2 = Pattern.compile("(\\w+)", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(operand);

        // p3 used to match IN, OUT, and DB hex digits
        Pattern p3 = Pattern.compile(".*([0-9A-F]{2}).*", Pattern.CASE_INSENSITIVE);
        Matcher m3 = p3.matcher(operand);

        Pattern p4 = Pattern.compile(".*([A-D],{1}).*", Pattern.CASE_INSENSITIVE);
        Matcher m4 = p4.matcher(operand);
        Pattern p5 = Pattern.compile(".*(, [A-D]{1}).*", Pattern.CASE_INSENSITIVE);
        Matcher m5 = p5.matcher(operand);
        if(m4.matches()) {
            String regStr = m4.group(1);
            register = regStr.charAt(0) - 0x40;
        }
        else if(m5.matches()) {
            String regStr = m5.group(1);
            register = regStr.charAt(2) - 0x40;
        }
        if (command.equalsIgnoreCase("NOP")) {
            mc[mcTotalLines++] = "00"; // opcode for NOP -- No Operation
            mc[mcTotalLines++] = "00";
            rc = 2;
        } else if (command.equalsIgnoreCase("OUT")) {
            mc[mcTotalLines++] = "A1";
            if (m3.matches()) {
                mc[mcTotalLines++] = m3.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("IN")) {
            mc[mcTotalLines++] = "A0";
            if (m3.matches()) {
                mc[mcTotalLines++] = m3.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("LOD")) {
            mc[mcTotalLines++] = "10";
            if (m1.matches()) {
                mc[mcTotalLines++] = "!" + m1.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("STO")) {
            mc[mcTotalLines++] = "11";
            if (m1.matches()) {
                mc[mcTotalLines++] = "!" + m1.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("ADD")) {
            mc[mcTotalLines++] = "20";
            if (m1.matches()) {
                mc[mcTotalLines++] = "!" + m1.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("SUB")) {
            mc[mcTotalLines++] = "21";
            if (m1.matches()) {
                mc[mcTotalLines++] = "!" + m1.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("ADC")) {
            mc[mcTotalLines++] = "22";
            if (m1.matches()) {
                mc[mcTotalLines++] = "!" + m1.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("JMP")) {
            mc[mcTotalLines++] = "30";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("JZ")) {
            mc[mcTotalLines++] = "31";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("JC")) {
            mc[mcTotalLines++] = "33";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("JNZ")) {
            mc[mcTotalLines++] = "32";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("JNC")) {
            mc[mcTotalLines++] = "34";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("JNK")) {
            mc[mcTotalLines++] = "35";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("CALL")) {
            mc[mcTotalLines++] = "B0";
            if (m2.matches()) {
                mc[mcTotalLines++] = "!" + m2.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("RET")) {
            mc[mcTotalLines++] = "B1";
            mc[mcTotalLines++] = "00";
            rc = 2;
        } else if (command.equalsIgnoreCase("PUSH")) {
            mc[mcTotalLines++] = "B2";
            if (m2.matches()) {
                if (m2.group(1).equalsIgnoreCase("A")) {
                    mc[mcTotalLines++] = "01";
                    register = 1;
                }
                else if (m2.group(1).equalsIgnoreCase("B")) {
                    mc[mcTotalLines++] = "02";
                    register = 2;
                }
                else if (m2.group(1).equalsIgnoreCase("C")) {
                    mc[mcTotalLines++] = "03";
                    register = 3;
                }
                else if (m2.group(1).equalsIgnoreCase("D")) {
                    mc[mcTotalLines++] = "04";
                    register = 4;
                }
                else if (m2.group(1).equalsIgnoreCase("SR")) {
                    mc[mcTotalLines++] = "00";
                    register = 0;
                } else {
                    errorDetected(currentLine);
                }
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("POP")) {
            mc[mcTotalLines++] = "B3";

            if (m2.matches()) {
                if (m2.group(1).equalsIgnoreCase("A")) {
                    mc[mcTotalLines++] = "01";
                    register = 1;
                } 
                else if (m2.group(1).equalsIgnoreCase("B")) {
                    mc[mcTotalLines++] = "02";
                    register = 2;
                }
                else if (m2.group(1).equalsIgnoreCase("C")) {
                    mc[mcTotalLines++] = "03";
                    register = 3;
                }
                else if (m2.group(1).equalsIgnoreCase("D")) {
                    mc[mcTotalLines++] = "04";
                    register = 4;
                }
                else if (m2.group(1).equalsIgnoreCase("SR")) {
                    mc[mcTotalLines++] = "00";
                    register = 0;
                } else {
                    errorDetected(currentLine);
                }
            } else {
                errorDetected(currentLine);
            }
            rc = 2;
        } else if (command.equalsIgnoreCase("HLT")) {
            mc[mcTotalLines++] = "FF";
            mc[mcTotalLines++] = "00";
            rc = 2;
        } else if (command.equalsIgnoreCase("DB")) {
            if (m3.matches()) {
                mc[mcTotalLines++] = m3.group(1);
            } else {
                errorDetected(currentLine);
            }
            rc = 1;
        }

        if(rc == 0) {
            errorDetected(currentLine);
        }
        else if(rc == 1) {
            registers[registerTotal++] = register;
        }
        else {
            registers[registerTotal++] = register;
            registers[registerTotal++] = register;
        }
        return rc;
    }

    // called when assembling and an error is detected
    private void errorDetected(int line) {
        String s = "A parsing error occured with:\n";
        s += source[line];
        s += "\nThe error occurred near line " + (line + 1);
        s += "\n(not including comments and blank lines).";

        if (errors == false) {
            errorMessage = s;
            errors = true;
        }
    }

    // called when assembling and a missing label is detected
    private void missingLabelDetected(int line) {
        String s = "A label seems to be missing:\n";
        s += source[line];
        s += "\nThe error occurred near line " + (line + 1);
        s += "\n(not including comments and blank lines).";

        if (errors == false) {
            errorMessage = s;
            errors = true;
        }
    }

    // run through the machine code and replace labels with addresses
    private void replaceLabels() {
        for (int i = 0; i < mcTotalLines; i++) {
            if (mc[i].startsWith("!")) {
                String myLabel = mc[i].substring(1);
                int j = 0;
                boolean found = false;
                while (!found && j < labelTotalLines) {
                    if (labelRecord[j].label.equalsIgnoreCase(myLabel)) {
                        found = true;
                        mc[i] = String.format("%02X", labelRecord[j].address);
                    }
                    j++;
                }
                if (!found) {
                    missingLabelDetected(i / 2);
                }
            }
        }
    }

    // write the machine code to a file for use by the MUCPU
    public void writeMC() throws Exception {
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(new File(fileName + ".mc.txt"))));

        for (int i = 0; i < mcTotalLines - 1; i++) {
            out.println(mc[i]);
        }
        out.print(mc[mcTotalLines - 1]);
        out.close();
    }

    // Return the machine code as a String array
    public String[] getMC() {
        return mc;
    }

    // display a subset of an array on the screen
    private void displayArray(String[] sa, int n) {
        for (int i = 0; i < n; i++) {
            System.out.println("i=" + i + ": " + sa[i]);
        }
    }

    // display the source code in the console window
    public void displaySource() {
        displayArray(source, sourceTotalLines);
    }

    // display the source code stripped of comments and blank lines
    public void displaySourceStripped() {
        displayArray(sourceStripped, sourceStrippedTotalLines);
    }

    // display the machine code in the console window
    public void displayMC() {
        displayArray(mc, mcTotalLines);
    }

    // display the label records in the console window
    public void displayLabelRecords() {
        for (int i = 0; i < labelTotalLines; i++) {
            System.out.println(labelRecord[i].label + " " + labelRecord[i].address);
        }
    }

    // display the complete code listing in the console window
    public void displayListing() {
        System.out.println(getListing());
    }

    // return the complete code listing as a String
    public String getListing() {
        int sourceCounter = 0;
        String s = "";

        for (int i = 0; i < mcTotalLines; i++) {
            if (sourceStripped[sourceCounter].matches(".*DB.*")) {
                s += String.format("%02X %2S    ", i, mc[i]);
                s += "" + source[sourceCounter++] + "\n";
            } // 2-byte instructions
            else if (source[sourceCounter] != null) {
                s += String.format("%02X %2S %2S ", i, mc[i], mc[++i]);
                s += "" + source[sourceCounter++] + "\n";
                //s += String.format("%02X %2S", i, mc[i]) + "\n";
            }
        }
        return s;
    }

    // returns true if any errors were detected during the assembly operation
    public boolean hasErrors() {
        return errors;
    }

    // returns a String containing the 1st error message
    public String errorMessage() {
        return errorMessage;
    }

    // assembles the source code into machine code
    public void assemble() {
        //asm.displaySource();
        stripComments();
        //asm.displaySourceStripped();
        parseLine();
        //displayLabelRecords();
        //asm.displayMC();
        replaceLabels();
        //asm.displayMC();
        // displayListing();
        //asm.writeMC();
    }

    // starting point to test this program as a stand alone application
    public static void main(String[] args) throws Exception {
        Scanner s = new Scanner(System.in);
        System.out.print("Enter filename to assemble: ");
        Assembler asm = new Assembler(s.nextLine());
        asm.displaySource();
        asm.stripComments();
        asm.displaySourceStripped();
        asm.parseLine();
        //asm.displayLabelRecords();
        //asm.displayMC();
        asm.replaceLabels();
        //asm.displayMC();
        asm.displayListing();
        asm.writeMC();
    }
}
