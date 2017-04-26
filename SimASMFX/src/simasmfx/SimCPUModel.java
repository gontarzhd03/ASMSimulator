package simasmfx;
// SimCPUModel.java by John Phillips on 8/18/2010 revised 10/21/2010
// version 1.01

// Interface for 8-bit cpus
public interface SimCPUModel {
    // Initialize ram to all zeros

    public abstract void initMemory();

    // Reset the CPU to the startup state
    public abstract void reset();

    // Return the amount of RAM in bytes
    public abstract int getMaxRam();

    // Return the byte stored at a given address
    public abstract int getRam(int address);

    // Return the value stored at a given port
    public abstract int getPort(int p);

    // Set RAM to a value given an address
    public abstract void setRam(int address, int value);

    // Set RAM to a value given an address
    public abstract void setRegVal(int idx, int value);
    
    // Set the value of the specified port
    public abstract void setPort(int p, int value);

    // Return true if the value of a port has changed this instruction cycle
    //public abstract boolean isPortChanged();
    public boolean isPort0Changed();

    public boolean isPort1Changed();

    public boolean isPort4Changed();
    // Return true if a RAM value has changed this instruction cycle

    public abstract boolean isRamChanged();

    // Returns the essential cpu registers as a String
    public abstract String toString();

    // Returns the names of the registers as a String
    public abstract String columnNames();

    // Execute a single instruction.
    // Returns false if the halt instruction was executed.
    public abstract boolean execute();

} // end interface SimModel
