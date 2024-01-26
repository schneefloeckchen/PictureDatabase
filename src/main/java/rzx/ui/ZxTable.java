/*
 * ZxTable.java
 *
 * Created on 11. Juli 2004, 08:59
 */
package rzx.ui;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

/**  Extension of the JTable class, to add a popup dialog, add a
 * ZxTableController objectt
 *
 * @author  Rene Zillmann
 */
public class ZxTable extends JTable {

//    private ZxTableController m_tableController = null;
    private TableColumnModel m_columnModel = null;

    /** Creates a new instance of ZxTable */
    public ZxTable() {
    }

    /**
     * Creates a table and defines its labels
     * @param labels labels of the table
     * 
     */
    public ZxTable(List labels) {
        super(null, labels.toArray());
    }

//    public ZxTable(List labels) {
//        super(null, labels);
//    }
//
    /**
     * Sets renderer and editor to a tables column
     * @param column column, where configuration has to be changed
     * @param editor new editor
     * @param renderer new renderer
     */
    public void setColumnConfiguration(int column,
            TableCellEditor editor, TableCellRenderer renderer) {
        setColumnRenderer(column, renderer);
        setColumnEditor(column, editor);
    }

    /**
     * assigns a renderer to a column
     * @param column wehre the renderer has to be assigned to
     * @param renderer the new renderer
     */
    public void setColumnRenderer(int column, TableCellRenderer renderer) {
        loadModel();
        m_columnModel.getColumn(column).setCellRenderer(renderer);
    }

    /**
     * assigns a editor to a column
     * @param column wehre the editor has to be assigned to
     * @param editor the new editor
     */
    public void setColumnEditor(int column, TableCellEditor editor) {
        loadModel();
        m_columnModel.getColumn(column).setCellEditor(editor);
    }

    public void setColumnPreferredWidth(int... width) {
        loadModel();
        int widthPointer = 0;
        try {
            Enumeration<TableColumn> columns = m_columnModel.getColumns();
            while (columns.hasMoreElements()) {
                columns.nextElement().setPreferredWidth(width[widthPointer++]);
            }
        } catch (IndexOutOfBoundsException ex) {
            System.err.println("ZxTabke - more fields in column width defined than table has columns");
        }
    }

    public void setColumnWidth(int column, int width) {
        getColumnModel().getColumn(column).setWidth(width);
    }
    
    private void loadModel() {
        m_columnModel = getColumnModel();   // no caching, because table layout may change!
    }

    @Override
    public void setModel(TableModel model) {
        super.setModel(model);
    }

    @Override
    public TableModel getModel() {
        return super.getModel();
    }

//    /**
//     * Adds an table controller to the table.
//     *
//     * @param controller controller instance to be added
//     */
//    public void setController(ZxTableController controller) {
//        m_tableController = controller;
//        m_tableController.registerTable(this);
//    }
}
