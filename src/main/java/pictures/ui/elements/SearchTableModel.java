package pictures.ui.elements;

import java.util.List;
import javax.swing.event.TableModelEvent;
import rzx.ui.ZxBaseROTableModel;
import rzx.ui.ZxResourceFactory;

/**
 * Base Class for the datamodels, which deliver the table data
 * for the searches for media or folder/directories (PicDirectory)
 * 
 * @author rene
 */
public abstract class SearchTableModel extends ZxBaseROTableModel {

  
  private ZxResourceFactory m_factory = ZxResourceFactory.getInstance();

  protected List m_data = null;

  /**
   * sets the labels for the table. The labels itself are taken from the
   * resource file. Naming convention is:
   * findStorageMedium.table.label.<tab>.<key>.
   *
   * @param tab
   * @param key
   */
  public void setLabels(String tab, String[] key) {
    m_labels = new String[key.length];
    String trailing = "findStorageMedium.table.label." + tab+".";
    for (int i = 0; i < key.length; i++)
      m_labels[i] = m_factory.getString(trailing + key[i]);
  }
    

  public void setData (List data) {
    m_data = data;
    fireAllTableModelListener(new TableModelEvent(
        this, TableModelEvent.HEADER_ROW));    // repaint the table
  }

  public Object getMedium(int index) {
    return m_data.get(index);
  }
  
  @Override
  public int getRowCount() {
    return m_data == null ? 0 : m_data.size();
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }


}
