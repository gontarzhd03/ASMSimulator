package simasmfx;
// SimCPUModelMUCPU.java by John Phillips on 8/18/2010 revised 10/21/2010
// version 1.01 -- minor updates 10/21/2010
// version 2.00 -- supports SimASM; added CALL, RET, PUSH, POP and stack pointer

//#########################################################################
//# Simulates the MU CompOrg 8-bit MUCPU.
//# All internal work is done in decimal format.
//#########################################################################
public class SimCPUModelMUCPU implements SimCPUModel {
    private final int NOP  = 0;
    private final int LOD  = 16;
    private final int STO  = 17;
    private final int ADD  = 32;
    private final int SUB  = 33;
    private final int ADC  = 34;
    private final int JMP  = 48;
    private final int JZ   = 49;
    private final int JNZ  = 50;
    private final int JC   = 51;
    private final int JNC  = 52;
    private final int IN   = 160;
    private final int OUT  = 161;
    private final int CALL = 176;
    private final int RET  = 177;
    private final int PUSH = 178;
    private final int POP  = 179;
    private final int HLT  = 255;

    private final int maxRam = 256;		// Amount of RAM to hold program and data
    private final int numReg = 5;
    private final int numPorts = 5;
    private final int dataSizeMask = 0x00FF;	// Size of CPU data registers
    private final int addrSizeMask = 0x00FF;	// Size of CPU address register

    private final int[] mem;        // All CPU memory
    private final int[] reg;        // All general purpose registers
    private final int[] port;       // All ports
    private final int[] regValues;  // All register values

    private int ip = 0;	// Instr. pointer - the address of the current instruction
    private int pc = 0;	// Program counter starts at address 0
    private int ir = 0;	// Instruction register
    private int ar = 0;	// Address register
    private int sp = maxRam - 1;	// Stack pointer
    private int sr = 0;	// Status register (xxxx xxZC) Z is zero flag, C is carry flag
    private boolean port0Changed = false;
    private boolean port1Changed = false;
    private boolean port4Changed = false;
    private boolean ramChanged = false;

    //#########################################################################
    //# Constructor SimCPUModelMUCPU - Allocate memory, registers & ports
    //#########################################################################
    public SimCPUModelMUCPU() {
        mem = new int[maxRam];
        reg = new int[numReg];
        port = new int[numPorts];
        regValues = new int[maxRam];
    }

    //#########################################################################
    //# initMemory - Initialize memory to all zeros
    //#########################################################################
    @Override
    public void initMemory() {
        for (int i = 0; i < maxRam; i++) {
            mem[i] = 0;
        }
    }
    
    //#########################################################################
    //# resetRegisters - Reset all general purpose registers to zero
    //#########################################################################
    private void resetRegisters() {
        for (int i = 0; i < numReg; i++) {
            reg[i] = 0;
        }
    }
    
    //#########################################################################
    //# resetRegValues - Reset all register values
    //#########################################################################
    private void resetRegValues() {
        for (int i = 0; i < regValues.length; i++) {
            regValues[i] = 0;
        }
    }

    //#########################################################################
    //# resetPorts - Reset all port values to zero
    //#########################################################################
    private void resetPorts() {
        for (int i = 0; i < numPorts; i++) {
            port[i] = 0;
        }
    }

    //#########################################################################
    //# reset - Reset the CPU to the startup state
    //#########################################################################
    @Override
    public void reset() {
        ip = 0;
        pc = 0;
        ir = 0;
        ar = 0;
        sr = 0;
        sp = maxRam - 1;
        port0Changed = false;
        port1Changed = false;
        port4Changed = false;
        ramChanged = false;
        resetRegisters();
        resetPorts();
        resetRegValues();
    }
    
    //#########################################################################
    //# getMaxRam - Get the maximum amount of ram
    //#########################################################################
    @Override
    public int getMaxRam() {
        return maxRam;
    }
    
    //#########################################################################
    //# getRam - Get an 8-bit value at memory location row
    //#########################################################################
    @Override
    public int getRam(int row) {
        return mem[row];
    }
    
    //#########################################################################
    //# getPort - Get a value at port number
    //#########################################################################
    @Override
    public int getPort(int p) {
        return port[p];
    }
    
    //#########################################################################
    //# setRam - Set memory location with an 8-bit value
    //#########################################################################
    @Override
    public void setRam(int row, int value) {
        mem[row] = checkData(value);
    }
    
    //#########################################################################
    //# setRegVal - Set register values
    //#########################################################################
    @Override
    public void setRegVal(int idx, int value) {
        regValues[idx] = checkData(value);
    }
    
    //#########################################################################
    //# setPort - Set a port number with 8-bit value
    //#########################################################################
    @Override
    public void setPort(int p, int value) {
        port[p] = checkData(value);
    }
    
    //#########################################################################
    //# isPort0Changed
    //#########################################################################
    @Override
    public boolean isPort0Changed() {
        return port0Changed;
    }
    
    //#########################################################################
    //# isPort1Changed
    //#########################################################################
    @Override
    public boolean isPort1Changed() {
        return port1Changed;
    }
    
    //#########################################################################
    //# isPort4Changed
    //#########################################################################
    @Override
    public boolean isPort4Changed() {
        return port4Changed;
    }
    
    //#########################################################################
    //# isRamChanged
    //#########################################################################
    @Override
    public boolean isRamChanged() {
        return ramChanged;
    }
    
    //#########################################################################
    //# columnNames - Register names in column format aligned with toString data.
    //#########################################################################
    @Override
    public String columnNames() {
        return "PC IR AR SR SP  R  A  B  C  D";
    }
    
