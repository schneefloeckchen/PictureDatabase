package rzx.ui;

import java.util.List;
import javax.swing.JComboBox;

/**
 * Extended Combobox, see also existing version from old Zx Library
 *
 * re-written for Java 8
 *
 *
 * @author rene
 */
public class ZxComboBox extends JComboBox {

    private ZxComboBoxModel m_model = null;

    public ZxComboBox() {
    }

//    public ZxComboBox(Vector<ZxComboBoxEntry> entries) {
//        m_model = new ZxComboBoxModel(entries);
//        setModel(m_model);
//    }

    public ZxComboBox(List<ZxComboBoxEntry> entries) {
        m_model = new ZxComboBoxModel(entries);
        setModel(m_model);
    }

    public ZxComboBox(ZxComboBoxEntry[] entries) {
        m_model = new ZxComboBoxModel(entries);
        setModel(m_model);
    }

    public int getSelectedKey() {
        ZxComboBoxEntry entry = (ZxComboBoxEntry) getSelectedItem();
        return entry.id;
    }

    public void setSelectionByKey(long id) {
        setSelectionByKey((int) id);
    }

    public void setSelectionByKey(int id) {
        int index = m_model.locateEntryByKey(id);
        if (index < 0) {
            index = 0;    // Ignore errormessage
        }
        this.setSelectedIndex(index);
    }

}
