/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.tools;

import static bas.TestBase.TEST_DATA_FOLDER;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.antlr.v4.runtime.tree.Trees;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.PictureMedium;

/**
 *
 * @author rene
 */
public class MediumLoadProcessorInitialTest
        extends MediumLoadProcessorTestBase {

  public MediumLoadProcessorInitialTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
    initLogging();

  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
    setUpBaseData();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Simple, initial Test. processes one Directory with one picture.
   */
  @Test
  public void testProcessOneImage() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD14/"), "Loading CD14");
    countPicDirMap(1);
    countDigiPicture(1);
    countDirectory(1);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
    checkForEmptyImages();
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNamedQuery("FindPicDirectoryByMediumID");
      query.setParameter("id", 1L);
      List<PicDirectory> dirs = query.getResultList();
      assertEquals(1, dirs.size(), "Wrong number of directories");
      PicDirectory dir1 = dirs.iterator().next();  // Get first and only entry
      assertEquals("CD14", dir1.getDirectoryName(), "Wrong directory name");
      Set<DigiPicture> pics = dir1.getPictures();
      assertEquals(1, pics.size(), "Sollten doch nur ein Bild sein");
      DigiPicture pic1 = pics.iterator().next();
      assertEquals("P1011615.JPG", pic1.getFileName(), "Falscher File Name");
    }
  }

  /**
   * Simple, initial Test. processes 2 Directories with one pictures.
   */
  @Test
  public void testProcessOneImageAndOneFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD14-1/"), "Loading CD14-1");
    countPicDirMap(1);
    countDigiPicture(1);
    countDirectory(2);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
    checkForEmptyImages();

