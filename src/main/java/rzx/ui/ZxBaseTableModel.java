/*
 */
package rzx.ui;

import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * All common functions
 *
 * @author rene
 */
public abstract class ZxBaseTableModel implements TableModel {

  private List<TableModelListener> m_listener = new LinkedList();
  protected String[] m_labels = {"No Labels Set"};      // Table header

  public void setLabels(String[] labels) {
    m_labels = labels;
  }

  public void fireAllTableModelListener() {
    fireAllTableModelListener(new TableModelEvent(this));
  }

  public void fireAllTableModelListener(TableModelEvent event) {
    m_listener.forEach((listener) -> {
      listener.tableChanged(event);
    });
  }

  @Override
  public String getColumnName(int columnIndex) {
    return m_labels[columnIndex];
  }

  @Override
  public int getColumnCount() {
    return m_labels.length;
  }
  
  @Override
  public void addTableModelListener(TableModelListener tl) {
    m_listener.add(tl);
  }

  @Override
  public void removeTableModelListener(TableModelListener tl) {
    m_listener.remove(tl);
  }

}
