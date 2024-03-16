package pictures.tools;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import picdata.DigiPicture;
import picdata.PicDirectory;
import rzx.ui.ZxLogPanel;

/**
 * Processor to remove the pictures of one directory. If the pictures are in a
 * further directory, on a further storage medium, they shall also be deleted
 * (later to implement). Sub-Folder will not be removed.
 *
 * @todo implement test, rigor testing
 *
 * @author rene
 */
public class DirectoryRemoveProcessor extends RemoveBaseProcessor {

  private ArrayList<PicShortData> m_imagesToRemove = null;
  // Directories, which hold the pictures to remove
  private HashSet<Long> m_affectedDirectories = null;
  private long m_picDirectoryId;
  private PicDirectory m_picDirectory = null;

  /**
   * identifies the pictures which shall be deleted
   *
   * @param id of the directory to analyze
   * @param logArea for the loggin messages
   *
   */
  public void load(long id, ZxLogPanel logArea) {
    mu_logPanel = logArea;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      m_affectedDirectories = new HashSet();
      m_picDirectory = em.find(PicDirectory.class, id);  // Do not use getById, as this closes the session
      if (m_picDirectory == null) {
        writeToLogPanel("Loaded directory is null");
      } else {
        m_picDirectoryId = id;
        m_affectedDirectories.add(m_picDirectoryId);      // Just in case, that it is empty
        m_imagesToRemove = new ArrayList();               // Dame reason..
        Set<DigiPicture> pictures = m_picDirectory.getPictures();
        if (pictures.isEmpty()) {
          writeToLogPanel("No images in this directory");
        } else {
          pictures.forEach((pic) -> {
            Set<PicDirectory> dirs = pic.getDirectories();
            int occ = dirs.size();
            for (PicDirectory dir : dirs) {
              m_affectedDirectories.add(dir.getId());
            }
            Long picId = pic.getId();
            m_imagesToRemove.add(new PicShortData(picId, pic.getFileName(), occ));
            writeToLogPanel(pic.getFileName() + " (" + pic.getId() + ")  Occ:" + occ);
          });
        }
      }
    }
    writeToLogPanel("Directories in Scope:");
    for (Long dirId : m_affectedDirectories) {
      writeToLogPanel(">" + dirId);
    }
    // New session to walk over the affected directories.
    // Check: Still pictures in them, any sub-directories
    // if not: Delete'em
  }

  /**
   * Makes the actual delete operation, based on the results of the load
   * method
   */
  public void delete() {
    writeToLogPanel("Starting delete, number of images in stock: " + m_imagesToRemove.size());
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      m_imagesToRemove.forEach((PicShortData shortPic) -> {
        writeToLogPanel("Deleting id: " + shortPic.id);
        DigiPicture pic = em.find(DigiPicture.class, shortPic.id);
//        Transaction trans = session.beginTransaction();    // needed for the delete
        em.getTransaction().begin();
//        session.delete(pic);      // This removes the picture objects and
        em.remove(pic);                 // This removes the picture objects and
        em.getTransaction().commit();   // the entries in the PIC_DIR_MAP
      });
    }
    writeToLogPanel("Now deleting directories, number is: " + m_affectedDirectories.size());
//    Session session2 = PicHibernateUtil.getSessionFactory().openSession();   // in a new session
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      boolean dirDeleted = true;
      while (dirDeleted) {
        for (Long dirId : m_affectedDirectories) {
          dirDeleted = false;
          PicDirectory dir = em.find(PicDirectory.class, dirId);
          if (dir != null) {                        // This ID was removed in the past
            String dName = dir.getDirectoryName();
            int dPicNum = dir.getPictures().size();
            int dSubNum = dir.getChildren().size();
            if (dPicNum != 0) {
              writeToLogPanel("Folder " + dName + " has still " + dPicNum + " images, deletion skipped");
            } else if (dSubNum != 0) {
              writeToLogPanel("Folder " + dName + " has still " + dSubNum + " child directories, deletion skipped");
            } else {
              em.getTransaction().begin();
              em.remove(dir);
              em.getTransaction().commit();
              writeToLogPanel("Folder deleted " + dName);
              dirDeleted = true;
            }
          }
        }
      }
    }
  }
}