    //#########################################################################
    //# toString - The essential cpu registers as a String
    //# Program counter or instruction pointer, Instruction register, Address register,
    //# Status register, Stack pointer, General purpose registers
    //#########################################################################
    @Override
    public String toString() {
        String s = String.format("%02X %02X %02X %02X %02X ", ip, ir, ar, sr, sp);

        for (int i = 0; i < numReg; i++) {
            s += String.format("%02X ", reg[i]);
        }
        return s;
    }
    
    //#########################################################################
    //# isZeroFlagSet
    //#########################################################################
    private boolean isZeroFlagSet() {
        return ((sr & 0x0002) == 0x0002);
    }
    
    //#########################################################################
    //# isCarryFlagSet
    //#########################################################################
    private boolean isCarryFlagSet() {
        return ((sr & 0x0001) == 0x0001);
    }
    
    //#########################################################################
    //# setFlags - Status register (xxxx xxZC) Z is zero flag, C is carry flag
    //#########################################################################
    private void setFlags(int x) {
        if (x > 255) {
            sr |= 0x0001; //Set the carry flag
        } else {
            sr &= 0xFFFE; //Mask off carry flag
        }
        if ((x & dataSizeMask) == 0) {
            sr |= 0x0002; //Set the zero flag
        } else {
            sr &= 0xFFFD; //Mask off the zero flag
        }
    }
    
    //#########################################################################
    //# checkAddress - Only allow first 8-bits
    //#########################################################################
    private int checkAddress(int a) {
        return (a & addrSizeMask);
    }
    
    //#########################################################################
    //# checkData - Only allow first 8-bits
    //#########################################################################
    private int checkData(int d) {
        return (d & dataSizeMask);
    }

    //#########################################################################
    //# Execute a single instruction; Returns false if the halt instruction was executed.
    //#########################################################################    
    @Override
    public boolean execute() {
        port0Changed = false;
        port1Changed = false;
        port4Changed = false;
        ramChanged = false;
        ip = checkAddress(pc);
        ir = checkData(mem[pc]);
        int ri = regValues[pc];
        pc = checkAddress(++pc);
        ar = checkData(mem[pc]);
        pc = checkAddress(++pc);

        switch (ir) {
            case NOP:                           // hex 00 - NOP x
                break;
            case LOD:                           // hex 10 - LOD A,[k]
                reg[ri] = checkData(mem[ar]);
                break;
            case STO:                           // hex 11 - STO [k],A
                mem[ar] = reg[ri];
                ramChanged = true;
                break;
            case ADD:                           // hex 20 - ADD A,[k]
                reg[ri] += checkData(mem[ar]);
                setFlags(reg[ri]);
                break;
            case SUB:                           // hex 21 - SUB A,[k]
                reg[ri] -= checkData(mem[ar]);
                setFlags(reg[ri]);
                break;
            case ADC:                           // hex 22 - ADC A,[k]
                reg[ri] += checkData(mem[ar]) + (isCarryFlagSet() ? 1 : 0);
                setFlags(reg[ri]);
                break;
            case JMP:                           // hex 30 - JMP k
                pc = checkAddress(ar);
                break;
            case JZ:                            // hex 31 - JZ k
                if (isZeroFlagSet()) {
                    pc = checkAddress(ar);
                }   
                break;
            case JNZ:                           // hex 32 - JNZ k
                if (!isZeroFlagSet()) {
                    pc = checkAddress(ar);
                }   
                break;
            case JC:                            // hex 33 - JC k
                if (isCarryFlagSet()) {
                    pc = checkAddress(ar);
                }   
                break;
            case JNC:                           // hex 34 - JNC k
                if (!isCarryFlagSet()) {
                    pc = checkAddress(ar);
                }
                break;
            case IN:                            // hex A0 - IN A,P
                if (ar == 2) {
                    reg[ri] = checkData(port[2]);
                }
                if (ar == 3) {
                    reg[ri] = checkData(port[3]);
                }
                break;
            case OUT:                           // hex A1 - OUT P,A
                if (ar == 0) {
                    port[0] = reg[ri];
                    port0Changed = true;
                }
                if (ar == 1) {
                    port[1] = reg[ri];
                    port1Changed = true;
                }
                if (ar == 4) {
                    port[4] = reg[ri];
                    port4Changed = true;
                }
                break;
            case CALL:                          // hex B0 - CALL k
                mem[sp] = pc;
                sp = checkAddress(--sp);
                pc = checkAddress(ar);
                ramChanged = true;
                break;
            case RET:                           // hex B1 - RET
                sp = checkAddress(++sp);
                pc = checkAddress(mem[sp]);
                break;
            case PUSH:                          // hex B2 - PUSH
                if (ar == 0) {
                    mem[sp] = sr;		// PUSH SR
                } else if (ar >= 1) {
                    mem[sp] = reg[ar];           // PUSH A
                }
                sp = checkAddress(--sp);
                ramChanged = true;
                break;
            case POP:                           // hex B3 - POP
                sp = checkAddress(++sp);
                if (ar == 0) {
                    sr = checkAddress(mem[sp]);	// POP SR
                } else if (ar >= 1) {
                    reg[ar] = checkData(mem[sp]);// POP A
                }
                break;
            case HLT:                           // hex FF - HLT
                return false;
            default:
                break;
        }
        for (int i = 0; i < numReg; i++) {
            reg[i] = checkData(reg[i]);
        }
        return true;
    }
} // end class SimCPUModelMUCPU