// Validating via SQL
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(
              "SELECT ID,DIRECTORY_NAME FROM PIC_DIRECTORY WHERE MEDIUM="
              + m_picMedium1Id + " ORDER BY ID");
      List<Object[]> res = query.getResultList();
      assertEquals(2, res.size());
      Object[] o = res.get(0);
      System.out.println("o is " + o.getClass().getName());
      assertEquals(1, ((Long) o[0]).longValue(), "ID should be 1");
      assertEquals("CD14-1", o[1], "Filename?");
      o = res.get(1);
      assertEquals(2, ((Long) o[0]).longValue(), "ID should be 2");
      assertEquals("Verzeichnis1", o[1], "Verzeichnisname?");

      query = em.createNativeQuery("SELECT FILE_NAME FROM DIGI_PICTURE WHERE ID = 1");
      List res2 = query.getResultList();
      assertEquals(1, res2.size());
      assertEquals("P1011615.JPG", res2.get(0), "Wrong image file name");

    }
  }

  /**
   * Simple, initial Test. processes 2 Directories with one pictures.
   */
  @Test
  @Timeout(5L)
  public void testProcessOneImagePlus2FurtherFilesAndOneFolder()
          throws InterruptedException {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD14-2/"), "Loading CD14-2");
    countPicDirMap(3);
    countDigiPicture(3);
    countDirectory(2);
    countRecords("CAMERA", 3);    // one created during setup and one during the import for the umage,
    countEmptyImages(2);          // one entry for the two non image files
    log("Initial count test done");

// Validating via JPA
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium medium = em.find(PictureMedium.class, 1);
      assertNotNull(medium, "Medium not found for id 1");
      Set<PicDirectory> dirs = medium.getPicDirectories();
      Assertions.assertAll("Valid Set of Directories",
              () -> assertNotNull(dirs),
              () -> Assertions.assertNotEquals(0, dirs.isEmpty())
      );
      assertEquals(2, dirs.size());
//      PicDirectory dir = dirs.iterator().next();  // Get first and the only entry)
//      assertEquals("CD14-2", dir.getDirectoryName(),
//              "Wrong folder name");
//      Set<DigiPicture> pics = dir.getPictures();
//      Assertions.assertAll("Valid Set of pictures",
//              () -> assertNotNull(pics),
//              () -> assertEquals(0, pics.size())
//      );
      log("Initial assertion test done, starting folder analysis");
      Iterator<PicDirectory> iterator = dirs.iterator();
      while (iterator.hasNext()) {
        PicDirectory dir = iterator.next();
        String name = dir.getDirectoryName();
        switch (name) {
          case "CD14-2":
            assertEquals(0, dir.getPictures().size());
            assertNull(dir.getParent());
            Set<PicDirectory> nd = dir.getChildren();
            assertEquals(1, nd.size());
            break;
          case "Verzeichnis1":
            assertEquals(3, dir.getPictures().size());
            assertNotNull(dir.getParent());
            Set<PicDirectory> nd2 = dir.getChildren();
            assertEquals(0, nd2.size());
            break;
        }
      }
      log("Finishing - closing transaction");
      em.getTransaction().commit();
    }
  }

  /**
   * Simple, initial Test. processes 1 Directories with three pictures.
   */
  @Test
  @Timeout(5)
  public void testProcessThreeImageAndOneFolder()
          throws InterruptedException {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD15/"), "Loading CD15");
    countPicDirMap(3);
    countDigiPicture(3);
    countDirectory(1);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium medium = em.find(PictureMedium.class, 1);
      Set<PicDirectory> dirs = medium.getPicDirectories();
      assertEquals(1, dirs.size(), "Number of directories on medium");
      PicDirectory rootDir = dirs.iterator().next();
      assertNotNull(rootDir);
      assertEquals("CD15", rootDir.getDirectoryName());
      Set<DigiPicture> pics = rootDir.getPictures();

      String[] expFileNames = new String[]{
        "P1011615.JPG", "P1011616.JPG", "P1011617.JPG"
      };
      validateAllFileNames(pics, expFileNames);
      em.getTransaction().commit();
    }
  }

  /**
   * Simple, initial Test. processes 2 Directories with three pictures.
   * Data in Folder CD15-1
   */
  @Timeout(5)
  @Test
  public void testProcessThreeImageAndTwoFolder() throws InterruptedException {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD15-1/"), "Loading CD15-1");
    countPicDirMap(3);
    countDigiPicture(3);
    countDirectory(2);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 2 Directories with three pictures.
   */
  @Test
  public void testProcessThreeImageAndThreeFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD15-2/"), "Loading CD15-2");
    countPicDirMap(3);
    countDigiPicture(3);
    countDirectory(3);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 2 Directories with three pictures.
   */
  @Test
  public void testProcessFourImageAndThreeFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD16/"), "Loading CD16");
    countPicDirMap(4);
    countDigiPicture(3);
    countDirectory(3);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 2 Directories with three pictures.
   */
  @Test
  public void testProcessOneImageAndTwoFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD16-1/"), "Loading CD16-1");
    countPicDirMap(2);
    countDigiPicture(1);
    countDirectory(2);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 3 Directories with one pictures.
   */
  @Test
  public void testProcessOneImageAndThreeFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD16-2/"), "Loading CD16-2");
    countPicDirMap(2);
    countDigiPicture(1);
    countDirectory(3);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 3 Directories with one pictures.
   */
  @Test
  public void testProcessTwoImageAndTwoFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD16-3/"), "Loading CD16-3");
    countPicDirMap(3);
    countDigiPicture(2);
    countDirectory(2);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 3 Directories with one pictures.
   */
  @Test
  public void testProcessTwoImageAndThreeFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD16-4/"), "Loading CD16-4");
    countPicDirMap(3);
    countDigiPicture(2);
    countDirectory(3);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 3 Directories with one pictures.
   */
  @Test
  public void testProcessTwoImageAndThreeFolderReverse() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD16-5/"), "Loading CD16-5");
    countPicDirMap(3);
    countDigiPicture(2);
    countDirectory(3);
    countRecords("CAMERA", 2);    // one created during setup and one during the import
  }

  /**
   * Simple, initial Test. processes 3 Directories with one pictures.
   */
  @Test
  void testProcessFourIdenticalImageInFourFolder() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD17/"), "Loading CD17");
    countPicDirMap(16);
    countDigiPicture(4);
    countDirectory(5);
    countRecords("CAMERA", 2);    // one created during setup and one during the import

