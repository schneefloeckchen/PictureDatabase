package pictures.tools;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.Searcher;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import rzx.ui.ZxErrorDialog;

/**
 * Task of this class:
 *
 * Run recursive through a folder, jump into subfolder and load all pictures
 * which are found in the folder and subfolder into the database
 *
 * Using of multithreading to prepare for parallel execution of the image
 * processing
 *
 * @author rene
 *
 * 31.10.2023 Migrating to Java 19 and JPA. Replace hibernates proprietary
 * Session mechanism by JPAs EntityManager.
 * 11.5.25 RZ / Migrate to DigiPictureFactory
 */
public class MediumLoadProcessor implements Runnable {

  // Member to control multithreading
  private final String m_theProcessorName = "MediumLoader";
  private final Thread m_theProcessor = new Thread(this, m_theProcessorName);
  private boolean m_runStatus = false;    // Start-Stop of the Thread
  private boolean m_suspended = false;    // true, if thread suspended
  private boolean m_inTest = false;       // true, if started from a jUnit test

  // Regular member
  private File m_folder = null;
  private final Logger m_logger = Logger.getLogger(getClass().getName());
  private PictureMedium m_medium = null;
  private final StatisticCollector m_statisticCollector = StatisticCollector.getInstance();

  public MediumLoadProcessor() {
  }

  public void setMedium(PictureMedium medium) {
    m_medium = medium;
  }

