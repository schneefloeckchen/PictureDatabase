package pictures.tools;

// import hib.PicHibernateUtil;
import java.util.List;
import java.util.Set;
import static org.junit.Assert.fail;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.TestBaseClass;

import static bas.TestBase.TEST_DATA_FOLDER;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import picdata.DigiPicture;

/**
 * Testprocess mit jUnit5 Framework
 * Von NB 12.4 generierter Test.
 *
 * @author rene
 */
@Timeout(10)
public class MediumLoadProcessorTest extends MediumLoadProcessorTestBase {

  public MediumLoadProcessorTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    System.out.println("Setting up Database Connection");
    setupDatabaseConnection();
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
//    log("Cleaning Database");
    cleanDatabase();
//    log("Setting up start data");
    setUpBaseData();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of setMedium method, of class MediumLoadProcessor.
   */
  @Test
  @Disabled
  public void testSetMedium() {
    log("setMedium");
    PictureMedium medium = new PictureMedium();
    PictureMedium pm = new PictureMedium();
    pm.setCode(101);
    pm.setLabel("Test Label");
    pm.setStorageMedium(m_mType1);
    pm.update();
    assertNotNull(pm.getId());

    MediumLoadProcessor instance = new MediumLoadProcessor();
    instance.setMedium(medium);
  }

