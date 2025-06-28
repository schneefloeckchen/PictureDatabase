package picdata;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pictures.tools.DigiPictureFactory;

/**
 * Test Class, which shall test the simultanious creation of folder and images.
 * Pretest for the MediumLoadProcessor test. Not walking through the filesystem
 * but creating the images here.
 *
 * Two sets of test methods:
 * (1) Create the image objects by code - not loading from the file system
 * (2) Creating the image objects from the file system
 *
 * @author rene
 */
public class ImageDirectoriesTest extends TestBaseClass {

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
    setUpBaseData();  // Creates 1 camera and 3 mediums (m_picMedium1 bis 3)
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Create the folder and the image within one transaction and one EntityManager
   */
  @Test
  protected void oneImageOneFolderInOneTransaction() {
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PicDirectory dir = new PicDirectory();
      dir.setMedium(m_picMedium1);
      dir.setParent(null);

      DigiPicture pic = new DigiPicture();
      pic.setCamera(m_camera1);
      pic.addDirectory(dir);

      em.getTransaction().begin();
      em.persist(dir);
      em.persist(pic);
      em.getTransaction().commit();

// Now for validation
      countDigiPicture(1);
      countDirectory(1);
      countPicDirMap(1);
      countRecords("CAMERA", 1);
    }
  }

  /**
   * Tests a scenario with 2 files, one of them in 2 folders.
   * Steps:
   * (1) Create root folder
   * (2) Create 1st folder (U)
   * (3) Create 2 Images and add them to folder U
   * (4) Create 2nd folder (V)
   * (5) Search for 1st image and add it to folder V
   *
   * 1 to 3 in 1st transaction
   * 4 and 5 in 2nd transaction
   *
   * Shall somehow simulate the MediaLoadProcessor
   */
  @Test
  protected void ThreImages3Folder2TransactionsTest() {

// (1) Create Root folder
    PicDirectory rootDir = new PicDirectory();
    rootDir.setDirectoryName("DVD");
    rootDir.setMedium(m_picMedium1);
    rootDir.setParent(null);   // As it is a root directory
    rootDir.update();

// remaing stuff is done using one EntitityManager and 2 transactions
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {

// (2) Create first folder
      em.getTransaction().begin();
      PicDirectory dirU = new PicDirectory();
      dirU.setParent(rootDir);
      dirU.setMedium(m_picMedium1);
      em.persist(dirU);

// (3) create 2 Images and add them to folder U
      DigiPicture pic1 = new DigiPicture();
      pic1.setCamera(m_camera1);
      pic1.setFileName("F1.JPEG");
      pic1.setPictureTakenMilis(1000);
      pic1.addDirectory(dirU);
      em.persist(pic1);
      DigiPicture pic2 = new DigiPicture();
      pic2.setCamera(m_camera1);
      pic2.setFileName("F2.JPEG");
      pic2.setPictureTakenMilis(1000);
      pic2.addDirectory(dirU);
      em.persist(pic2);
      em.getTransaction().commit();

// (4) create 2nd folder (V)
      em.getTransaction().begin();
      PicDirectory dirV = new PicDirectory();
      dirV.setParent(rootDir);
      dirV.setMedium(m_picMedium1);
      em.persist(dirV);

// (5) Search for first picture and addd it to the Folder V
      Searcher searcher = new Searcher();
      DigiPicture picX = em.merge(searcher.searchPictureByNameAndMilis("F1.JPEG", 1000));
      picX.addDirectory(dirV);
      picX.addDirectory(dirV);
      em.persist(picX);
      em.getTransaction().commit();
    }
  }

  /**
   * Tests initial step, creating the root folder and later in an transaction adding one more
   * folder to it.
   */
  @Test
  protected void twoFolderTest() {
// (1) Create Root folder
    PicDirectory rootDir = new PicDirectory();
    rootDir.setDirectoryName("DVD");
    rootDir.setMedium(m_picMedium1);
    rootDir.setParent(null);   // As it is a root directory
    rootDir.update();    // Uses it own EntityManager and Transaction

// (2) In a new EntityManager and Transaction add a new folder
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {

// (2) Create first folder
      em.getTransaction().begin();
      PicDirectory dirU = new PicDirectory();
      dirU.setDirectoryName("U");
      dirU.setParent(rootDir);      // Kann auch mit merge in den em ziehen
      dirU.setMedium(m_picMedium1);
      em.persist(dirU);
      em.getTransaction().commit();
    }
    countDirectory(2);
  }

  /**
   * Tests initial step, creating the root folder and later in an transaction adding one more
   * folder to it. adding one image to the 2nd folder
   */
  @Test
  protected void twoFolderOneImageTest() {
// (1) Create Root folder
    PicDirectory rootDir = new PicDirectory();
    rootDir.setDirectoryName("DVD");
    rootDir.setMedium(m_picMedium1);
    rootDir.setParent(null);   // As it is a root directory
    rootDir.update();    // Uses it own EntityManager and Transaction

// (2) In a new EntityManager and Transaction add a new folder
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {

// (2) Create first folder
      em.getTransaction().begin();
      PicDirectory dirU = new PicDirectory();
      dirU.setDirectoryName("U");
      dirU.setParent(rootDir);      // Kann auch mit merge in den em ziehen
      dirU.setMedium(m_picMedium1);
      em.persist(dirU);

// (3) add the picture
      String startFolderName = TEST_DATA_FOLDER + "/CD16-5/V/";
      String file1Name = startFolderName + "P1011615.JPG";
      DigiPictureFactory factory = new DigiPictureFactory();
      DigiPicture pic1;
      try {
        factory.loadImageFile(startFolderName, "P1011615.JPG");
        pic1 = factory.createDigiPicture();
      } catch (Exception ex) {
        ex.printStackTrace();
        return;
      }
      long takenMilis = pic1.getPictureTakenMilis();
      pic1.addDirectory(dirU);
//      em.persist(pic1.getCamera());
      pic1.persist(em);      // Takes care of camera, but isn' using a transaction
      em.persist(pic1);

      em.getTransaction().commit();
    }
    countDirectory(2);
    countDigiPicture(1);
    countPicDirMap(1);
    countRecords("CAMERA", 2);
  }

  /**
   * Tests a scenario with 2 files, one of them in 2 folders.
   * Steps:
   * (1) Create root folder
   * (2) Create 1st folder (U)
   * (3) Create 2 Images and add them to folder U
   * (4) Create 2nd folder (V)
   * (5) Search for 1st image and add it to folder V
   *
   * 1 to 3 in 1st transaction
   * 4 and 5 in 2nd transaction
   *
   * Shall somehow simulate the MediaLoadProcessor
   * Same as the previous file, but real images from the filesystem is used.
   * Also the ImageFactory is used.
   */
  @Test
  protected void ThreImagesFromFiles3Folder2TransactionsTest() {
// (1) Create Root folder
    PicDirectory rootDir = new PicDirectory();
    rootDir.setDirectoryName("DVD");
    rootDir.setMedium(m_picMedium1);
    rootDir.setParent(null);   // As it is a root directory
    rootDir.update();    // Uses it own EntityManager and Transaction

// remaing stuff is done using one EntitityManager and 2 transactions
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {

// (2) Create first folder
      em.getTransaction().begin();
      PicDirectory dirU = new PicDirectory();
      dirU.setDirectoryName("U");
      dirU.setParent(rootDir);
      dirU.setMedium(m_picMedium1);
      em.persist(dirU);

// (3) load 2 Pictures to the folder, grabs the pictures from the folder (Just stotage location)
// Nothing to do with folder names etc.
      String startFolderName = TEST_DATA_FOLDER + "/CD16-5/V/";
      String file1Name = startFolderName + "P1011615.JPG";
      String file2Name = startFolderName + "P1011616.JPG";
      try {
        DigiPictureFactory factory = new DigiPictureFactory();
        factory.loadImageFile(startFolderName, "P1011615.JPG");
        DigiPicture pic1 = factory.createDigiPicture();
        long takenMilis = pic1.getPictureTakenMilis();
        pic1.addDirectory(dirU);
//        em.persist(pic1.getCamera());
//        em.persist(pic1);
        pic1.persist(em);
        em.getTransaction().commit();
        em.getTransaction().begin();
        factory.loadImageFile(startFolderName, "P1011616.JPG");
        DigiPicture pic2 = factory.createDigiPicture();
        pic2.addDirectory(dirU);
//        em.persist(pic2);
        pic2.persist(em);
        em.persist(dirU);
        em.getTransaction().commit();

// (4) create 2nd folder (V)
        em.getTransaction().begin();
        PicDirectory dirV = new PicDirectory();
        dirV.setParent(rootDir);
        dirV.setMedium(m_picMedium1);
        em.persist(dirV);

// (5) Search for first picture and addd it to the Folder V
        Searcher searcher = new Searcher();
        DigiPicture picX = em.merge(searcher.
                searchPictureByNameAndMilis("P1011615.JPG", takenMilis));
        picX.addDirectory(dirV);
        picX.addDirectory(dirV);
        em.persist(picX);
        em.getTransaction().commit();
      } catch (Exception ex) {
        ex.printStackTrace();
      }

// Ergebnis in Datenbank prüfen
      countDigiPicture(2);
      countPicDirMap(3);
      countRecords("CAMERA", 2);

    }
  }
}
