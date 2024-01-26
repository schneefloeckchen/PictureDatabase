package rzx.ui;

import java.util.List;
import java.util.Vector;
import javax.swing.DefaultComboBoxModel;

/**
 * 
 * 
 * @author rene
 * @param <X>
 */
public class ZxComboBoxModel<X extends ZxComboBoxEntry> extends DefaultComboBoxModel{
    
    private int m_sizeOfComboBox = 0;
    
//    public ZxComboBoxModel(Vector<X> entries) {
//        super (entries);
//        m_sizeOfComboBox = entries.size();
//    }
//    
    public ZxComboBoxModel(List<X> entries) {
        super (entries.toArray());
        m_sizeOfComboBox = entries.size();
    }
    
    public ZxComboBoxModel(X[] entries) {
        super(entries);
        m_sizeOfComboBox = entries.length;
    }

    int locateEntryByKey(int id) {
        for (int iret=0; iret<m_sizeOfComboBox; iret++) {
            X entry = (X)getElementAt(iret);
            if (entry.id == id) return iret;
        }
        return -1;
    }
    
    
}
