package pictures.tools;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import pictures.ui.BaseDialogUI;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxComboBox;
import rzx.ui.ZxComboBoxEntry;
import rzx.ui.ZxLogPanel;
import rzx.ui.ZxTable;

/**
 * Dialog, um einen einzelnen Record in der Datenbank anzuzeigen Anzeige des
 * ganzen in einer Tabelle, erste Spalte die Labels, zweite die Daten
 *
 * Dialogaufbau:
 *
 * im NORTH Frame fuer Buttons (erst mal reserviert)
 *
 * im CENTER Frame Tabelle fuer die Daten
 *
 * im SOUTH Frame log Feld fuer Meldungen
 *
 * @author rene
 */
public class DisplayRecord extends BaseDialogUI implements TableModel {

  public static final int TABLE_CAMERA = 0;
  public static final int TABLE_DIGI_PICTURE = 1;
  public static final int TABLE_MEDIUM_TYPE = 2;
  public static final int TABLE_PICTURE_MEDIUM = 3;
  public static final int TABLE_PIC_DIRECTORY = 4;

  private static final ZxComboBoxEntry[] TABLE = {
    new ZxComboBoxEntry(TABLE_CAMERA, "CAMERA"),
    new ZxComboBoxEntry(TABLE_DIGI_PICTURE, "DIGI_PICTURE"),
    new ZxComboBoxEntry(TABLE_MEDIUM_TYPE, "MEDIUM_TYPE"),
    new ZxComboBoxEntry(TABLE_PICTURE_MEDIUM, "PICTURE_MEDIUM"),
    new ZxComboBoxEntry(TABLE_PIC_DIRECTORY, "PIC_DIRECTORY"),};

  private static final String[][] columns = {
    {"ID", "VERSION", "CREATION_DATE", "REMARK", "MODEL", "MANUFACTURER"}, // CAMERA
    {"ID", "VERSION", "CREATION_DATE", "PICTURE_TAKEN_DATE", "PICTURE_MILIS",
      "CAMERA", "REMARK", "FILE_NAME", "THUMB_FORMAT", "ORIENTATION", "HEIGHT", "WIDTH"}, // DIGI_PICTURE
    {"ID", "VERSION", "CREATION_DATE", "REMARK", "DEPICTION", "CAPACITY"}, // MEDIUM_TYPE
    {"ID", "VERSION", "CREATION_DATE", "REMARK", "STORAGE_MEDIUM", "CODE", "LABEL", // PICTURE_MEDIUM
      "TITLE", "CONTENT", "DATE_WRITTEN", "WRITTEN_BY"},
    {"ID", "VERSION", "CREATION_DATE", "REMARK", "DIRECTORY_NAME", "DESCRIPTION", // PIC_DIRECTORY
      "MEDIUM", "PARENT"}
  };
  private JButton mu_closeButton = null;
  private ZxTable mu_resultTable = new ZxTable();
  private ZxLogPanel mu_logPanel = new ZxLogPanel();

  private String[] m_fields;
  private Object[] m_resultLine;
  private List<TableModelListener> m_tableModelListener = new ArrayList<>();

  public DisplayRecord() {
    jInit();
  }

  private void jInit() {
    setTitle("Display Record");
    m_uiElementFactory.configure("displayrecord", this);     // u_ui.. in BaseDialog erzeugt
    ZxButtonPanel buttonPanel = new ZxButtonPanel();
    mu_closeButton = buttonPanel.createAndAddButton("close");
    add(buttonPanel, BorderLayout.NORTH);
    add(new JScrollPane(mu_resultTable), BorderLayout.CENTER);
    add(mu_logPanel, BorderLayout.SOUTH);
    finish();
  }

  public void load(int tableId, int id) {
    m_fields = columns[tableId];
    String sqlCommand = "SELECT " + String.join(", ", m_fields) + " FROM "
            + TABLE[tableId].name + " WHERE ID=" + id;
    m_logger.fine(sqlCommand);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(sqlCommand);
      List<Object[]> result = query.getResultList();
      int size = result.size();
      m_logger.log(Level.FINE, "Query done, list length is {0}", size);
      if (size == 0) {
        mu_logPanel.write("No Entry for this ID");
      } else {
        m_resultLine = result.get(0);
        mu_resultTable.setModel(this);
        this.setVisible(true);
        mu_logPanel.write("multiple Entries for this ID: " + size);
      }
    }
  }

  /**
   * erzeugt eine ZxCombobox, ueber die die aufrufende Methode / Anwender die
   * Tabelle auswaehlen kann, aus der gelesen werden soll.
   *
   * Concept is, that all knowledge about the functionality is collected here.
   *
   * @return the created combobox
   */
  public static ZxComboBox getSelectTableComboBox() {
    ZxComboBox comboBox = new ZxComboBox(TABLE);
    return comboBox;
  }

  /**
   * ActionListener interface implementation *
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == mu_closeButton) {
      this.dispose();
    } else {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  // Implementation of the TableModel interface
  @Override
  public int getRowCount() {
    return m_fields.length;
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return "";
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  @Override
  public boolean isCellEditable(int rowIndex, int columnIndex) {
    return false;
  }

  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    try {
      if (columnIndex == 0) {
        return m_fields[rowIndex];
      } else {
        return m_resultLine[rowIndex].toString();
      }
    } catch (NullPointerException ex) {
      return "";
    }
  }

  @Override
  public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

  }

  @Override
  public void addTableModelListener(TableModelListener l) {
    m_tableModelListener.add(l);
  }

  @Override
  public void removeTableModelListener(TableModelListener l) {
    m_tableModelListener.remove(l);
  }

}
