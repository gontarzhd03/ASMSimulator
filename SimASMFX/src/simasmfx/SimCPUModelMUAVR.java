package simasmfx;
// SimCPUModelSAVR.java by John Phillips on 8/18/2010 revised 10/21/2010
// version 1.01

/** SimModel simulates a simplified version of the Atmel 8-bit AVR
 * All internal work is done in decimal format.
 */
public class SimCPUModelMUAVR implements SimCPUModel {

    private final int maxRam = 256;		// Amount of RAM to hold program and data
    private final int numReg = 8;
    private final int numPorts = 5;
    private final int dataSizeMask = 0x00FF;	// Size of CPU data registers
    private final int addrSizeMask = 0x00FF;	// Size of CPU address register

    private int[] mem;
    private int ip = 0;			// Instr. pointer - the address of the current instruction
    private int pc = 0;			// Program counter starts at address 0
    private int ir = 0;			// Instruction register
    private int ar = 0;			// Address register
    private int sp = maxRam - 1;	// Stack pointer
    private int sr = 0;			// Status register (xxxx xxZC) Z is zero flag, C is carry flag
    private int[] reg;			// General purpose registers
    private int[] port;
    private boolean port0Changed = false;
    private boolean port1Changed = false;
    private boolean port4Changed = false;
    private boolean ramChanged = false;

    public SimCPUModelMUAVR() {
        mem = new int[maxRam];
        reg = new int[numReg];
        port = new int[numPorts];
    }

    // Initialize memory to all zeros
    public void initMemory() {
        for (int i = 0; i < maxRam; i++) {
            mem[i] = 0;
        }
    }

    private void resetRegisters() {
        for (int i = 0; i < numReg; i++) {
            reg[i] = 0;
        }
    }

    private void resetPorts() {
        for (int i = 0; i < numPorts; i++) {
            port[i] = 0;
        }
    }

    // Reset the CPU to the startup state
    public void reset() {
        ip = 0;
        pc = 0;
        ir = 0;
        ar = 0;
        sp = maxRam - 1;
        sr = 0;
        port0Changed = false;
        port1Changed = false;
        port4Changed = false;
        ramChanged = false;
        resetRegisters();
        resetPorts();
    }

    public int getMaxRam() {
        return maxRam;
    }

    public int getRam(int row) {
        return mem[row];
    }

    public int getPort(int p) {
        return port[p];
    }

    public void setRam(int row, int value) {
        mem[row] = checkData(value);
    }
    
    //#########################################################################
    //# setRegVal - Set register values
    //#########################################################################
    @Override
    public void setRegVal(int idx, int value) {
    }
    
    public void setPort(int p, int value) {
        port[p] = checkData(value);
    }

    public boolean isPort0Changed() {
        return port0Changed;
    }

    public boolean isPort1Changed() {
        return port1Changed;
    }

    public boolean isPort4Changed() {
        return port4Changed;
    }

    public boolean isRamChanged() {
        return ramChanged;
    }

    // Register names in column format aligned with toString data.
    public String columnNames() {
        return "PC IR AR SP SR R0 R1 R2 R3 R4 R5 R6 R7";
    }

    // Registers as a string of hex values.
    public String toString() {
        String s = String.format("%02X %02X %02X %02X %02X ", ip, ir, ar, sp, sr);

        for (int i = 0; i < numReg; i++) {
            s += String.format("%02X ", (reg[i]));
        }

        return s;
    }