  public void setToTest() {
    m_inTest = true;
    m_runStatus = true;
  }

//  public void process(String folder) {
//    File file = new File(folder);
//    process(file);
//  }
//
  public int process(File folder) {
    if (m_medium == null)
      if (m_inTest)
        System.out.println("MediumLoadProcessor -- No medium loaded");
      else
        ZxErrorDialog.displaySimpleErrorMessage(null, "mediaLoad.error.noMedium");
    else {
      m_statisticCollector.reset();
      m_statisticCollector.startExecution();
      executeOnDirectory(folder, null);                // during the import operation
      String numberOfRoots;
      try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
        Query query = em.createQuery(
                "select count(p) from PicDirectory p where p.medium.id=" + m_medium.getId() + " and p.parent is null");

        numberOfRoots = query.getSingleResult().toString();
        m_logger.log(Level.FINE,
                "MediumLoadProzessor -- numberOfRoot: {0}", numberOfRoots);
      }
      if (m_inTest)
        return Integer.parseInt(numberOfRoots);
      else {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog(null, "Import finished\nnumber of roots:" + numberOfRoots);
      }
      m_runStatus = false;
      m_statisticCollector.endExecution();
      return Integer.parseInt(numberOfRoots);
    }
    return 0;
  }

  /**
   * Method is called recursive and walks through a folder to save the images to
   * the database.
   *
   * Use one entityManager per recursion.
   *
   * @param folder Folder to work through
   * @param parent the parent folder, just for the database entries. If null it
   * is the first folder
   */
  private void executeOnDirectory(File folder, PicDirectory parent) {
    boolean directoryToUpdate = false;
    String directoryName = folder.getName();
    String fullDirectoryName;
    if (parent != null)
      fullDirectoryName = parent.getDirectoryName() + "->" + directoryName;   // not really full, but shall be sufficient
    else
      fullDirectoryName = directoryName; //      m_logger.severe("MediumLoadProcessor -- Warning - Parent folder is null");
    Searcher searcher = new Searcher();
    PicDirectory directory = new PicDirectory();
    directory.setDirectoryName(directoryName);
    directory.setParent(parent);
    if (m_medium == null)
      m_logger.log(Level.SEVERE, "MediumLoadProcessor -- Warning - medium is null - directoryname is {0}", fullDirectoryName);
    directory.setMedium(m_medium);
    m_statisticCollector.startDBSave();
    directory.update();      // Create in database, use own transaction
    m_statisticCollector.endDBsave();

    List<File> directoryList = new ArrayList<>();   // Collect folder for later processing
    File[] files = folder.listFiles();

// Use own entityManager for working on the pictures.
// One EntityManager per folder, but one Transaction per rund
    try (EntityManager entityManager
            = PicJPAUtil.getInstance().createEntityManager()) {
      searcher.setEntityManager(entityManager);
      DigiPictureFactory digiPictureFactory = new DigiPictureFactory();
      PicDirectory mergedDirectory
              = entityManager.merge(directory);
      for (File file : files) {
        entityManager.getTransaction().begin();
        m_logger.log(Level.FINE, "Working on file: {0}", file.getName());
        if (file.isDirectory())
          directoryList.add(file);        // Save for later processing, after closing this transaction
        else
          try {
          digiPictureFactory.loadImageFile(file);
          m_statisticCollector.startDBsearch();
          DigiPicture pic = searcher.searchPictureByNameAndMilis(
                  digiPictureFactory.getFileName(),
                  digiPictureFactory.getPictureTakenMillis());
          m_statisticCollector.endDBsearch();
          if (pic == null) {             // check, if copy of picture is already in DB
            m_statisticCollector.startCompress();
            pic = digiPictureFactory.createDigiPicture();
            pic.pushEntityManagerToSearcher(entityManager);
            m_statisticCollector.endCompress();
            pic.addDirectory(mergedDirectory);     // yes
            m_statisticCollector.startDBSave();
            m_logger.log(Level.FINE, "Saving -- {0}", pic.getFileName());
            pic.persist(entityManager);    // persis the new picture and probably also a new
            m_statisticCollector.endDBsave();   // Camera object
            m_statisticCollector.countPicture();
            m_statisticCollector.addThumbSize(pic.getThumbSize());
          } else {                        // already there, so add this directory to the
            mergedDirectory.addPicture(entityManager.merge(pic));    // move the picture found into the transaction
            m_statisticCollector.countDuplicate(pic.getFileName());
            directoryToUpdate = true;
          }  // @todo cleanup exeeption froliferation 
        } catch (JpegProcessingException ex) {
          m_statisticCollector.addError(file.getName(), fullDirectoryName, "JPEG Processing error");
          log("JPEG Processing Error: " + ex.getLocalizedMessage());
        } catch (IOException ex) {
          m_statisticCollector.addError(file.getName(), fullDirectoryName, "I/O error");
          log("IO Error: " + ex.getLocalizedMessage());
        } catch (SQLException ex) {
          m_statisticCollector.addError(file.getName(), fullDirectoryName, "SQL Processing error");
          log("SQL Processing Error: " + ex.getLocalizedMessage());
        } catch (TiffProcessingException ex) {
          m_statisticCollector.addError(file.getName(), fullDirectoryName, "TIFF Processing error");
          log("TIFF Processing Error: " + ex.getLocalizedMessage());
        } catch (PngProcessingException ex) {
          m_statisticCollector.addError(file.getName(), fullDirectoryName, "PNG Processing error");
          log("Png Processing Error: " + ex.getLocalizedMessage());
        } catch (Exception ex) {
          m_statisticCollector.addError(file.getName(), fullDirectoryName,
                  "General Processing error -> " + ex.getLocalizedMessage());
          log("General Processing Error: " + ex.getLocalizedMessage());
        }
        if (!m_runStatus) {     // Update latest version of directory before thread stops
          updateDirectory(mergedDirectory, directoryToUpdate, entityManager);
          return;                 // Stops the thread
        }
        updateDirectory(mergedDirectory, directoryToUpdate, entityManager);
        synchronized (this) {
          while (m_suspended) try {
            wait();
          } catch (InterruptedException ex) {
          }
        }
        entityManager.getTransaction().commit();
      }   // End of for files.. loop
    }   // end of try
// Now work on the saved folders
    if (!directoryList.isEmpty())
      for (File dir : directoryList)
        if (dir.isDirectory())
          executeOnDirectory(dir, directory);
        else
          m_logger.severe("Internal error -- Hier sollten nur directories auftauchen");
  }

  /**
   * This one is called from inside an transaction
   *
   * @param directory
   * @param update
   * @param em
   */
  private void updateDirectory(PicDirectory directory, boolean update,
          EntityManager em) {
    if (update) {
      m_statisticCollector.startDBSave();
      em.persist(directory);
    }
  }

  private void log(String string) {
    m_logger.severe(string);
  }

  public void start(File folder) {
    log("starting the thread");
    m_folder = folder;
    if (!m_runStatus) {
      m_runStatus = true;
      m_theProcessor.start();
    }
  }

  public void stop() {
    if (m_runStatus)
      if (m_theProcessor.isAlive())
        m_runStatus = false;
  }

  /**
   * Suspends the execution for this thread, called from the user interface
   */
  public void suspendThreadExecution() {
    if (m_theProcessor != null)
      m_suspended = true;
  }

  public void resumeThreadExecution() {
    if (m_theProcessor != null) {
      m_suspended = false;
      synchronized (this) {
        notify();
      }
    }
  }

// implementation of the runnable interface
  @Override
  public void run() {
    m_logger.info("Start of run method");
    process(m_folder);
  }
}
