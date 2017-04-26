package simasmfx;

import javafx.beans.property.SimpleStringProperty;

public class AddrDataRow {
    private final SimpleStringProperty Addr;
    private final SimpleStringProperty Data;
    
    public AddrDataRow(String addr, String data) {
        this.Addr = new SimpleStringProperty(addr);
        this.Data = new SimpleStringProperty(data);
    }
//### Gets
    public String getAddr() {
        return Addr.get();
    }
    public String getData() {
        return Data.get();
    }
//### Sets
    public void setAddr(String addr) {
        Addr.set(addr);
    }
    public void setData(String data) {
        Data.set(data);
    }
}
