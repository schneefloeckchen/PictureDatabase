package rzx.ui;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.BevelBorder;

/**
 * Panel, as base class for different tree tables. On the left side a tree,
 * right a table. Depending on the selected leaf on the tree the content
 * on the table changes.
 * 
 * It will work with a ZxTreeTableDataModel
 * 
 * Basic setup is a Split pane, left the tree and right the table.
 * No Buttons?
 * Steuerung der gesamten Table durch das TableModel
 * 
 * Model muss gesetzt sein, bevor das Ding angezeigt wird.
 * 
 * @author rene
 */
public class ZxTreeTable extends ZxPanel {
    
    private ZxTree mu_tree = new ZxTree();
    private ZxTable mu_table = new ZxTable();
    
    private ZxTreeTableDataModel m_dataModel = null;
   
    
    public ZxTreeTable() {
        jInit();
    }
    
    private void jInit() {
        this.setLayout(new GridLayout());
        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(new JScrollPane(mu_tree));
        splitPane.setRightComponent(new JScrollPane(mu_table));
        splitPane.setDividerLocation(300);
        splitPane.setDividerSize(3);
        add(splitPane);
        setBorder(new BevelBorder(BevelBorder.RAISED));
 //       mu_tree.addMouseListener();
    }
    
    public void setDataModel (ZxTreeTableDataModel model) {
        m_dataModel = model;
        mu_tree.setModel(m_dataModel);
    }
    
    
    
//    private ActionListener localListener = (ActionEvent e) -> {
//        Object src = e.getSource();
//        if (src == mu_tree) System.out.println("Ist Tree");
//        else System.out.println("unknown event");
//    } ; 
    
}