// Now for the structure
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      String sqlSatz = "SELECT COUNT(*) FROM PIC_DIR_MAP WHERE DIGI_PICTURE_ID=";
      for (int i = 1; i < 5; i++) {
        String sql = sqlSatz + i;
        Query query = em.createNativeQuery(sql);
        Object result = query.getSingleResult();
        assertEquals(4, ((Long) result).longValue());
      }
      sqlSatz = "SELECT COUNT(*) FROM PIC_DIR_MAP WHERE PIC_DIRECTORY_ID=";
      for (int i = 2; i < 6; i++) {     // Directory with 1 is the root
        String sql = sqlSatz + i;
        Query query = em.createNativeQuery(sql);
        Object result = query.getSingleResult();
        assertEquals(4, ((Long) result).longValue());
      }
    }
  }

  @Test
  @DisplayName("4 subfolder with identical Files plus 2 non image files")
  void testCD17_1() {
    log("Starting >>> ");
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/CD17-1/"), "Loading CD17-1");
    countPicDirMap(18);
    countDigiPicture(6);
    countDirectory(5);
    countRecords("CAMERA", 3);    // one created during setup and one during the import
  }

  @Test
  @DisplayName("4 stacked subfolder with 4 identical images each")
  void testCD17_4() {
    doStandardTest("CD17-4", 5, 20, 4, 2);
  }

  @Test
  @DisplayName("CD17-2: Einfolder mit 4 Bildern in mehrern leeren foldern")
  void testCD17_2() {
    doStandardTest("CD17-2", 7, 4, 4, 2);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium medium = em.find(PictureMedium.class, 1);
      Set<PicDirectory> dirs = medium.getPicDirectories();
      assertEquals(7, dirs.size());
      for (PicDirectory dir : dirs)
        switch (dir.getDirectoryName()) {
          case "dummy":
            assertEquals(0, dir.getPictures().size(), "dummy shall have no pictures");
            assertEquals(2, dir.getChildren().size(), "Dummy shall have 2 sub folder");
            break;
          case "U":
            assertEquals(1, dir.getChildren().size(), "Subfoldercount of U");
            assertEquals(4, dir.getPictures().size(), "Picturecount in U");
            break;
          case "V":
            assertEquals(0, dir.getChildren().size(), "V no subfolder");
            assertEquals(0, dir.getPictures().size(), "Pictures in V");
            break;
          case "X":
          case "Y":
            assertEquals(0, dir.getChildren().size(), "X or Y no subfolder");
            assertEquals(0, dir.getPictures().size(), "Pictures in X or Y");
            break;
          case "CD17-2":
            assertEquals(3, dir.getChildren().size());
            assertEquals(0, dir.getPictures().size());
            break;
          default:
            assertTrue(false, "Unknown folder");

        }
      em.getTransaction().commit();
    }
  }

  @Test
  @DisplayName("Tree with empty folders")
  void testEmptyFolders() {
    doStandardTest("CD17-3", 6, 0, 0, 1);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium medium = em.find(PictureMedium.class, m_picMedium1Id);
      Set<PicDirectory> allDirs = medium.getPicDirectories();
      assertEquals(6, allDirs.size());
      PicDirectory root = null;;
      for (PicDirectory dir : allDirs)               // Filter the root folder
        if (dir.getParent() == null)
          root = dir;
      assertEquals("CD17-3", root.getDirectoryName());
      Set<PicDirectory> dirs1 = root.getChildren();
      assertEquals(2, dirs1.size(), "First level of folder");
      for (PicDirectory dir : dirs1)
        switch (dir.getDirectoryName()) {
          case "V1":
            Set<PicDirectory> u = dir.getChildren();
            assertEquals(1, u.size(), "Subfolder V1");
            PicDirectory du = u.iterator().next();
            assertEquals("U", du.getDirectoryName());
            Set<PicDirectory> v = du.getChildren();
            assertEquals(1, v.size(), "Subfolder U");
            PicDirectory dv = v.iterator().next();
            assertEquals("V", dv.getDirectoryName());
            Set<PicDirectory> w = dv.getChildren();
            assertEquals(1, w.size(), "Subfolder V");
            PicDirectory dw = w.iterator().next();
            assertEquals("W", dw.getDirectoryName());
            Set<PicDirectory> x = dw.getChildren();
            assertEquals(0, x.size(), "Subfolder W");

            break;
          case "V2":
            assertEquals(0, dir.getChildren().size(), "Subfolder in V2");
            assertEquals(0, dir.getPictures().size(), "Images in V2");
            break;
          default:
            assertTrue(false, "Invalid folder in root");
        }
      em.getTransaction().commit();
    }
  }
}
