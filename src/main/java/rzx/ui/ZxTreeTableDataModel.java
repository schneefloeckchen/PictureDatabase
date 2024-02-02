package rzx.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import picdata.DigiPicture;
import picdata.PicDirectory;
import java.util.logging.Logger;

/**
 * Implements the TableModel and the TreeModel
 *
 * @todo move to the app, this is not generic
 * @author rene
 */
public class ZxTreeTableDataModel extends ZxBaseTableModel implements TreeModel {

    private List<TreeModelListener> m_treeModelListener = new LinkedList();
    // Data for the Tree Model
    private List<PicDirectory> m_allDirectories = null;
    private Map<Long, List<PicDirectory>> m_treeMap = new TreeMap();      // Cache for the directories
    private PicDirectory m_rootDirectory = null;
//    private List<PicDirectory> m_selectedDirectory = null;
    // Data for the table / PicData
    private final String m_labels[] = {"ID", "File Name", "from", "at", "Dupl.?"};
    private final Map<Long, DigiPicture[]> m_pictureMap = new TreeMap<>();      //Cache for the pictures
    private DigiPicture[] m_selectedPictures = null;

    private Logger m_logger = Logger.getLogger(getClass().getName());

    public boolean m_duplicateInfo = false;

    public void loadMedium(long id) {
        m_allDirectories = PicDirectory.getPicDirectoryListByStorageMedium(id);
        for (PicDirectory dir : m_allDirectories) {      // Searching for root directory
            if (dir.getParent() == null) {
                m_rootDirectory = dir;
            }
        }
        if (m_rootDirectory == null) {
            m_logger.severe("No Root Element found");
        } else {
//            m_logger.logDevelop("RootDirectory is " + m_rootDirectory.getDirectoryName() + " / " + m_rootDirectory.getId());
            fireAllTreeListenerForStructureChange();
        }
    }

    /**
     * loads the files from the selected directory in the tree into the table
     *
     * @param id
     */
    public void loadFiles(PicDirectory dir) {
        long id = dir.getId();
        if (m_pictureMap.containsKey(id)) {
            m_selectedPictures = m_pictureMap.get(id);
        } else {
//            Session session = PicHibernateUtil.getSessionFactory().openSession();
//            
            PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
            try (
                    EntityManager em = jpaUtil.createEntityManager()) {

                PicDirectory dir2 = em.find(PicDirectory.class, id);
                Set<DigiPicture> pics = dir2.getPictures();
                m_selectedPictures = new DigiPicture[pics.size()];
                int i = 0;
                Iterator<DigiPicture> iter = pics.iterator();
                while (iter.hasNext()) {
                    m_selectedPictures[i++] = iter.next();
                }
                m_pictureMap.put(id, m_selectedPictures);
            }
//            session.close();
        }
        m_logger.log(Level.FINE, "Folder {0} has {1} pictures", new Object[]{dir.getDirectoryName(), m_selectedPictures.length});
        fireAllTableModelListener();
    }

    public void fireAllTreeListenerForStructureChange() {
        Object[] path = {m_rootDirectory};
        for (TreeModelListener listener : m_treeModelListener) {
            listener.treeStructureChanged(new TreeModelEvent(this, path));
        }
    }

    public DigiPicture getPictureFromRow(int row) {
        return m_selectedPictures[row];
    }

    public void addDuplicateInfo(boolean duplicateInfo) {
        m_duplicateInfo = duplicateInfo;
        fireAllTableModelListener(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }

    // TableModel Interface
    @Override
    public int getRowCount() {
        if (m_selectedPictures == null) {
            return 0;
        } else {
            return m_selectedPictures.length;
        }
    }

    @Override
    public int getColumnCount() {
        return m_labels.length + (m_duplicateInfo ? 0 : -1);
    }

    @Override
    public String getColumnName(int columnIndex) {
        return m_labels[columnIndex];
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
        DigiPicture p = m_selectedPictures[rowIndex];
        return switch (columnIndex) {
            case 0 ->
                p.getId();
            case 1 ->
                p.getFileName();
            case 2 ->
                p.getPictureTakenDay();
            case 3 ->
                p.getPictureTakenTime();
            case 4 ->
                p.hasDuplicates() ? "*" : "";
            default ->
                "";
        };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    // Implementation of the TreeDataModel
    @Override
    public Object getRoot() {
//        m_logger.logDevelop("Getting Root Element");
        return m_rootDirectory;
    }

    @Override
    public Object getChild(Object parent, int index) {
//        m_logger.logDevelop("Getting child for index "+index);
        if (parent instanceof PicDirectory) {
            List<PicDirectory> children = getChildren((PicDirectory) parent);
            return children.get(index);
        } else {
            return null;
        }

    }

    @Override
    public int getChildCount(Object parent) {
//        m_logger.logDevelop("Getting Child count for " + ((PicDirectory) parent).getId());
        if (parent instanceof PicDirectory) {
            List<PicDirectory> children = getChildren((PicDirectory) parent);
//            m_logger.logDevelop("Children count is: " + children.size());
            return children.size();
        } else {
            return 0;
        }
    }

    @Override
    public boolean isLeaf(Object node) {
//        m_logger.logDevelop("Checking for Leaf");
        if (node instanceof PicDirectory) {
            List<PicDirectory> children = getChildren((PicDirectory) node);
            if (children == null || children.size() == 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
        m_treeModelListener.add(l);
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
        m_treeModelListener.remove(l);
    }

    private List<PicDirectory> getChildren(PicDirectory parent) {
        long parentID = parent.getId();
        List<PicDirectory> children = m_treeMap.get(parentID);
        if (children != null) {
            return children;
        } else {
            List<PicDirectory> list = new ArrayList();
            for (PicDirectory dir : m_allDirectories) {
                PicDirectory p = dir.getParent();
                if (p != null) {
                    if (p.getId() == parentID) {
                        list.add(dir);
                    }
                }
            }
            m_treeMap.put(parentID, list);
            return list;
        }
    }
}
