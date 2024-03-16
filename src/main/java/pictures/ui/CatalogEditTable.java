/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pictures.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import picdata.PicCatalogItem;
import pictures.ui.elements.TableModelBase;
import java.util.logging.Logger;

/**
 * Class builds a table which is used to edit a catalog. Table stores all entries
 * of the catalog. Edit done in the table
 *
 * @author rene
 */
public class CatalogEditTable extends JTable implements TableModelBase {

//    private static SessionFactory m_factory;
    private ArrayList<TableModelListener> m_listener = new ArrayList();
    private String[] m_labels = null;
    private String m_catalog = null;         // Stores the class name of the current table
    private int m_colCount = 0;
    private ArrayList<PicCatalogItem> m_catalogItems = null;
    private final Logger m_logger = Logger.getLogger(getClass().getName());

    public CatalogEditTable() {
        jInit();
    }

    private void jInit() {
        setModel(this);
    }

    /**
     * Initializes the catalog UI by defining the labels and loading the data
     * from the database
     * 
     * @param catalog Class name for the catalog
     * @param labels column nammes for the catalog columns, 
     */
    public void initializeCatalog(String catalog, String[] labels) {
        m_catalog = catalog;
        setLabels(TableModelBase.getLabels("catalogUI.edit.label." + catalog, labels));
        m_logger.log(Level.FINE, "New labels set, loading data from catalog {0}", catalog);
        loadAll(catalog);
    }

    public void setLabels(String labels[]) {
        m_labels = labels;
        m_colCount = m_labels.length;           // to avoid permanent calaculation
    }                                           // in getColumnCount

    public String getCatalogName() {
        return m_catalog;
    }

/**    public void loadAll(String table) {
        try {
            Session session = PicHibernateUtil.getSessionFactory().openSession();
            Transaction tx = null;
            tx = session.beginTransaction();
            Query query = session.createQuery("from " + table);
            m_catalogItems = (ArrayList) query.list();
            m_logger.log(Level.FINE, "Loaded " + m_catalogItems.size());
            updateAllListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
            m_logger.fine("Update All Listener gelaufen");
        } catch (HibernateException ex) {
            ZxErrorDialog.displaySimpleErrorMessage(this, "generic.error.hibernateError");
            ex.printStackTrace();
        } catch (ExceptionInInitializerError ex) {
            ZxErrorDialog.displaySimpleErrorMessage(this, "generic.error.noDatabase");
        }
    }
**/
    /**
     * loads all Elements of the given database table into the JTable
     * 
     * @param table 
     */
    public void loadAll(String table) {
      try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
        Query query = em.createQuery("from "+table);
        m_catalogItems = (ArrayList) query.getResultList();
        updateAllListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
      }
    }

    /**
     * Adds the item to the table in the ui, but not into the database
     * @param item 
     */
    public void addEntry(PicCatalogItem item) {
        m_catalogItems.add(item);
        updateAllListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    /**
     * saves the updated catalog items to the database
     */
    void saveChanges() {
        m_catalogItems.forEach((catalogItem) -> {
            if (catalogItem.isDirty())
                catalogItem.update();
        });
    }

    public void updateAllListener(TableModelEvent evt) {
        m_listener.forEach((l) -> {
            l.tableChanged(evt);
        });
    }

    // Implementation of the TableModel
    @Override
    public int getColumnCount() {
        return m_colCount;
    }

    @Override
    public int getRowCount() {
        int rowCount = 0;
        if (m_catalogItems != null)
            rowCount = m_catalogItems.size();
        return rowCount;
    }

    @Override
    public Object getValueAt(int row, int col) {
        PicCatalogItem catalogItem = m_catalogItems.get(row);
        return catalogItem.getEntry(col);
    }

    @Override
    public void setValueAt(Object o, int row, int col) {
        PicCatalogItem catalogItem = m_catalogItems.get(row);
        catalogItem.setEntry((String) o, col);
    }

    @Override
    public String getColumnName(int i) {
        return m_labels[i];
    }

    @Override
    public Class<?> getColumnClass(int i) {
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return true;
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
