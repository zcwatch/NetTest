package sw.iot.droid.nettest;

/**
 * Created by GoldWatch on 6/21/17.
 */

public class NameAddress {
    private String Name;
    private String Address;
    public NameAddress(String Name, String Address) {
        super();
        this.Name = Name;
        this.Address = Address;
    }
    public String getName() {
        return Name;
    }
    public void setName(String Name) {
        this.Name = Name;
    }
    public String getAddress() {
        return Address;
    }
    public void setAddress(String Address) {
        this.Address = Address;
    }

}