    // Execute a single instruction.
    // Returns false if the halt instruction was executed.
    public boolean execute() {
        port0Changed = false;
        port1Changed = false;
        port4Changed = false;
        ramChanged = false;
        ip = checkAddress(pc);
        ir = checkData(mem[pc]);
        pc = checkAddress(++pc);
        ar = checkData(mem[pc]);
        pc = checkAddress(++pc);

        if (ir == 0) { // hex 00 - NOP x
            ;
        } else if (ir == 4) {		// hex 04 - ADD Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] += reg[rr];
            setFlags(reg[rd]);
        } else if (ir == 8) {		// hex 08 - ADC Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] += reg[rr] + (isCarryFlagSet() ? 1 : 0);
            setFlags(reg[rd]);
        } else if (ir == 20) {		// hex 14 - SUB Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] -= reg[rr];
            setFlags(reg[rd]);
        } else if (ir == 32) {		// hex 20 - AND Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] &= reg[rr];
            setFlags(reg[rd]);
        } else if (ir == 36) {		// hex 24 - OR Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] |= reg[rr];
            setFlags(reg[rd]);
        } else if (ir == 40) {		// hex 28 - EOR Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] ^= reg[rr];
            setFlags(reg[rd]);
        } else if (ir == 48) {		// hex 30 - INC Rd
            ++reg[ar];
            setFlags(reg[ar]);
        } else if (ir == 49) {		// hex 31 - DEC Rd
            --reg[ar];
            setFlags(reg[ar]);
        } else if (ir == 50) {		// hex 32 - TST Rd
            int temp = reg[ar] & reg[ar];
            setFlags(temp);
        } else if (ir == 64) {		// hex 40 - JMP k
            pc = checkAddress(ar);
        } else if (ir == 68) {		// hex 44 - CALL k
            mem[sp] = pc;
            sp = checkAddress(--sp);
            mem[sp] = sr;
            sp = checkAddress(--sp);
            pc = ar;
        } else if (ir == 72) {		// hex 48 - RET
            sp = checkAddress(++sp);
            sr = checkAddress(mem[sp]);
            sp = checkAddress(++sp);
            pc = checkAddress(mem[sp]);
        } else if (ir == 80) {		// hex 50 - CP Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            int temp = reg[rd] - reg[rr];
            setFlags(temp);
        } else if (ir == 96) {		// hex 60 - BREQ k
            if (isZeroFlagSet()) {
                pc = checkAddress(ar);
            }
        } else if (ir == 97) {		// hex 61 - BRNE k
            if (!isZeroFlagSet()) {
                pc = checkAddress(ar);
            }
        } else if (ir == 98) {		// hex 62 - BRCS k
            if (isCarryFlagSet()) {
                pc = checkAddress(ar);
            }
        } else if (ir == 99) {		// hex 63 - BRCC k
            if (!isCarryFlagSet()) {
                pc = checkAddress(ar);
            }
        } else if (ir == 100) {		// hex 64 - BRSH k
            if (isZeroFlagSet() || !isCarryFlagSet()) { // ???
                pc = checkAddress(ar);
            }
        } else if (ir == 101) {		// hex 65 - BRLO k
            if (!isZeroFlagSet() && isCarryFlagSet()) { // ???
                pc = checkAddress(ar);
            }
        } else if (ir == 111) {		// hex 6F - MOV Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] = reg[rr];
        } else if (ir == 112) {		// hex 70 - LDI R0,k
            reg[0] = ar;
        } else if (ir == 113) {		// hex 71 - LDI R1,k
            reg[1] = ar;
        } else if (ir == 114) {		// hex 72 - LDI R2,k
            reg[2] = ar;
        } else if (ir == 115) {		// hex 73 - LDI R3,k
            reg[3] = ar;
        } else if (ir == 116) {		// hex 74 - LDI R4,k
            reg[4] = ar;
        } else if (ir == 117) {		// hex 75 - LDI R5,k
            reg[5] = ar;
        } else if (ir == 118) {		// hex 76 - LDI R6,k
            reg[6] = ar;
        } else if (ir == 119) {		// hex 77 - LDI R7,k
            reg[7] = ar;
        } else if (ir == 144) {		// hex 90 - LD Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] = checkData(mem[reg[rr]]);
        } else if (ir == 145) {		// hex 91 - LD Rd,Rr+
            int rd = destReg(ar);
            int rr = srcReg(ar);
            reg[rd] = checkData(mem[reg[rr]]);
            reg[rr] = checkData(++reg[rr]);
        } else if (ir == 148) {		// hex 94 - ST Rd,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            mem[reg[rd]] = reg[rr];
            ramChanged = true;
        } else if (ir == 149) {		// hex 95 - ST Rd+,Rr
            int rd = destReg(ar);
            int rr = srcReg(ar);
            mem[reg[rd]] = reg[rr];
            reg[rd] = checkData(++reg[rd]);
            ramChanged = true;
        } else if (ir == 160) {		// hex A0 - IN Rd,P
            int r = destReg(ar);
            int p = srcReg(ar);
            if (p == 2) {
                reg[r] = checkData(port[2]);
            }
            if (p == 3) {
                reg[r] = checkData(port[3]);
            }
        } else if (ir == 161) { 		// hex A1 - OUT P,Rr
            int p = destReg(ar);
            int r = srcReg(ar);
            if (p == 0) {
                port[0] = reg[0];
                port0Changed = true;
            }
            if (p == 1) {
                port[1] = reg[0];
                port1Changed = true;
            }
            if (p == 4) {
                port[4] = reg[0];
                port4Changed = true;
            }
        } else if (ir == 162) {		// hex A2 - Push Rr
            mem[sp] = reg[ar];
            --sp;
            sp = checkAddress(sp);
            ramChanged = true;
        } else if (ir == 163) {		// hex A3 - Pop Rd
            sp = checkAddress(++sp);
            reg[ar] = checkData(mem[sp]);
        } else if (ir == 255) { 		// hex FF - HLT
            return false;
        }

        for (int i = 0; i < numReg; i++) {
            reg[i] = checkData(reg[i]);
        }

        return true;
    }

    private int destReg(int twoRegisters) {
        int dest = twoRegisters & 0x70;
        return dest >>> 4;
    }

    private int srcReg(int twoRegisters) {
        return twoRegisters & 0x07;
    }

    private boolean isZeroFlagSet() {
        return ((sr & 0x0002) == 0x0002);
    }

    private boolean isCarryFlagSet() {
        return ((sr & 0x0001) == 0x0001);
    }

    private void setFlags(int x) {
        if (x > 255) {
            sr |= 0x0001;
        } else {
            sr &= 0xFFFE;
        }

        if ((x & dataSizeMask) == 0) {
            sr |= 0x0002;
        } else {
            sr &= 0xFFFD;
        }
    }

    private int checkAddress(int a) {
        return (a & addrSizeMask);
    }

    private int checkData(int d) {
        return (d & dataSizeMask);
    }

} // end class SimCPUModelMUAVR
