package simasmfx;
// SimLCDModel4x40.java by John Phillips on 8/17/2010 revised 10/18/2010
// version 1.0

public class SimLCDModel4x40 implements SimLCDModel {

    private final int maxChar = 160;
    private int[] lcdDisplay = new int[maxChar];
    private int cursor = 0;

    public SimLCDModel4x40() {
        clearLCD();
    }

    public void clearLCD() {
        for (int i = 0; i < maxChar; i++) {
            lcdDisplay[i] = 32;
        }
        cursor = 0;
    }

    public void updateLCD(int x) {
        if (cursor >= maxChar) {
            cursor = 0;
        }

        if (x == 129) {
            clearLCD();
        } else if (x == 10) { // line feed
            if (cursor < 120) {
                cursor += 40;
            } else {
                for (int i = 0; i < 120; i++) {
                    lcdDisplay[i] = lcdDisplay[i + 40];
                }
                for (int i = 120; i < maxChar; i++) {
                    lcdDisplay[i] = 32;
                }
            }
        } else if (x == 13) { // carriage return
            if (cursor < 39) {
                cursor = 0;
            } else if (cursor < 79) {
                cursor = 40;
            } else if (cursor < 119) {
                cursor = 80;
            } else if (cursor < 140) {
                cursor = 120;
            }
        } else if (x == 8 && cursor > 0) { // backspace
            cursor--;
            for (int i = cursor; i < maxChar - 1; i++) {
                lcdDisplay[i] = lcdDisplay[i + 1];
                lcdDisplay[maxChar - 1] = 32;
            }
        } else if (x < 128 && cursor < maxChar) {
            lcdDisplay[cursor++] = x;
        }
    }

    public String toString() {
        String s = "";
        for (int i = 0; i < 159; i++) {
            if (i % 40 == 0 && i != 0) {
                s += "\n";
            }
            s += Character.toString((char) lcdDisplay[i]);
        }
        return s;
    }
} // end SimLCDModel4x40
