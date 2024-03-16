package pictures.tools;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.Searcher;
import rzx.ui.ZxLogPanel;
//import pictures.tools.PicShortData;

/**
 * Class to remove a medium incl. directories and stores pictures from the
 * database. If a CAMERA object was created, it will remain in the database
 *
 * @todo add internationalization / resource
 *
 * @author rene
 */
public class MediumDeleteProcessor extends RemoveBaseProcessor {

  private HashMap<PicDirectory, ArrayList<PicShortData>> m_directoriesToDelete = null;
  private PictureMedium m_medium = null;
  private long m_mediumId;

  public MediumDeleteProcessor() {

  }

  /**
   * First step for the delete medium process. It fills the data,
   * which are required in the 2nd step to delete the data from the database
   * Separating this allows to do checks in between.
   *
   * @param code CODE field of the medium which needs to be deleted including
   * directories and picture. Pictures will stay in the database if they are
   * stored in further media.
   * @param logArea the log-area from the tools ui.
   */
  public void load(int code, ZxLogPanel logArea) {
    mu_logPanel = logArea;
    Searcher searcher = new Searcher();
    m_directoriesToDelete = new HashMap<>();
    m_medium = searcher.searchPictureMediumByCode(code);
    if (m_medium == null)
      logMessage("CD/DVD with " + code + "not found");
    else {
      m_mediumId = m_medium.getId();
// All directorys regardless of level, working in the loop on this flat list,
// no need to walk into the substructures.
      Set<PicDirectory> dirs = m_medium.getPicDirectories();
      logMessage("Number of Directories found: " + dirs.size());
      /*     try {
      try (Session session = PicHibernateUtil.getSessionFactory().getCurrentSession()) {
      m_logger.log (Level.FINE, "Current session is {0}", session);
      }
      } catch (HibernateException ex) {
      m_logger.log (Level.FINE,"No open session found");
      } */
      for (PicDirectory dir : dirs) {
        logMessage("Working on: " + dir.getDirectoryName());
        Set<DigiPicture> pics = dir.getPictures();
        ArrayList<PicShortData> picList = new ArrayList();
        for (DigiPicture pic : pics) {
          // Pics from one folder
          Set<PicDirectory> dirList = pic.getDirectories();
          //          int occ = pic.getDirectories().size();   // in how many directories is this one?
          int occ = 1;          // indicates, if the picture is in multiple directories in multiple CDs
          if (dirList.size() > 1)     // check, if the multiple pics are on multiple CDs
            for (PicDirectory d : dirList)
              if (d.getMedium().getId() != m_mediumId) {
                occ = 2;
                break;
              }     picList.add(new PicShortData(pic.getId(), pic.getFileName(), occ));
              logMessage("Directory " + dir.getDirectoryName() + " Number of pics: " + picList.size());
              m_directoriesToDelete.put(dir, picList);
        }
      }
    }
  }

  /**
   * shall be used for test and debug purpose only. Dumps the
   * result of the previous prepare (load) process
   */
  public void dumpLoadResultFortest() {
    logMessage("\n -- Result of the load process\n");
    m_directoriesToDelete.forEach(
        (PicDirectory dir, ArrayList<PicShortData> picList) -> {
          logMessage("\n -- for Folder " + dir.getDirectoryName()
              + " ID: " + dir.getId());
          logMessage(" -- Number of pictures to delete: " + picList.size());
          logMessage(" -- Folder is in medium w. ID:"
              + dir.getMedium().getId() + " (" + dir.getMedium().getLabel());
          picList.forEach((PicShortData picShort) -> {
            logMessage("Picture ID " + picShort.id + " (" + picShort.fileName + ")"
                + "   occ:" + picShort.occ);
          });
        });

  }
  /**
   * The actual delete processor.
   * m_directoriesToDelete is a map, each entry has a directory and the
   * associated pictures in the directory.
   */
//  boolean directoryTouched; // to change variables in a lambda they must be global

  public void delete() {
    logMessage("Starting delete");
    m_directoriesToDelete.forEach((PicDirectory dir, ArrayList<PicShortData> picList) -> {
//      Session session = PicHibernateUtil.getSessionFactory().openSession();      // One session per directory
//      Transaction transaction = session.beginTransaction();
      try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
//      directoryTouched = false;
      em.getTransaction().begin();
      picList.forEach((shortPic) -> {
        DigiPicture pic = (DigiPicture) em.find(DigiPicture.class, shortPic.id);
        if (shortPic.occ == 1 && pic != null) {
          em.remove(pic);
          logMessage("Picture " + pic.getFileName() + " deleted");
        }
      });
      logMessage("Delete Pictures finished, committing transaction");
      em.getTransaction().commit();
      logMessage("Committed, starting new transaction to remove the MAP Entries");
      em.getTransaction().begin();
      logMessage("deleteing MAP Entries");
      Query query = em.createNativeQuery("DELETE FROM PIC_DIR_MAP WHERE PIC_DIRECTORY_ID=" + dir.getId());
      query.executeUpdate();
      em.getTransaction().commit();
      }
    });
    logMessage("Deleting reloaded medium, including linked directories");
    m_medium = PictureMedium.getById(m_mediumId);
//    try ( Session session = PicHibernateUtil.getSessionFactory().openSession()) {
//      Transaction transaction = session.beginTransaction();
//      session.delete(m_medium);
//      transaction.commit();
//    }
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      em.remove(em.merge(m_medium));
      em.getTransaction().commit();
    }
    logMessage("Done");
  }

  private void logMessage(String message) {
    if (mu_logPanel == null)
      m_logger.info(message);
    else
      mu_logPanel.write(message);
  }
}
