package rzx.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.table.TableModel;
import pictures.ui.BaseDialogUI;

/**
 * Dialog which shows tabular content in a table. Only a close button - later
 * export - is visible
 *
 * @author rene
 */
public class ZxGenericTableDialog extends BaseDialogUI {

    private ZxTable mt_table = new ZxTable();

    public ZxGenericTableDialog() {
        jInit();
    }
    
    private void jInit() {
        add(new JScrollPane(mt_table), BorderLayout.CENTER);
        finish();
    }

    public void setModel(TableModel model) {
        mt_table.setModel(model);
    }
    
    // Implement the ActionListener interface from BaseDIalogUI
    @Override
    public void actionPerformed(ActionEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