  /**
   * Test of process method, of class MediumLoadProcessor.
   * Not running in a thread like in the application
   * Test with test folder CD1
   */
  @Test
  public void testProcessCD1() throws InterruptedException {
    log("Started process CD1");
    List<PictureMedium> mediumListOld = PictureMedium.getAllStorageMedia();
    int oldMediumCount = mediumListOld.size();
    System.out.println("Testing process with one folder, already " + oldMediumCount + " folder in database");
    int numberOfRoots = loadData(m_picMedium1, TEST_DATA_FOLDER + "/CD1/");
    assertEquals(1, numberOfRoots, "Number of created folder roots in the database");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      long activeId = m_picMedium1.getId();
      PictureMedium activeMedium = em.find(PictureMedium.class, activeId);
      Set<PicDirectory> directories = activeMedium.getPicDirectories();
      int sizeOfDirectories = directories.size();
      if (sizeOfDirectories > 2) {
        log("Mehr als zwei Directory erzeugt");
        directories.forEach(dir -> {
          System.out.println(" -> " + dir.getDirectoryName());
        });
      }
      assertEquals(2, sizeOfDirectories, "Number of created folder in Database");
      directories.forEach(dir -> {
        String dirName = dir.getDirectoryName();
        switch (dirName) {
          case "CD1" -> {
            // should have one dir-child, and no pictures
            assertEquals(1, dir.getChildren().size(), "Count of child directories");
            assertEquals(0, dir.getPictures().size(), "Count of Files");
          }
          case "Landschaftsbau2021" -> {
            assertEquals(0, dir.getChildren().size(), "Count of child directories");
            assertEquals(28, dir.getPictures().size(), "Count of Files");
          }
          default ->
            fail("Involid directory name : " + dirName);
        }
      });
      em.getTransaction().commit();
    }
    countPicDirMap(28);
    countDigiPicture(28);
    countRecords("CAMERA", 2);   // 1 from setup, 1 from the import process
  }

  /**
   * Test die process methode der Klasse mit den Daten aus dem CD2 folder.
   * Sind 2 unterfolder, je 10 pics, jedoch 9 sind identisch
   */
  @Test
  public void testProcessCD2() {
    log("Started Process CD2");
    int numberOfRoots = loadData(m_picMedium2, TEST_DATA_FOLDER + "/CD2/");
    assertEquals(1, numberOfRoots, "Number of created folder roots in the database");
    long activeId = m_picMedium2.getId();
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium activeMedium = em.find(PictureMedium.class, activeId);
      Set<PicDirectory> directories = activeMedium.getPicDirectories();
      assertEquals(3, directories.size(), "Count of created folder records");
      directories.forEach((PicDirectory dir) -> {
        String dirName = dir.getDirectoryName();
        switch (dirName) {
          case "CD2" -> {
            // should have one dir-child, and no pictures
            assertEquals(2, dir.getChildren().size(), "Count of child directories");
            assertEquals(0, dir.getPictures().size(), "Count of Files");
          }
          case "Landschaftsbau2020" -> {
            assertEquals(0, dir.getChildren().size(), "Count of child directories");
            assertEquals(10, dir.getPictures().size(), "Count of Files");
          }
          case "TestVerzeichnis" -> {
            assertEquals(0, dir.getChildren().size(), "Count of child directories");
            assertEquals(10, dir.getPictures().size(), "Count of Files");
          }
          default ->
            fail("Involid directory name : " + dirName);
        }
      });
      em.getTransaction().commit();
    }
    countPicDirMap(20);
    countDigiPicture(11);
    countRecords("CAMERA", 2);   // 1 from setup, 1 from the import process
  }

  /**
   * Test die process methode der Klasse mit den Daten aus dem CD3 folder.
   * Sind 2 unterfolder, je 10 pics, jedoch 9 sind identisch
   */
  @Test
  public void testProcessCD3() {
    log("Started CD3");
    int numberOfRoots = loadData(m_picMedium2, TEST_DATA_FOLDER + "/CD3/");
    assertEquals(1, numberOfRoots, "Number of created folder roots in the database");
    long activeId = m_picMedium2.getId();
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium activeMedium = em.find(PictureMedium.class, activeId);
      Set<PicDirectory> directories = activeMedium.getPicDirectories();
      assertEquals(4, directories.size(), "Count of created folder records");
      directories.forEach(dir -> {
        String dirName = dir.getDirectoryName();
        switch (dirName) {
          case "CD3":
            // should have one dir-child, and no pictures
            assertEquals(2, dir.getChildren().size(), "Count of child directories");
            assertEquals(3, dir.getPictures().size(), "Count of Files");
            break;
          case "Landschaftsbau2019":
            assertEquals(0, dir.getChildren().size(), "Count of child directories");
            assertEquals(10, dir.getPictures().size(), "Count of Files");
            break;
          case "TestVerzeichnis":
            assertEquals(1, dir.getChildren().size(), "Count of child directories");
            assertEquals(10, dir.getPictures().size(), "Count of Files");
            break;
          case "TV4":
            assertEquals(0, dir.getChildren().size(), "Count of child directories");
            assertEquals(4, dir.getPictures().size(), "Count of Files");
            break;
          default:
            fail("Involid directory name : " + dirName);
            break;
        }
      });
      em.getTransaction().commit();
    }
    countPicDirMap(27);
    countDigiPicture(16);
  }

  /**
   * Validates the processing for different pictures with the same filename,
   * identification via the picture taken date (filed millis).
   *
   */
  @Test
  public void testProcessCD4() {

    log();
    assertEquals(1, loadData(m_picMedium1, TEST_DATA_FOLDER + "/CD4/"), "Loading CD4");
    countPicDirMap(28);
    countDigiPicture(28);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(
              "SELECT ID FROM DIGI_PICTURE WHERE FILE_NAME = 'P1011617.JPG'");
      List result = query.getResultList();
      System.out.println("Result size is " + result.size());
      assertEquals(2, result.size(),
              "P1011617.JPG shall be double in DB");
    }
  }

  /**
   * wie testProcessCD4, jedoch ist in einem weiteren subfolder eine echte Kopie
   * von P1011617.JPG, die nicht doippelt angelegt werden sollte.
   *
   */
  @Test
  public void testProcessCD5() {
    log();
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD5/"), "Loading CD4");
    countPicDirMap(29);
    countDigiPicture(28);
    countDirectory(4);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(
              "SELECT ID FROM DIGI_PICTURE WHERE FILE_NAME = 'P1011617.JPG'");
      List result = query.getResultList();
      System.out.println("Result size is " + result.size());
      assertEquals(2, result.size(), "P1011617.JPG shall be only double in DB");
    }
  }

  @Test
  @DisplayName("4 different cameras")
  void testProcessCD18() {
    doStandardTest("CD18", 5, 19, 19, 5);

    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PictureMedium medium = em.find(PictureMedium.class,
              m_picMedium1Id);
      PicDirectory rootDir = getRootFolder(medium);
      assertEquals("CD18", rootDir.getDirectoryName(), "Name of root folder");
      Set<PicDirectory> dirs = rootDir.getChildren();
      assertEquals(4, dirs.size(), "Number of sub folder of root");
      for (PicDirectory dir : dirs) {
        Set<DigiPicture> pics = dir.getPictures();
        String directoryName = dir.getDirectoryName();
        assertNotNull(pics, "No pics in folder " + directoryName);
        String[] fileNames = switch (directoryName) {
          case "Canon" ->
            new String[]{
              "IMG_1084.JPG", "IMG_1085.JPG", "IMG_1086.JPG", "IMG_1087.JPG"};
          case "HTC" ->
            new String[]{
              "IMAG0190.jpg", "IMAG0191.jpg", "IMAG0192.jpg",
              "IMG-20170319-WA0000.jpeg"};

          case "Panasonic" ->
            new String[]{
              "P1100232.JPG", "P1100233.JPG", "P1100234.JPG",
              "P1100235.JPG", "P1100236.JPG", "P1100237.JPG"};

          case "Samsung" ->
            new String[]{
              "SAM_0086.JPG", "SAM_0087.JPG", "SAM_0088.JPG",
              "SAM_0089.JPG", "SAM_0090.JPG"};
          default ->
            null;
        };
        assertNotNull(fileNames, "Invalid folder in test");
        validateAllFileNames(pics, fileNames);
        DigiPicture pic = pics.iterator().next();    // Grab one picture
        // finally check camera names
        String model = switch (directoryName) {
          case "Canon" ->
            "Canon IXUS 170";
          case "HTC" ->
            "HTC One_M8";
          case "Panasonic" ->
            "DMC-GF3";
          case "Samsung" ->
            "SAMSUNG PL90/VLUU PL90";
          default ->
            "NIX";
        };
        assertEquals(model, pic.getCamera().getModel(),
                "Cameramodel for pic " + pic.getFileName());
      }
    }
  }

  @Test
  @DisplayName("Realer folder, CD aus Urlaub. Aelter und mit Video")
  @Timeout(100)
  void testProcessCD19() {
    doStandardTest("CD19", 4, 712, 712, 4);
  }

}
