package simasmfx;
// SimLCDModel.java by John Phillips on 10/18/2010
// version 1.0

public interface SimLCDModel {
    // clear the LCD

    public void clearLCD();

    // write an ASCII int value to the display
    public void updateLCD(int ASCIIvalue);

    // return the contents of the display as a string
    public String toString();
}
