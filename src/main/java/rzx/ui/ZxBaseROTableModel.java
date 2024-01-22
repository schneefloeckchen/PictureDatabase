package rzx.ui;

/**
 * Base Table Model for Read Only Tables
 * 
 * 12.12.2020
 * 
 * @author rene
 */
public abstract class ZxBaseROTableModel extends ZxBaseTableModel {

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

}
