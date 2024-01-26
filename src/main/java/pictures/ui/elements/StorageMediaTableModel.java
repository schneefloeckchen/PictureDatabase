package pictures.ui.elements;

import java.util.List;
import picdata.PictureMedium;
import rzx.ui.ZxBaseTableModel;

/**
 *
 * @author rene
 */
public class StorageMediaTableModel extends ZxBaseTableModel {

    private List<PictureMedium> mu_pictureMediaData = null;
    private static final String[] LABELS = new String[]{
        "Code", "Label", "Title", "Content"
    };
    
    public StorageMediaTableModel(List<PictureMedium> data) {
        mu_pictureMediaData = data;
    }
    
    
    public PictureMedium getEntryAt(int row) {
        return mu_pictureMediaData.get(row);
    }
    
    public void add(PictureMedium medium) {
        mu_pictureMediaData.add(medium);
        fireAllTableModelListener();
    }
    @Override
    public int getRowCount() {
        return mu_pictureMediaData.size();
    }

    @Override
    public int getColumnCount() {
        return LABELS.length;
    }

    @Override
    public String getColumnName(int i) {
        return LABELS[i];
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int i, int i1) {
        return false;
    }

    @Override
    public Object getValueAt(int i, int i1) {
        PictureMedium medium = mu_pictureMediaData.get(i);
        switch (i1) {
            case 0:
                return medium.getCode();
            case 1:
                return medium.getLabel();
            case 2:
                return medium.getTitle();
            case 3:
                return medium.getContent();
            default: return "internal error";
        }
    }

    @Override
    public void setValueAt(Object o, int i, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
