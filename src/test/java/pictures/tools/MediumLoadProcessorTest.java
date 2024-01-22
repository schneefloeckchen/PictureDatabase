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
import jakarta.transaction.Transactional;
import java.io.File;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Testprocess mit jUnit5 Framework, die domain tests sind noch jUnit4.
 * Von NB 12.4 generierter Test.
 *
 * @author rene
 */
public class MediumLoadProcessorTest extends TestBaseClass {

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
  public void testProcessCD1() {
    log("Started");
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
  }

  /**
   * Test die process methode der Klasse mit den Daten aus dem CD2 folder.
   * Sind 2 unterfolder, je 10 pics, jedoch 9 sind identisch
   */
  @Test
  public void testProcessCD2() {
    log("Started");
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
  }

  /**
   * Test die process methode der Klasse mit den Daten aus dem CD3 folder.
   * Sind 2 unterfolder, je 10 pics, jedoch 9 sind identisch
   */
  @Test
  public void testProcessCD3() {
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
    assertEquals(1, loadData(m_picMedium1, TEST_DATA_FOLDER + "/CD4/"), "Loading CD4");
    countPicDirMap(28);
    countDigiPicture(28);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(
          "SELECT ID FROM DIGI_PICTURE WHERE FILE_NAME = 'P1011617.JPG'");
      List result = query.getResultList();
      System.out.println("Result size is " + result.size());
      assertEquals(2, result.size(), "P1011617.JPG shall be double in DB");
    }
  }

  /**
   * wie testProcessCD4, jedoch ist in einem weiteren subfolder eine echte Kopie
   * von P1011617.JPG, die nicht doippelt angelegt werden sollte.
   *
   */
  @Test
  public void testProcessCD5() {
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

  protected int loadData(PictureMedium medium, String testDataFolder) {
    File folder = new File(testDataFolder);
    MediumLoadProcessor instance = new MediumLoadProcessor();
    instance.setToTest();
    instance.setMedium(medium);
    return instance.process(folder);
  }

}
