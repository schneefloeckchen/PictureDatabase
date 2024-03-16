package pictures.tools;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import picdata.DigiPicture;
import picdata.PicDirectory;
import rzx.ui.ZxBaseTableModel;

/**
 * Class to find duplicate files in a folder.
 * @todo find out where used, to be removed ??
 * @author rene
 */
public class DuplicateHunter extends ZxBaseTableModel implements Runnable {

    private Logger m_logger = Logger.getLogger(getClass().getName());
    private PicDirectory m_picDirectory = null;       // dir to analyze
    private ArrayList<DuplicateRow> m_duplicateData = null;
    private Set<Integer> m_numberOfDuplicates = null;  // no double entries
//    private int m_numberOfDuplicatesPointer = 0;
    private int m_maxNumberOfDuplicates = 0;

    public DuplicateHunter() {
    }

    public DuplicateHunter(PicDirectory directory) {
        m_picDirectory = directory;
    }

    public void process() {
        m_duplicateData = new ArrayList<>();
        m_numberOfDuplicates = Collections.synchronizedSet(new HashSet());   // to allow double access from the process and the datamodel
//        Session session = PicHibernateUtil.getSessionFactory().openSession();
//        session.refresh(m_picDirectory);
        PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
        try (EntityManager em = jpaUtil.createEntityManager()) {
            PicDirectory picDirectory = em.merge(m_picDirectory);
            Set<DigiPicture> pictures = picDirectory.getPictures();
            for (DigiPicture pic : pictures) {
                Set<PicDirectory> directories = pic.getDirectories();
                DuplicateRow row = new DuplicateRow();
                row.fileName = pic.getFileName();
                row.cdCodes = new ArrayList<>();
                directories.forEach((PicDirectory dir) -> {
                    row.cdCodes.add(dir.getMedium().getCode());
                });
                if (row.cdCodes.size() > m_maxNumberOfDuplicates) {       // as initial value
                    m_maxNumberOfDuplicates = row.cdCodes.size();
                    fireAllTableModelListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
                } else {
                    fireAllTableModelListener();
                }
                m_duplicateData.add(row);
//            fireAllTableModelListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));   // eigentlich nur, wenn spalten erweitert wurden.
            }
//        session.close();
        }
    }

    /**
     * reduces the column count to the next count.
     */
    public void reduce() {
        if (m_maxNumberOfDuplicates > 0) {
            m_maxNumberOfDuplicates--;
        }
        fireAllTableModelListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    public void increase() {
        m_maxNumberOfDuplicates++;
        fireAllTableModelListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    //  Implementation of the remaining methods of the TableModel interface
    @Override
    public int getRowCount() {
        return m_duplicateData.size();
    }

    @Override
    public int getColumnCount() {
        return m_maxNumberOfDuplicates + 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        if (columnIndex == 0) {
            return "file name";
        } else {
            return "CD/DVD Code";
        }
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
        DuplicateRow row = m_duplicateData.get(rowIndex);
        if (columnIndex == 0) {
            return row.fileName;
        } else if (row.cdCodes.size() < columnIndex) {
            return "";
        } else {
            return row.cdCodes.get(columnIndex - 1);   // Column 0 is the filename
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // implements the Runnable interface
    @Override
    public void run() {
        process();
    }

    private class DuplicateRow {

        String fileName;
        ArrayList<Integer> cdCodes = new ArrayList<>();
    }
}
