package rzx.ui;

/**
 * For keyed comboboxes
 * 
 * @author rene
 */
public class ZxComboBoxEntry {
    public int id;
    public String name;    // display type of the entry
    
    public ZxComboBoxEntry(int id, String name) {
        this.id = id;
        this.name = name;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
}
